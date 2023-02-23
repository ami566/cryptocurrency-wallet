package bg.sofia.uni.fmi.mjt.cryptowallet.command;

import java.util.ArrayList;
import java.util.List;

public class CommandCreator {
    private static List<String> getCommandArguments(String input) {
        List<String> commands = new ArrayList<>();
        StringBuilder sb = new StringBuilder();

        boolean insideQuote = false;

        for (char c : input.toCharArray()) {
            if (c == '"') {
                insideQuote = !insideQuote;
            }
            if (c == ' ' && !insideQuote) {
                commands.add(sb.toString().replace("\"", ""));
                sb.delete(0, sb.length());
            } else {
                sb.append(c);
            }
        }

        commands.add(sb.toString().replace("\"", ""));

        return commands;
    }

    public static Command newCommand(String input) {
        List<String> commands = CommandCreator.getCommandArguments(input);
        String[] args = commands.subList(1, commands.size()).toArray(new String[0]);

        CommandType commandType = CommandType.valueOfCommand(commands.get(0));
        return new Command(commandType, args);
    }
}
