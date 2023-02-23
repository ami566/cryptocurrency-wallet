package bg.sofia.uni.fmi.mjt.cryptowallet.users;

import bg.sofia.uni.fmi.mjt.cryptowallet.assets.Asset;
import bg.sofia.uni.fmi.mjt.cryptowallet.assets.AssetsDatabase;
import bg.sofia.uni.fmi.mjt.cryptowallet.exceptions.*;
import bg.sofia.uni.fmi.mjt.cryptowallet.wallet.Wallet;
import bg.sofia.uni.fmi.mjt.cryptowallet.wallet.transaction.Transaction;

import java.io.Serializable;
import java.net.URISyntaxException;
import java.util.List;

public class User implements Serializable {

    private static final long serialVersionUID = 1357924680L;
    private final String username;
    private final String password;
    private final Wallet wallet;
    private boolean isLoggedIn;

    public User(String username, String password, double money) {
        this.username = username;
        this.password = password;
        isLoggedIn = true;
        wallet = new Wallet(money);
    }

    public User(String username, String password) {
        this(username, password, 0);
    }

    public String getUsername() {
        return username;
    }

    public Wallet getWallet() {
        return wallet;
    }

    public boolean matchPasswords(String pass) {
        return password.equals(pass);
    }

    public void login() {
        isLoggedIn = true;
    }

    public void logout() {
        isLoggedIn = false;
    }

    public void buyCrypto(Asset asset, double amount) throws NotEnoughMoneyInWalletException, UnauthorizedException {
        isAuthorizedForTransactions();
        wallet.buyCrypto(asset, amount);
    }

    public double sellCrypto(Asset asset) throws CryptoCurrencyNotInWalletException, UnauthorizedException {
        isAuthorizedForTransactions();
        return wallet.sellCrypto(asset);
    }

    public void depositMoney(double money) throws UnauthorizedException {
        isAuthorizedForTransactions();
        wallet.deposit(money);
    }

    public String getWalletSummary() {
        return wallet.getWalletSummary();
    }

    public String getWalletOverallSummary(AssetsDatabase db) throws NoSuchAssetException, HttpException, URISyntaxException {
        return wallet.getWalletOverallSummary(db);
    }

    public void withdrawMoney(double money) throws NotEnoughMoneyInWalletException, UnauthorizedException {
        isAuthorizedForTransactions();
        wallet.withdraw(money);
    }

    public List<Transaction> getTransactions() {
        return wallet.getTransactions();
    }

    public boolean isAuthorizedForTransactions() throws UnauthorizedException {
        if (!isLoggedIn) {
            throw new UnauthorizedException("User not logged in!");
        }
        return true;
    }

}
