package bg.sofia.uni.fmi.mjt.cryptowallet.users;

import bg.sofia.uni.fmi.mjt.cryptowallet.assets.Asset;
import bg.sofia.uni.fmi.mjt.cryptowallet.exceptions.*;
import bg.sofia.uni.fmi.mjt.cryptowallet.wallet.transaction.BoughtCryptoTransaction;
import bg.sofia.uni.fmi.mjt.cryptowallet.wallet.transaction.DepositMoneyTransaction;
import bg.sofia.uni.fmi.mjt.cryptowallet.wallet.transaction.SoldCryptoTransaction;
import bg.sofia.uni.fmi.mjt.cryptowallet.wallet.transaction.Transaction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class UsersDatabaseTest {
    private static final String TEST_FILE_NAME = "resources/test_users.txt";
    private static final String TEST_USERNAME = "ami566";
    private static final String TEST_PASSWORD = "12345";
    private static final Path FILE_PATH = Path.of(TEST_FILE_NAME);
    private static UsersDatabase userRepository;

    @BeforeEach
    void setup() throws IOException, ClassNotFoundException {
        if (!Files.exists(FILE_PATH)) {
            Files.createFile(FILE_PATH);
        }

        userRepository = new UsersDatabase(FILE_PATH);
    }

    @AfterEach
    void teardown() throws IOException {
        Files.deleteIfExists(FILE_PATH);
    }

    @Test
    public void testRegisterUser()
            throws UserAlreadyExistsException, NoSuchUserException {
        userRepository.register(TEST_USERNAME, TEST_PASSWORD);
        User ivan = userRepository.getUserByUsername(TEST_USERNAME);

        assertEquals(TEST_USERNAME, ivan.getUsername(), "Registered user is not the same");
    }

    @Test
    public void testRegisterUserWithExistingUsernameThrowsUserAlreadyExistsException() throws UserAlreadyExistsException {
        userRepository.register(TEST_USERNAME, TEST_PASSWORD);

        assertThrows(UserAlreadyExistsException.class,
                () -> userRepository.register(TEST_USERNAME, TEST_PASSWORD),
                "Cannot have two users with the same username");
    }

    @Test
    public void testRegisterUserWithNullAndEmptyUsername() {
        assertThrows(IllegalArgumentException.class,
                () -> userRepository.register("", TEST_PASSWORD),
                "Cannot have user with empty username");
        assertThrows(IllegalArgumentException.class,
                () -> userRepository.register(null, TEST_PASSWORD),
                "Cannot have user with null username");
    }

    @Test
    public void testRegisterUserWithNullAndEmptyPassword() {
        assertThrows(IllegalArgumentException.class,
                () -> userRepository.register("aaaa", ""),
                "Cannot have user with empty password");
        assertThrows(IllegalArgumentException.class,
                () -> userRepository.register("aaaa", null),
                "Cannot have user with null password");
    }

    @Test
    public void testLoginUserThatDoesNotExists() {
        assertThrows(NoSuchUserException.class,
                () -> userRepository.login("dsa", TEST_PASSWORD),
                "Cannot have user with empty username");
    }

    @Test
    public void testLoginWithInvalidCredentialsThrowsUnauthorizedException() throws UserAlreadyExistsException {
        userRepository.register(TEST_USERNAME, TEST_PASSWORD);
        assertThrows(UnauthorizedException.class, () -> userRepository.login(TEST_USERNAME, "test"));
    }

    @Test
    public void testDepositMoneyWithInvalidUserThrowsIllegalArgumentException() {
        User test = new User(TEST_USERNAME, TEST_PASSWORD, 200);
        assertThrows(NoSuchUserException.class, () -> userRepository.deposit(test, 120),
                "User that is not in the database cannot deposit money");
    }


    @Test
    public void testDepositMoneyWithNegativeAmountThrowsIllegalArgumentException() throws UserAlreadyExistsException {
        userRepository.register(TEST_USERNAME, TEST_PASSWORD);

        User test = new User(TEST_USERNAME, TEST_PASSWORD, 200);
        assertThrows(IllegalArgumentException.class, () -> userRepository.deposit(test, -12),
                "You cannot deposit negative amount of money");
    }

    @Test
    public void testDepositMoneyDepositsMoneyCorrectly() throws UserAlreadyExistsException, NoSuchUserException, UnauthorizedException {
        userRepository.register(TEST_USERNAME, TEST_PASSWORD);

        User test = new User(TEST_USERNAME, TEST_PASSWORD, 0);
        userRepository.deposit(test, 600);
        User dbUser = userRepository.getUserByUsername(TEST_USERNAME);

        assertEquals(600, dbUser.getWallet().getMoneyInAccount(), "Invalid wallet balance after deposit");
    }

    @Test
    public void testDepositMoneyCorrect() throws UserAlreadyExistsException, NoSuchUserException, UnauthorizedException {
        userRepository.register(TEST_USERNAME, TEST_PASSWORD);

        User test = new User(TEST_USERNAME, TEST_PASSWORD, 0);
        userRepository.deposit(test, 400);
        User dbUser = userRepository.getUserByUsername(TEST_USERNAME);

        assertEquals(400, dbUser.getWallet().getMoneyInAccount(), "Invalid wallet balance after deposit");
    }

    @Test
    public void testBuyCryptoWithNegativeAmountThrowsIllegalArgumentException() throws IOException, UserAlreadyExistsException {
        userRepository.register(TEST_USERNAME, TEST_PASSWORD);
        User test = new User(TEST_USERNAME, TEST_PASSWORD, 0);
        Asset testAsset = new Asset("test", "test", 1, 100.0,
                "test",
                "test");

        assertThrows(IllegalArgumentException.class, () -> userRepository.buyCrypto(test, testAsset, -1),
                "Buying crypto with negative amount should throw exception");
    }

    @Test
    public void testBuyCryptoWithInsufficientAmountInWalletThrowsNotEnoughMoneyException() throws IOException, UserAlreadyExistsException {
        userRepository.register(TEST_USERNAME, TEST_PASSWORD);
        User test = new User(TEST_USERNAME, TEST_PASSWORD, 200);
        Asset testAsset = new Asset("test", "test", 1, 100.0,
                "test",
                "test");

        assertThrows(NotEnoughMoneyInWalletException.class, () -> userRepository.buyCrypto(test, testAsset, 300),
                "Buying crypto without having money should throw exception");
    }


    @Test
    public void testBuyCryptoExecutesBuyTransaction() throws UserAlreadyExistsException, NotEnoughMoneyInWalletException, NoSuchUserException, UnauthorizedException {
        userRepository.register(TEST_USERNAME, TEST_PASSWORD);
        userRepository.getUserByUsername(TEST_USERNAME).depositMoney(200);
        User test = new User(TEST_USERNAME, TEST_PASSWORD, 200);
        Asset testAsset = new Asset("test", "test", 1, 100.0,
                "test",
                "test");
        List<Transaction> transactions = List.of(new DepositMoneyTransaction(200), new BoughtCryptoTransaction(150, 1.5, testAsset.assetId(), 100.0));

        userRepository.buyCrypto(test, testAsset, 150);
        User dbUser = userRepository.getUserByUsername(TEST_USERNAME);
        List<String> s = transactions.stream().map(Transaction::transactionString).toList();
        List<String> result = dbUser.getTransactions().stream().map(Transaction::transactionString).toList();
        assertEquals(150, dbUser.getWallet().getSpentMoney().get(testAsset), "Invalid money spend on crypto");
        assertEquals(s, result, "Invalid transactions generated");
    }

    @Test
    public void testGeneratingTransactionsCorrect() throws UserAlreadyExistsException, NotEnoughMoneyInWalletException, NoSuchUserException, UnauthorizedException, CryptoCurrencyNotInWalletException {
        userRepository.register(TEST_USERNAME, TEST_PASSWORD);
        userRepository.getUserByUsername(TEST_USERNAME).depositMoney(200);
        User test = new User(TEST_USERNAME, TEST_PASSWORD, 200);
        Asset testAsset = new Asset("test", "test", 1, 100.0,
                "test",
                "test");
        List<Transaction> transactions = List.of(new DepositMoneyTransaction(200),
                new BoughtCryptoTransaction(150, 1.5, testAsset.assetId(), 100.0),
                new SoldCryptoTransaction(150, 1.5, testAsset.assetId(), 100.0));

        userRepository.buyCrypto(test, testAsset, 150);
        userRepository.sellCrypto(test, testAsset);
        // userRepository.withdraw(test, 100);
        User dbUser = userRepository.getUserByUsername(TEST_USERNAME);
        List<String> s = transactions.stream().map(Transaction::transactionString).toList();
        List<String> result = dbUser.getTransactions().stream().map(Transaction::transactionString).toList();
        assertEquals(s, result, "Invalid transactions generated");
    }

    @Test
    public void testSellCryptoWithNotBoughtAssetThrowsException() throws UserAlreadyExistsException {
        userRepository.register(TEST_USERNAME, TEST_PASSWORD);

        User test = new User(TEST_USERNAME, TEST_PASSWORD, 200);
        Asset testAsset = new Asset("test", "test", 1, 100.0,
                "test",
                "test");

        assertThrows(CryptoCurrencyNotInWalletException.class, () -> userRepository.sellCrypto(test, testAsset),
                "Trying to sell not bought asset is not possible");
    }

    @Test
    public void testBuyCryptoTwiceAndWithdraw() throws UserAlreadyExistsException, NotEnoughMoneyInWalletException, NoSuchUserException, UnauthorizedException, CryptoCurrencyNotInWalletException {
        userRepository.register(TEST_USERNAME, TEST_PASSWORD);
        User test = new User(TEST_USERNAME, TEST_PASSWORD, 400);
        Asset testAsset = new Asset("test", "test", 1, 100.0,
                "test",
                "test");
        userRepository.deposit(test, 400);
        userRepository.buyCrypto(test, testAsset, 190);
        userRepository.buyCrypto(test, testAsset, 200);
        User dbUser = userRepository.getUserByUsername(TEST_USERNAME);
        userRepository.withdraw(dbUser, 10);
        dbUser = userRepository.getUserByUsername(TEST_USERNAME);
        assertEquals(0, dbUser.getWallet().getMoneyInAccount(), "Operations not executed correctly");
    }

    @Test
    public void testLoginCorrect() throws UserAlreadyExistsException, NoSuchUserException, UnauthorizedException {
        userRepository.register(TEST_USERNAME, TEST_PASSWORD);
        userRepository.login(TEST_USERNAME, TEST_PASSWORD);
        assertTrue(userRepository.getUserByUsername(TEST_USERNAME).isAuthorizedForTransactions(), "Should return true when logged in");
    }

    @Test
    public void testLogoutCorrect() throws UserAlreadyExistsException, NoSuchUserException, UnauthorizedException {
        userRepository.register(TEST_USERNAME, TEST_PASSWORD);
        userRepository.getUserByUsername(TEST_USERNAME).logout();
        assertThrows(UnauthorizedException.class, () -> userRepository.getUserByUsername(TEST_USERNAME).isAuthorizedForTransactions(), "Should return true when logged in");
    }
}

