import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;

public class Main {

    public static void main(String[] args) throws Exception {

        Scanner scanner = new Scanner(System.in);

        Path currentDirectory = Paths.get(System.getProperty("user.dir"));

        while (true) {

            System.out.print("$ ");

            String command = scanner.nextLine();

            List<String> parts = parseCommand(command);

            if (parts.isEmpty()) {
                continue;
            }

            String commandName = parts.get(0);

            if (commandName.equals("exit")) {

                break;

            } else if (commandName.equals("echo")) {

                for (int i = 1; i < parts.size(); i++) {

                    if (i > 1) {
                        System.out.print(" ");
                    }

                    System.out.print(parts.get(i));
                }

                System.out.println();

            } else if (commandName.equals("type")) {

                if (parts.size() < 2) {
                    continue;
                }

                String argument = parts.get(1);

                if (argument.equals("exit")
                        || argument.equals("echo")
                        || argument.equals("type")
                        || argument.equals("pwd")
                        || argument.equals("cd")) {

                    System.out.println(argument + " is a shell builtin");

                } else {

                    String path = System.getenv("PATH");

                    String[] directories = path.split(":");

                    boolean found = false;

                    for (int i = 0; i < directories.length; i++) {

                        String directory = directories[i];

                        Path fullPath = Paths.get(directory, argument);

                        if (Files.exists(fullPath)
                                && Files.isExecutable(fullPath)) {

                            System.out.println(argument + " is " + fullPath);

                            found = true;

                            break;
                        }
                    }

                    if (!found) {

                        System.out.println(argument + ": not found");
                    }
                }

            } else if (commandName.equals("pwd")) {

                System.out.println(currentDirectory);

            } else if (commandName.equals("cd")) {

                if (parts.size() < 2) {
                    continue;
                }

                String inputPath = parts.get(1);

                Path userPath = Paths.get(inputPath);

                Path destination;

                if (inputPath.equals("~")) {

                    destination = Paths.get(
                            System.getProperty("user.home"));

                } else if (userPath.isAbsolute()) {

                    destination = userPath;

                } else {

                    destination = currentDirectory
                            .resolve(userPath)
                            .normalize();
                }

                if (Files.exists(destination)
                        && Files.isDirectory(destination)) {

                    currentDirectory = destination;

                } else {

                    System.out.println(
                            "cd: " + inputPath
                                    + ": No such file or directory");
                }

            } else {

                String programName = parts.get(0);

                String path = System.getenv("PATH");

                String[] directories = path.split(":");

                boolean found = false;

                for (int i = 0; i < directories.length; i++) {

                    String directory = directories[i];

                    Path fullPath = Paths.get(directory, programName);

                    if (Files.exists(fullPath)
                            && Files.isExecutable(fullPath)) {

                        ProcessBuilder pb = new ProcessBuilder(parts);

                        pb.inheritIO();

                        Process process = pb.start();

                        process.waitFor();

                        found = true;

                        break;
                    }
                }

                if (!found) {

                    System.out.println(
                            programName + ": command not found");
                }
            }
        }

        scanner.close();
    }

    static List<String> parseCommand(String command) {

        // Store final arguments
        List<String> arguments = new ArrayList<>();

        // Build current argument
        String currentArgument = "";

        // Track quote states
        boolean insideSingleQuote = false;
        boolean insideDoubleQuote = false;

        // Process each character
        for (int i = 0; i < command.length(); i++) {

            char c = command.charAt(i);

            // Handle escape character
            if (c == '\\' && insideSingleQuote == false) {

                if (insideDoubleQuote) {

                    if (i + 1 < command.length()) {

                        if (command.charAt(i + 1) == '"' || command.charAt(i + 1) == '\\') {
                            // Add escaped character
                            currentArgument = currentArgument + command.charAt(i + 1);
                            i++;

                        }

                        else {
                            currentArgument = currentArgument + c;
                        }

                    } else {

                        // Add trailing backslash
                        currentArgument = currentArgument + c;
                    }
                } else {

                    if (i + 1 < command.length()) {

                        currentArgument = currentArgument + command.charAt(i + 1);
                        i++;

                    } else {
                        currentArgument = currentArgument + c;
                    }

                }
            }

            // Handle single quote
            else if (c == '\'')

            {

                if (insideDoubleQuote) {

                    // Treat as normal character
                    currentArgument = currentArgument + c;

                } else {

                    // Toggle single quote state
                    insideSingleQuote = !insideSingleQuote;
                }

                // Handle double quote
            } else if (c == '"') {

                if (insideSingleQuote) {
                    // Treat as normal character
                    currentArgument = currentArgument + c;

                } else {
                    // Toggle double quote state
                    insideDoubleQuote = !insideDoubleQuote;
                }

                // Handle space or tab
            } else if (c == ' ' || c == '\t') {

                if (insideSingleQuote || insideDoubleQuote) {

                    // Keep space inside quotes
                    currentArgument = currentArgument + c;

                }

                else {

                    // End current argument
                    if (!currentArgument.isEmpty()) {
                        arguments.add(currentArgument);
                        currentArgument = "";
                    }
                }

            }

            else {

                // Add normal character
                currentArgument = currentArgument + c;
            }
        }

        // Add last argument
        if (!currentArgument.isEmpty()) {
            arguments.add(currentArgument);
        }

        return arguments;
    }
}