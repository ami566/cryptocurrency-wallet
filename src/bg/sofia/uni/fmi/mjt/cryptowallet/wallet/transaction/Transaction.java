package bg.sofia.uni.fmi.mjt.cryptowallet.wallet.transaction;

import java.io.Serializable;

public abstract class Transaction implements Serializable {
    public abstract String transactionString();
}
