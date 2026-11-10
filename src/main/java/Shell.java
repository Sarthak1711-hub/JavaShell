import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import java.util.HashMap;

public class Shell {

    public void run() throws Exception {

        Path currentDirectory = Paths.get(System.getProperty("user.dir"));

        CommandCompleter completer = new CommandCompleter(currentDirectory); // Provide command completion when the TAB
                                                                             // key is pressed.

        LineReader reader = LineReaderBuilder.builder().completer(completer).option(LineReader.Option.AUTO_LIST, false)
                .build();

        HashMap<String, String> completions = new HashMap<>();

        while (true) {

            String command = reader.readLine("$ ");

            List<String> parts = CommandParser.parseCommand(command);

            if (parts.isEmpty()) {
                continue;
            }

            String commandName = parts.get(0);

            if (BuiltinCommands.exit(commandName)) {

                break;

            } else if (commandName.equals("echo")) {

                BuiltinCommands.echo(parts);

            } else if (commandName.equals("type")) {

                if (parts.size() < 2) {
                    continue;
                }

                BuiltinCommands.type(parts.get(1));

            } else if (commandName.equals("pwd")) {

                BuiltinCommands.pwd(currentDirectory);

            } else if (commandName.equals("cd")) {

                currentDirectory = BuiltinCommands.cd(parts, currentDirectory, completer);

            } else if (commandName.equals("complete")) {

                if (parts.size() < 2) {
                    continue;
                }

                if (parts.get(1).equals("-C")) {

                    if (parts.size() < 4) {
                        continue;
                    }

                    String scriptPath = parts.get(2);
                    String targetCommand = parts.get(3);

                    completions.put(targetCommand, scriptPath);

                } else if (parts.get(1).equals("-p")) {

                    if (parts.size() < 3) {
                        continue;
                    }

                    String argument = parts.get(2);

                    if (completions.containsKey(argument)) {

                        String scriptPath = completions.get(argument);

                        System.out.println("complete -C '" + scriptPath + "' " + argument);

                    } else {

                        System.out.println("complete: " + argument + ": no completion specification");
                    }
                }
            } else {

                ExternalCommandExecutor.execute(parts);
            }
        }
    }
}