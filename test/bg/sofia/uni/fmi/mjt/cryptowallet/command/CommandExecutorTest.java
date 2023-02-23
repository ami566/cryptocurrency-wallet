package bg.sofia.uni.fmi.mjt.cryptowallet.command;

import bg.sofia.uni.fmi.mjt.cryptowallet.assets.Asset;
import bg.sofia.uni.fmi.mjt.cryptowallet.assets.AssetsDatabase;
import bg.sofia.uni.fmi.mjt.cryptowallet.exceptions.*;
import bg.sofia.uni.fmi.mjt.cryptowallet.users.User;
import bg.sofia.uni.fmi.mjt.cryptowallet.users.UsersDatabase;
import bg.sofia.uni.fmi.mjt.cryptowallet.wallet.transaction.BoughtCryptoTransaction;
import bg.sofia.uni.fmi.mjt.cryptowallet.wallet.transaction.SoldCryptoTransaction;
import bg.sofia.uni.fmi.mjt.cryptowallet.wallet.transaction.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class CommandExecutorTest {
    @Mock
    private UsersDatabase usersDb = mock(UsersDatabase.class);

    @Mock
    private AssetsDatabase assetsDb = mock(AssetsDatabase.class);

    @InjectMocks
    private CommandExecutor commandExecutor = new CommandExecutor(assetsDb, usersDb);

    User test;
    @BeforeEach
    void setup() {
        test = test = new User("test", "test", 100);
    }
    @Test
    public void testExecuteReturnsUnknownCommand() throws NoSuchUserException, HttpException, URISyntaxException, UserAlreadyExistsException, NoSuchAssetException, NotEnoughMoneyInWalletException, CryptoCurrencyNotInWalletException {
        assertEquals("Unknown command", commandExecutor.execute(CommandCreator.newCommand("test")),
                "Unknown command was not reached");
        assertEquals("Unknown command", commandExecutor.execute(CommandCreator.newCommand("test"),
                        null),
                "Unknown command was not reached");
    }

    @Test
    public void testLoginSuccessfully()
            throws NoSuchUserException, HttpException, URISyntaxException, UserAlreadyExistsException {
        String actual = commandExecutor.execute(CommandCreator.newCommand("login ami566 1234"));

        assertEquals("Logged in successfully as ami566", actual, "Invalid login result");
    }

    @Test
    public void testLoginThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> commandExecutor.execute(CommandCreator.newCommand("login amira")),
                "IllegalArgumentException was not thrown");
    }

    @Test
    public void testLoginThrowsUnauthorizedException()
            throws HttpException, NoSuchUserException {
        doThrow(UnauthorizedException.class).when(usersDb).login(anyString(), anyString());

        assertThrows(UnauthorizedException.class,
                () -> commandExecutor.execute(CommandCreator.newCommand("login ami566 1234")),
                "UnauthorizedException was not thrown");
    }

    @Test
    public void testRegisterSuccessfully() throws NoSuchUserException, HttpException, URISyntaxException, UserAlreadyExistsException {
        String actual = commandExecutor.execute(CommandCreator.newCommand("register ami566 1234"));

        assertEquals("Registered successfully! Welcome ami566", actual, "Invalid register result");
    }

    @Test
    public void testRegisterThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> commandExecutor.execute(CommandCreator.newCommand("register ami566")),
                "IllegalArgumentException was not successfully");
    }

    @Test
    public void testHelpReturnsDescription()
            throws HttpException, URISyntaxException, NoSuchUserException, UserAlreadyExistsException {
        String actual = commandExecutor.execute(CommandCreator.newCommand("help"));
        String expected = """
                Supported commands:
                login <username> <password> 
                register <username> <password>
                logout
                list-offerings - Shows 50 cryptos from the api
                deposit <amount>
                withdraw <amount>
                buy --offering=<offering_code> --money=<amount>
                sell --offering=<offering_code>
                get-wallet-summary
                get-wallet-overall-summary""";
        assertEquals(expected, actual, "Incorrect help result");
    }

    @Test
    public void testListOfferingsThrowsHttpException() throws URISyntaxException, HttpException {
        when(assetsDb.getAllAssets()).thenThrow(HttpException.class);

        assertThrows(HttpException.class, () ->
                        commandExecutor.execute(CommandCreator.newCommand("list-offerings")),
                "get list-offerings did not throw exception");
    }

    @Test
    public void testListOfferingsReturnsAssets() throws HttpException, URISyntaxException, NoSuchUserException, UserAlreadyExistsException {
        Asset asset1 = new Asset("test", "test", 1, 100.0,
                "test",
                "test");
        Asset asset2 = new Asset("test2", "test2", 1, 200.0,
                "test2",
                "test2");
        Map<String, Asset> assets = new HashMap<>();
        assets.put("test1", asset1);
        assets.put("test2", asset2);
        String expected = assets.values().stream().map(Asset::toString).collect(Collectors.joining()).trim();
        when(assetsDb.getAllAssets()).thenReturn((assets));

        String actual = commandExecutor.execute(CommandCreator.newCommand("list-offerings"));
        assertEquals(expected, actual, "Invalid response of list offerings");
    }

    @Test
    public void testDepositCorrect() throws HttpException, URISyntaxException, NoSuchAssetException, NotEnoughMoneyInWalletException, CryptoCurrencyNotInWalletException, NoSuchUserException {
        String actual = commandExecutor.execute(CommandCreator.newCommand("deposit-money 1000"), test);

        assertEquals("1000.0 USD were deposited to your account", actual,
                "1000.0 dollars were not deposited");
    }

    @Test
    public void testDepositWithWrongMoneyThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> commandExecutor.execute(CommandCreator.newCommand("deposit-money test"), test),
                "Command executor should have failed with invalid money as argument");
    }

    @Test
    public void testDepositWithWrongArgumentsThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> commandExecutor.execute(CommandCreator.newCommand("deposit-money"), test),
                "Command executor should have failed without amount as argument");
    }

    @Test
    public void testWithdrawCorrect() throws HttpException, URISyntaxException, NoSuchAssetException, NotEnoughMoneyInWalletException, CryptoCurrencyNotInWalletException, NoSuchUserException {
        String actual = commandExecutor.execute(CommandCreator.newCommand("withdraw-money 50"), test);

        assertEquals("50.0 USD were withdrew from your account", actual,
                "50.0 dollars were not deposited");
    }

    @Test
    public void testWithdrawWithWrongMoneyThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> commandExecutor.execute(CommandCreator.newCommand("withdraw-money test"), test),
                "Command executor should have failed with invalid money as argument");
    }

    @Test
    public void testWithdrawWithWrongArgumentsThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> commandExecutor.execute(CommandCreator.newCommand("withdraw-money"), test),
                "Command executor should have failed without amount as argument");
    }

    @Test
    public void testLogout() throws NoSuchUserException, HttpException, URISyntaxException, NoSuchAssetException, NotEnoughMoneyInWalletException, CryptoCurrencyNotInWalletException {
        assertEquals("Logged out successfully", commandExecutor.execute(CommandCreator.newCommand("logout"), test));
    }

    @Test
    public void testBuyCryptoWithInvalidArgumentsThrowsInvalidArgumentsException() {
        assertThrows(IllegalArgumentException.class, () -> commandExecutor.execute(CommandCreator.newCommand(
                "buy"
        ), test), "Command executor should have failed with wrong arguments");
    }

    @Test
    public void testBuyCryptoWithInvalidFormatOfOfferingCodeThrowsInvalidArgumentsException() {
        assertThrows(IllegalArgumentException.class, () -> commandExecutor.execute(CommandCreator.newCommand(
                "buy --offering-code= --money=1000"
        ), test), "Command executor should have failed with wrong format of offerings code");
    }

    @Test
    public void testBuyCryptoWithMissingOfferingCodeThrowsInvalidArgumentsException() {
        assertThrows(IllegalArgumentException.class, () -> commandExecutor.execute(CommandCreator.newCommand(
                "buy --code=32 --money=1000"
        ), test), "Command executor should have failed with missing offering code");
    }

    @Test
    public void testBuyCryptoWithInvalidFormatOfMoneyThrowsInvalidArgumentsException() {
        assertThrows(IllegalArgumentException.class, () -> commandExecutor.execute(CommandCreator.newCommand(
                "buy --offering=BTC --money="
        ), test), "Command executor should have failed with wrong format of money");
    }

    @Test
    public void testBuyCryptoWithMissingMoneyThrowsInvalidArgumentsException() {
        assertThrows(IllegalArgumentException.class, () -> commandExecutor.execute(CommandCreator.newCommand(
                "buy --offering=BTC --amount=65"
        ), test), "Command executor should have failed with missing money");
    }

    @Test
    public void testBuyCryptoWithWrongMoneyArgumentInvalidArgumentsException() {
        assertThrows(IllegalArgumentException.class, () -> commandExecutor.execute(CommandCreator.newCommand(
                "buy --offering=BTC --money=test"
        ), test), "Command executor should have failed with illegal number passed to money");
    }

    @Test
    public void testBuyCryptoSuccessfulPurchase() throws NoSuchAssetException, NotEnoughMoneyInWalletException, CryptoCurrencyNotInWalletException, NoSuchUserException, HttpException, URISyntaxException {
        String actual = commandExecutor.execute(CommandCreator.newCommand("buy --offering=BTC --money=1000"), test);

        assertEquals("BTC for 1000.0 was successfully bought", actual, "BTC was not bought");
    }

    @Test
    public void testSellWithoutArgumentThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> commandExecutor.execute(CommandCreator.newCommand(
                "sell"
        ), test), "Command executor should have failed when no arguments passed");
    }

    @Test
    public void testSellSuccessfullySellCrypto() throws HttpException, URISyntaxException, NoSuchAssetException, NotEnoughMoneyInWalletException, CryptoCurrencyNotInWalletException, NoSuchUserException {
        String actual = commandExecutor.execute(CommandCreator.newCommand("sell --offering=BTC"), test);

        assertEquals("BTC was successfully sold", actual, "BTC was no sold");
    }

    @Test
    public void testGetWalletSummaryReturnsCorrectWalletInfo() throws HttpException, IOException, URISyntaxException, NoSuchAssetException, NotEnoughMoneyInWalletException, CryptoCurrencyNotInWalletException, NoSuchUserException {
        Asset test1 = new Asset("test", "test", 1, 100.0,
                "test",
                "test");
        Asset test2 = new Asset("test2", "test2", 1, 200.0,
                "test2",
                "test2");
        Transaction testTransaction1 = new BoughtCryptoTransaction(100, 1, test1.assetId(), test1.priceUsd());
        Transaction testTransaction2 = new SoldCryptoTransaction(100, 1, test1.assetId(), test2.priceUsd());

        test.buyCrypto(test1, 100);
        test.sellCrypto(test1);
        String transactionsStr = Stream.of(testTransaction1, testTransaction2)
                .map(Transaction::transactionString)
                .collect(Collectors.joining(System.lineSeparator()))
                .trim();
        String expected = String.format("%s\n%s", "Wallet balance: 100.00", transactionsStr);
        String actual = commandExecutor.execute(CommandCreator.newCommand("get-wallet-summary"), test);

        assertEquals(expected, actual, "Wallet summary is wrong");
    }

    @Test
    public void testGetWalletOverallSummaryReturnsNoInfo() throws HttpException, IOException, URISyntaxException, NoSuchAssetException, NotEnoughMoneyInWalletException, CryptoCurrencyNotInWalletException, NoSuchUserException {
        String actual = commandExecutor.execute(CommandCreator.newCommand("get-wallet-overall-summary"),
                test);

        assertEquals("There is no info", actual, "No info was expected");
    }

   // @Test
    public void testGetWalletOverallSummaryReturnsInfo() throws HttpException, IOException, URISyntaxException, NoSuchAssetException, NotEnoughMoneyInWalletException, CryptoCurrencyNotInWalletException, NoSuchUserException {
        test.depositMoney(300);
        Asset test1 = new Asset("test", "test", 1, 100.0,
                "test",
                "test");
        Asset test2 = new Asset("test2", "test2", 1, 200.0,
                "test2",
                "test2");
        test.buyCrypto(test1, 100);
        test.buyCrypto(test2, 200);

        when(assetsDb.getAssetById("test")).thenReturn(new Asset("test", "test",
                1, 150.0, "test", "test"));
        when(assetsDb.getAssetById("test2")).thenReturn(new Asset("test2", "test2",
                1, 170.0,
                "test2",
                "test2"));

        String expected = """
            test {
                buyValue: '100.00',
                sellValue: '150.00',
                gained: '50.00',
                lost: '0.00'
            }
            test2 {
                buyValue: '200.00',
                sellValue: '170.00',
                gained: '0.00',
                lost: '30.00'
            }""";
        String actual = commandExecutor.execute(CommandCreator.newCommand("get-wallet-overall-summary"),
                test);

        assertEquals(expected, actual, "Invalid overall summary");
    }
}
