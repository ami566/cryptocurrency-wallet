package bg.sofia.uni.fmi.mjt.cryptowallet.users;

import bg.sofia.uni.fmi.mjt.cryptowallet.assets.Asset;
import bg.sofia.uni.fmi.mjt.cryptowallet.exceptions.*;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class UsersDatabase {
    private final Path usersDbPath;
    private Map<String, User> users = new HashMap<>();

    public UsersDatabase(Path dbFile) throws IOException, ClassNotFoundException {
        usersDbPath = dbFile;
        loadUsersFromFile();
    }

    private void loadUsersFromFile() throws ClassNotFoundException, IOException {
        File file = usersDbPath.toFile();
        if (file.exists() && file.length() > 0) {
            try (var fileInputStream = Files.newInputStream(usersDbPath);
                 var objectInputStream = new ObjectInputStream(fileInputStream)
            ) {
                users = (Map<String, User>) objectInputStream.readObject();
            }
        }
    }

    public User getUserByUsername(String name) throws NoSuchUserException {
        if (checkStringEmptyOrNull(name)) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        checkIfUserExists(name);

        return users.get(name);
    }

    public void register(String username, String password) throws UserAlreadyExistsException {
        if (checkStringEmptyOrNull(username)) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }

        if (checkStringEmptyOrNull(password)) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }

        if (users.containsKey(username)) {
            throw new UserAlreadyExistsException("The given username is already used");
        }

        User user = new User(username, password);
        users.put(username, user);
        updateFile();
    }

    public void login(String username, String password) throws NoSuchUserException, UnauthorizedException {
        User user = getUserByUsername(username);
        if (!user.matchPasswords(password)) {
            throw new UnauthorizedException("Invalid username or password");
        }
        user.login();
    }

    public void logout(User user) {
        users.get(user.getUsername()).logout();
    }

    public void deposit(User user, double amount) throws UnauthorizedException, NoSuchUserException {
        checkIfUserExists(user.getUsername());
        users.get(user.getUsername()).depositMoney(amount);
        updateFile();
    }

    public void withdraw(User user, double amount) throws NotEnoughMoneyInWalletException, UnauthorizedException, NoSuchUserException {
        checkIfUserExists(user.getUsername());
        users.get(user.getUsername()).withdrawMoney(amount);
        updateFile();
    }

    public void sellCrypto(User user, Asset asset) throws CryptoCurrencyNotInWalletException, UnauthorizedException, NoSuchUserException {
        checkIfUserExists(user.getUsername());
        users.get(user.getUsername()).sellCrypto(asset);
        updateFile();
    }

    public void buyCrypto(User user, Asset asset, double amount) throws NotEnoughMoneyInWalletException, UnauthorizedException, NoSuchUserException {
        checkIfUserExists(user.getUsername());
        users.get(user.getUsername()).buyCrypto(asset, amount);
        updateFile();
    }

    private void updateFile() {
        try (var outputStream = new ObjectOutputStream(Files.newOutputStream(usersDbPath))) {
            outputStream.writeObject(users);
        } catch (IOException e) {
            System.out.println("Problem occurred while writing on file");
        }
    }

    private boolean checkStringEmptyOrNull(String str) {
        return (str == null || str.isEmpty());
    }

    private void checkIfUserExists(String username) throws NoSuchUserException {
        if (!users.containsKey(username)) {
            throw new NoSuchUserException("User with the given username does not exist in the database");
        }
    }
}
