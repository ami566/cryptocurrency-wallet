package bg.sofia.uni.fmi.mjt.cryptowallet;

import bg.sofia.uni.fmi.mjt.cryptowallet.command.Command;
import bg.sofia.uni.fmi.mjt.cryptowallet.command.CommandCreator;
import bg.sofia.uni.fmi.mjt.cryptowallet.command.CommandExecutor;
import bg.sofia.uni.fmi.mjt.cryptowallet.command.CommandType;
import bg.sofia.uni.fmi.mjt.cryptowallet.exceptions.*;
import bg.sofia.uni.fmi.mjt.cryptowallet.logger.ErrorLogger;
import bg.sofia.uni.fmi.mjt.cryptowallet.users.User;
import bg.sofia.uni.fmi.mjt.cryptowallet.users.UsersDatabase;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.concurrent.CompletionException;

public class Server {
    private static final String USERS_FILE = "resources/users.txt";
    private static final int SERVER_PORT = 6666;
    private static final int BUFFER_SIZE = 10000;
    private static final String HOST = "localhost";
    private final UsersDatabase usersDb;
    private final CommandExecutor commandExecutor;

    private final int port;
    private boolean isWorking = true;

    private final ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
    private Selector selector;

    public Server(int port, CommandExecutor cmdExecutor, UsersDatabase users) {
        this.port = port;
        commandExecutor = cmdExecutor;
        usersDb = users;
    }

    public void start() {
        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {
            selector = Selector.open();
            configureServerSocketChannel(serverSocketChannel, selector);
            while (isWorking) {
                try {
                    int readyChannels = selector.select();
                    if (readyChannels == 0) {
                        continue;
                    }

                    Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
                    while (keyIterator.hasNext()) {
                        SelectionKey key = keyIterator.next();

                        if (key.isReadable()) {
                            SocketChannel clientChannel = (SocketChannel) key.channel();
                            String clientInput = getClientInput(clientChannel);
                            if (clientInput == null) {
                                clientChannel.close();
                                keyIterator.remove();
                                continue;
                            }

                            try {
                                executeCommand(key, clientChannel, clientInput);
                            } catch (UserAlreadyExistsException | HttpException | IllegalArgumentException |
                                     URISyntaxException e) {
                                writeOutput(clientChannel, e.getMessage());
                            } catch (CompletionException e) {
                                ErrorLogger.logException(e);
                                writeOutput(clientChannel,
                                        "Please check your internet connection and try again");
                            } catch (Exception e) {
                                ErrorLogger.logException(e);
                                writeOutput(clientChannel, "Something went wrong... Please try again later");
                            }
                        } else if (key.isAcceptable()) {
                            accept(selector, key);
                        }

                        keyIterator.remove();
                    }
                } catch (IOException e) {
                    ErrorLogger.logException(e);
                    System.out.println("Error occurred while processing client request: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            ErrorLogger.logException(e);
        }
    }

    public void stop() {
        isWorking = false;
        if (selector.isOpen()) {
            selector.wakeup();
        }
    }

    private void configureServerSocketChannel(ServerSocketChannel channel, Selector selector) throws IOException {
        channel.bind(new InetSocketAddress(HOST, this.port));
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_ACCEPT);
    }

    private String getClientInput(SocketChannel clientChannel) throws IOException {
        buffer.clear();

        int readBytes = clientChannel.read(buffer);
        if (readBytes < 0) {
            clientChannel.close();
            return null;
        }

        buffer.flip();

        byte[] clientInputBytes = new byte[buffer.remaining()];
        buffer.get(clientInputBytes);

        return new String(clientInputBytes, StandardCharsets.UTF_8);
    }

    private void executeCommand(SelectionKey key, SocketChannel channel, String input) throws IOException, HttpException, URISyntaxException,
            NoSuchUserException, UserAlreadyExistsException, NoSuchAssetException, NotEnoughMoneyInWalletException, CryptoCurrencyNotInWalletException {
        Command command = CommandCreator.newCommand(input);
        CommandType commandType = command.command();

        String output = switch (commandType) {
            case LOGIN, REGISTER: {
                String cmdResult = commandExecutor.execute(command);
                User user = usersDb.getUserByUsername(command.arguments()[0]);
                key.attach(user);
                yield cmdResult;
            }
            case LIST_CRYPTO, HELP: {
                yield commandExecutor.execute(command);
            }
            default: {
                if (key.attachment() == null) {
                    yield "Unregistered user cannot execute this command";
                }

                User user = (User) key.attachment();
                String cmdResult = commandExecutor.execute(command, user);
                User newUser = usersDb.getUserByUsername(user.getUsername());
                key.attach(newUser);
                yield cmdResult;
            }
        };
        writeOutput(channel, output);
    }

    private void writeOutput(SocketChannel channel, String output) throws IOException {
        buffer.clear();
        buffer.put(output.getBytes());
        buffer.flip();

        channel.write(buffer);
    }

    private void accept(Selector selector, SelectionKey key) throws IOException {
        ServerSocketChannel sockChannel = (ServerSocketChannel) key.channel();
        SocketChannel accept = sockChannel.accept();

        accept.configureBlocking(false);
        accept.register(selector, SelectionKey.OP_READ);
    }
}
