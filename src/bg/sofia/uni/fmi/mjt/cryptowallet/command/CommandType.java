package bg.sofia.uni.fmi.mjt.cryptowallet.command;

public enum CommandType {
    LOGIN("login"),
    LOGOUT("logout"),
    REGISTER("register"),
    DEPOSIT("deposit-money"),
    WITHDRAW("withdraw-money"),
    BUY_CRYPTO("buy"),
    SELL_CRYPTO("sell"),
    LIST_CRYPTO("list-offerings"),
    WALLET_SUMMARY("get-wallet-summary"),
    WALLET_OVERALL_SUMMARY("get-wallet-overall-summary"),
    HELP("help"),
    UNKNOWN("");

    public final String name;

    public static CommandType valueOfCommand(String name) {
        for (CommandType commandType : values()) {
            if (commandType.name.equals(name)) {
                return commandType;
            }
        }
        return UNKNOWN;
    }

    CommandType(String name) {
        this.name = name;
    }
}

