import java.util.ArrayList;
import java.util.List;

public class CommandParser {

    public static List<String> parseCommand(String command) {

        List<String> arguments = new ArrayList<>();

        String currentArgument = "";

        boolean insideSingleQuote = false;
        boolean insideDoubleQuote = false;

        for (int i = 0; i < command.length(); i++) {

            char c = command.charAt(i);

            if (c == '\\' && !insideSingleQuote) {

                if (insideDoubleQuote) {

                    if (i + 1 < command.length()) {

                        if (command.charAt(i + 1) == '"'
                                || command.charAt(i + 1) == '\\') {

                            currentArgument += command.charAt(i + 1);
                            i++;

                        } else {

                            currentArgument += c;
                        }

                    } else {

                        currentArgument += c;
                    }

                } else {

                    if (i + 1 < command.length()) {

                        currentArgument += command.charAt(i + 1);
                        i++;

                    } else {

                        currentArgument += c;
                    }
                }

            } else if (c == '\'') {

                if (insideDoubleQuote) {
                    currentArgument += c;

                } else {
                    insideSingleQuote = !insideSingleQuote;
                }

            } else if (c == '"') {

                if (insideSingleQuote) {
                    currentArgument += c;

                } else {
                    insideDoubleQuote = !insideDoubleQuote;
                }

            } else if (c == ' ' || c == '\t') {

                if (insideSingleQuote || insideDoubleQuote) {
                    currentArgument += c;

                } else {

                    if (!currentArgument.isEmpty()) {
                        arguments.add(currentArgument);
                        currentArgument = "";
                    }
                }

            } else {

                currentArgument += c;
            }
        }

        if (!currentArgument.isEmpty()) {
            arguments.add(currentArgument);
        }

        return arguments;
    }
}