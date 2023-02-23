package bg.sofia.uni.fmi.mjt.cryptowallet.wallet.transaction;

public class DepositMoneyTransaction extends Transaction {
    private double money;

    public DepositMoneyTransaction(double m) {
        money = m;
    }

    @Override
    public String transactionString() {
        return "Deposited " + money + " USD.";
    }
}
