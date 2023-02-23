import bg.sofia.uni.fmi.mjt.cryptowallet.Server;
import bg.sofia.uni.fmi.mjt.cryptowallet.assets.AssetsDatabase;
import bg.sofia.uni.fmi.mjt.cryptowallet.command.CommandExecutor;
import bg.sofia.uni.fmi.mjt.cryptowallet.restServer.ServerRequest;
import bg.sofia.uni.fmi.mjt.cryptowallet.users.UsersDatabase;

import java.io.IOException;
import java.nio.file.Path;

public class Main {
    private static final String USERS_FILE = "resources/users.txt";
    private static final int SERVER_PORT = 6666;

    public static void main(String[] args) {
       /* Map<String, User> m = new HashMap<>();
        User u = new User("ami566", "amiraemin66", 2000);
        m.put("ami566", u);
        try (var outputStream = new ObjectOutputStream(Files.newOutputStream(Path.of(USERS_FILE)))) {
            outputStream.writeObject(m);
        } catch (IOException e) {
            System.out.println("Problem occurred while writing on file");
            ////
        }
        */
        ServerRequest requestToApi = new ServerRequest();
        try {
            UsersDatabase usersDb = new UsersDatabase(Path.of(USERS_FILE));
            AssetsDatabase assetsDb = new AssetsDatabase(requestToApi);

            CommandExecutor commandExecutor = new CommandExecutor(assetsDb, usersDb);
            Server server = new Server(SERVER_PORT, commandExecutor, usersDb);
            server.start();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error occurred while starting server: " + e.getMessage());
        }

    }
}