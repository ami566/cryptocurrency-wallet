package bg.sofia.uni.fmi.mjt.cryptowallet.wallet;

import bg.sofia.uni.fmi.mjt.cryptowallet.assets.Asset;
import bg.sofia.uni.fmi.mjt.cryptowallet.assets.AssetsDatabase;
import bg.sofia.uni.fmi.mjt.cryptowallet.exceptions.CryptoCurrencyNotInWalletException;
import bg.sofia.uni.fmi.mjt.cryptowallet.exceptions.HttpException;
import bg.sofia.uni.fmi.mjt.cryptowallet.exceptions.NoSuchAssetException;
import bg.sofia.uni.fmi.mjt.cryptowallet.exceptions.NotEnoughMoneyInWalletException;
import bg.sofia.uni.fmi.mjt.cryptowallet.wallet.transaction.BoughtCryptoTransaction;
import bg.sofia.uni.fmi.mjt.cryptowallet.wallet.transaction.DepositMoneyTransaction;
import bg.sofia.uni.fmi.mjt.cryptowallet.wallet.transaction.SoldCryptoTransaction;
import bg.sofia.uni.fmi.mjt.cryptowallet.wallet.transaction.Transaction;

import java.io.Serializable;
import java.net.URISyntaxException;
import java.util.*;

public class Wallet implements Serializable {
    private double moneyInAccount;
    private final Map<Asset, Double> moneyInCrypto;
    private final Map<Asset, Double> spentMoney;
    private final List<Transaction> transactions;


    public Wallet(double money) {
        //  this.db = db;
        moneyInCrypto = new HashMap<>();
        spentMoney = new HashMap<>();
        transactions = new ArrayList<>();
        moneyInAccount = money;
    }

    public synchronized void deposit(double money) {
        checkForInvalidAmountOfMoney(money);

        moneyInAccount += money;
        transactions.add(new DepositMoneyTransaction(money));
        // have to update the files ?
    }

    public synchronized void buyCrypto(Asset asset, double amount) throws NotEnoughMoneyInWalletException {
        checkForInvalidAmountOfMoney(amount);

        if (amount > moneyInAccount) {
            throw new NotEnoughMoneyInWalletException("There's not enough money in your wallet. Sum available: " + amount);
        }

        double cryptoBought = amount / asset.priceUsd();
        double moneySpend = amount;
        moneyInAccount -= cryptoBought * asset.priceUsd();
        transactions.add(new BoughtCryptoTransaction(amount, cryptoBought, asset.assetId(), asset.priceUsd()));

        if (checkIfHasCertainCryptoInWallet(asset)) {
            cryptoBought += moneyInCrypto.get(asset);
            moneySpend += spentMoney.get(asset);
        }
        moneyInCrypto.put(asset, cryptoBought);
        spentMoney.put(asset, moneySpend);
    }

    public synchronized double sellCrypto(Asset asset) throws CryptoCurrencyNotInWalletException {

        if (!checkIfHasCertainCryptoInWallet(asset)) {
            throw new CryptoCurrencyNotInWalletException("There aren't money in that currency in your wallet.");
        }

        double cryptoSold = asset.priceUsd() * moneyInCrypto.get(asset);
        transactions.add(new SoldCryptoTransaction(cryptoSold, moneyInCrypto.get(asset), asset.assetId(), asset.priceUsd()));
        moneyInCrypto.remove(asset);
        spentMoney.remove(asset);
        moneyInAccount += cryptoSold;
        return cryptoSold;
    }

    public String getWalletSummary() {
        StringBuilder result = new StringBuilder();
        result.append(String.format("Wallet balance: %.02f\n", moneyInAccount));
        for (Transaction transaction : transactions) {
            result.append(transaction.transactionString());
            result.append(System.lineSeparator());
        }
        return result.toString().trim();
    }

    public String getWalletOverallSummary(AssetsDatabase db) throws NoSuchAssetException, HttpException, URISyntaxException {
        StringBuilder result = new StringBuilder();

        for (Map.Entry<Asset, Double> entrySet : spentMoney.entrySet()) {
            double moneySpend = entrySet.getValue();
            double numOfCoins = moneyInCrypto.get(entrySet.getKey());
            Asset asset = db.getAssetById(entrySet.getKey().assetId());
            double currentPrice = numOfCoins * asset.priceUsd();
            double difference = moneySpend - currentPrice;
            double gained = 0.0;
            double lost = 0.0;

            if (difference <= 0) {
                gained = Math.abs(difference);
            } else {
                lost = difference;
            }

            result.append(String.format("""
                    %s {
                        buyValue: '%.02f',
                        sellValue: '%.02f',
                        gained: '%.02f',
                        lost: '%.02f'
                    }""", asset.name(), moneySpend, currentPrice, gained, lost));
            result.append("\n");
        }

        if (result.isEmpty()) {
            return "There is no info";
        }

        return result.toString().trim();
    }

    public void withdraw(double money) throws NotEnoughMoneyInWalletException {
        checkForInvalidAmountOfMoney(money);

        if (money > moneyInAccount) {
            throw new NotEnoughMoneyInWalletException("There's not enough money in your wallet. Sum available: " + money);
        }
        moneyInAccount -= money;
    }

    private boolean checkIfHasCertainCryptoInWallet(Asset a) {
        return moneyInCrypto.containsKey(a);
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public double getMoneyInAccount() {
        return moneyInAccount;
    }

    private void checkForInvalidAmountOfMoney(double money) {
        if (money <= 0) {
            throw new IllegalArgumentException("Money amount cannot be zero or negative ");
        }
    }

    public Map<Asset, Double> getSpentMoney() {
        return Collections.unmodifiableMap(spentMoney);
    }
}
