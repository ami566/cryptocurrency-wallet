package bg.sofia.uni.fmi.mjt.cryptowallet.command;

import bg.sofia.uni.fmi.mjt.cryptowallet.assets.Asset;
import bg.sofia.uni.fmi.mjt.cryptowallet.assets.AssetsDatabase;
import bg.sofia.uni.fmi.mjt.cryptowallet.exceptions.*;
import bg.sofia.uni.fmi.mjt.cryptowallet.users.User;
import bg.sofia.uni.fmi.mjt.cryptowallet.users.UsersDatabase;

import java.net.URISyntaxException;
import java.util.Collection;

public class CommandExecutor {
    private static final String UNKNOWN_COMMAND = "Unknown command";
    private static final String OFFERING = "--offering";
    private static final String MONEY = "--money";
    private static final String SEPARATOR = "=";
    private static final int TWO = 2;

    private AssetsDatabase assets;
    private UsersDatabase users;

    public CommandExecutor(AssetsDatabase assetsDb, UsersDatabase usersDb) {
        assets = assetsDb;
        users = usersDb;
    }

    public String execute(Command command) throws HttpException, URISyntaxException, UserAlreadyExistsException, NoSuchUserException {
        return switch (command.command()) {
            case LOGIN -> login(command);
            case REGISTER -> register(command);
            case LIST_CRYPTO -> listCrypto();
            case HELP -> help();
            default -> UNKNOWN_COMMAND;
        };
    }

    public String execute(Command command, User user) throws HttpException, URISyntaxException, NotEnoughMoneyInWalletException, NoSuchAssetException, CryptoCurrencyNotInWalletException, NoSuchUserException {
        return switch (command.command()) {
            case LOGOUT -> logout(user);
            case DEPOSIT -> deposit(command, user);
            case WITHDRAW -> withdraw(command, user);
            case BUY_CRYPTO -> buyCrypto(command, user);
            case SELL_CRYPTO -> sellCrypto(command, user);
            case WALLET_SUMMARY -> getWalletSummary(user);
            case WALLET_OVERALL_SUMMARY -> getWalletOverallSummary(user);
            default -> UNKNOWN_COMMAND;
        };
    }

    private String help() {
        return """
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
    }

    private String login(Command cmd) throws NoSuchUserException, UnauthorizedException {
        String[] args = cmd.arguments();
        if (args.length != TWO) {
            throw new IllegalArgumentException("You need two arguments to log in!");
        }

        users.login(args[0], args[1]);
        return "Logged in successfully as " + args[0];
    }

    private String register(Command cmd) throws UserAlreadyExistsException {
        String[] args = cmd.arguments();
        if (args.length != TWO) {
            throw new IllegalArgumentException("You need two arguments to register");
        }
        users.register(args[0], args[1]);
        return "Registered successfully! Welcome " + args[0];
    }

    private String listCrypto() throws HttpException, URISyntaxException {
        StringBuilder sb = new StringBuilder();
        Collection<Asset> list = assets.getAllAssets().values();
        for (Asset asset : list) {
            sb.append(asset.toString());
        }
        return sb.toString().trim();
    }

    private String logout(User user) {
        users.logout(user);
        return "Logged out successfully";
    }

    private String deposit(Command cmd, User user) throws UnauthorizedException, NoSuchUserException {
        String[] args = cmd.arguments();
        if (args.length != 1) {
            throw new IllegalArgumentException("Invalid arguments for deposit command");
        }

        double money = parseMoneyInput(args[0]);
        users.deposit(user, money);
        return money + " USD were deposited to your account";
    }

    private String withdraw(Command cmd, User user) throws NotEnoughMoneyInWalletException, UnauthorizedException, NoSuchUserException {
        String[] args = cmd.arguments();
        if (args.length != 1) {
            throw new IllegalArgumentException("Invalid arguments for withdraw command");
        }

        double money = parseMoneyInput(args[0]);
        users.withdraw(user, money);
        return money + " USD were withdrew from your account";
    }

    private String sellCrypto(Command cmd, User user) throws NoSuchAssetException, HttpException,
            URISyntaxException, CryptoCurrencyNotInWalletException, NoSuchUserException {
        String[] args = cmd.arguments();
        if (args.length != 1) {
            throw new IllegalArgumentException("Invalid arguments for sell command");
        }

        String assetId = getAssetId(args[0]);
        Asset asset = assets.getAssetById(assetId);
        users.sellCrypto(user, asset);
        return assetId + " was successfully sold";
    }

    private String buyCrypto(Command cmd, User user) throws NotEnoughMoneyInWalletException, NoSuchAssetException, HttpException, URISyntaxException, NoSuchUserException {
        String[] args = cmd.arguments();
        if (args.length != TWO) {
            throw new IllegalArgumentException("Invalid arguments to buy");
        }

        String assetId = getAssetId(args[0]);
        Asset asset = assets.getAssetById(assetId);

        String[] moneyArgument = args[1].split(SEPARATOR);
        if (moneyArgument.length != TWO) {
            throw new IllegalArgumentException("Illegal format of money argument");
        }

        if (!MONEY.equals(moneyArgument[0])) {
            throw new IllegalArgumentException("Money argument was not given");
        }

        double money = parseMoneyInput(moneyArgument[1]);
        users.buyCrypto(user, asset, money);

        return assetId + " for " + money + " was successfully bought";
    }

    public String getWalletSummary(User user) {
        return user.getWalletSummary();
    }

    public String getWalletOverallSummary(User user) throws NoSuchAssetException, HttpException, URISyntaxException {
        return user.getWalletOverallSummary(assets);
    }

    private String getAssetId(String offeringCode) {
        String[] offeringCodeArgument = offeringCode.split(SEPARATOR);
        if (offeringCodeArgument.length != TWO) {
            throw new IllegalArgumentException("Invalid format of offering code");
        }
        if (!OFFERING.equals(offeringCodeArgument[0])) {
            throw new IllegalArgumentException("Offering code was not passed");
        }

        return offeringCodeArgument[1];
    }

    private double parseMoneyInput(String s) {
        double money = 0.0;
        try {
            money = Double.parseDouble(s);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Money not in the correct format");
        }
        return money;
    }
}
