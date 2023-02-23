package bg.sofia.uni.fmi.mjt.cryptowallet;

import bg.sofia.uni.fmi.mjt.cryptowallet.logger.ErrorLogger;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

public class Client {
    private static final int SERVER_PORT = 6666;
    private static final String HOST = "localhost";
    private static final String EXIT = "exit";
    private static final int BUFFER_SIZE = 10000;
    private static ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);

    public static void main(String[] args) {
        try (SocketChannel socketChannel = SocketChannel.open();
             Scanner scanner = new Scanner(System.in)) {
            socketChannel.connect(new InetSocketAddress(HOST, SERVER_PORT));

            while (true) {
                String message = scanner.nextLine(); // read a line from the console

                if (EXIT.equals(message)) {
                    break;
                }

                if (message != null && !message.isEmpty()) {
                    writeMessageToChannel(message, socketChannel);
                }

                String reply = getResponse(socketChannel); // buffer drain
                System.out.println(reply);
            }
        } catch (ConnectException e) {
            System.out.println("Something went wrong on our end. " +
                    "Try again later or contact administrator by providing the logs in resources/errors.txt");
            ErrorLogger.logException(e);
        } catch (IOException e) {
            System.out.println("There is a problem with the network communication");
            e.printStackTrace();
            ErrorLogger.logException(e);
        }
    }

    private static String getResponse(SocketChannel socketChannel) throws IOException {
        buffer.clear();
        socketChannel.read(buffer);
        buffer.flip();

        byte[] byteArray = new byte[buffer.remaining()];
        buffer.get(byteArray);
        return new String(byteArray, "UTF-8");
    }

    private static void writeMessageToChannel(String message, SocketChannel socketChannel) throws IOException {
        buffer.clear();  // switch to writing mode
        buffer.put(message.getBytes());
        buffer.flip();  // switch to reading mode
        socketChannel.write(buffer);
    }
}
