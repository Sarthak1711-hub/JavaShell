import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;

public class Shell {

    public void run() throws Exception {

        Path currentDirectory = Paths.get(System.getProperty("user.dir"));

        CommandCompleter completer = new CommandCompleter(currentDirectory);

        LineReader reader = LineReaderBuilder.builder().completer(completer).option(LineReader.Option.AUTO_LIST, false).build();

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

                currentDirectory = BuiltinCommands.cd(parts,currentDirectory,completer);

            } else {

                ExternalCommandExecutor.execute(parts);
            }
        }
    }
}