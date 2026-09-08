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

            // exit builtin
            if (commandName.equals("exit")) {

                break;
            }

            // echo builtin
            else if (commandName.equals("echo")) {

                for (int i = 1; i < parts.size(); i++) {

                    if (i > 1) {
                        System.out.print(" ");
                    }

                    System.out.print(parts.get(i));
                }

                System.out.println();
            }

            // type builtin
            else if (commandName.equals("type")) {

                if (parts.size() < 2) {
                    continue;
                }

                String argument = parts.get(1);

                // Check builtins
                if (argument.equals("exit")
                        || argument.equals("echo")
                        || argument.equals("type")
                        || argument.equals("pwd")
                        || argument.equals("cd")) {

                    System.out.println(argument + " is a shell builtin");

                } else {

                    // Get PATH
                    String path = System.getenv("PATH");

                    // Split PATH into directories
                    String[] directories = path.split(":");

                    boolean found = false;

                    // Search every directory
                    for (int i = 0; i < directories.length; i++) {

                        String directory = directories[i];

                        // Create full path
                        Path fullPath = Paths.get(directory, argument);

                        // Check if it exists AND is executable
                        if (Files.exists(fullPath)
                                && Files.isExecutable(fullPath)) {

                            System.out.println(argument + " is " + fullPath);

                            found = true;

                            break;
                        }
                    }

                    // If not found
                    if (!found) {

                        System.out.println(argument + ": not found");
                    }
                }
            }

            // pwd builtin
            else if (commandName.equals("pwd")) {

                System.out.println(currentDirectory);
            }

            // cd builtin
            else if (commandName.equals("cd")) {

                if (parts.size() < 2) {
                    continue;
                }

                String inputPath = parts.get(1);

                Path userPath = Paths.get(inputPath);

                Path destination;

                // cd ~
                if (inputPath.equals("~")) {

                    destination = Paths.get(
                            System.getProperty("user.home"));

                }

                // Absolute path
                else if (userPath.isAbsolute()) {

                    destination = userPath;

                }

                // Relative path
                else {

                    destination = currentDirectory.resolve(userPath).normalize();
                }

                // Check destination
                if (Files.exists(destination)&& Files.isDirectory(destination)) {

                    currentDirectory = destination;

                } else {

                    System.out.println("cd: " + inputPath + ": No such file or directory");
                }
            }
            // Unknown command
            else {

                String programName = parts.get(0);

                String path = System.getenv("PATH");

                String[] directories = path.split(":");

                boolean found = false;

                // Search PATH
                for (int i = 0; i < directories.length; i++) {

                    String directory = directories[i];

                    Path fullPath = Paths.get(directory, programName);

                    if (Files.exists(fullPath)&& Files.isExecutable(fullPath)) {

                        ProcessBuilder pb = new ProcessBuilder(parts);

                        pb.inheritIO();

                        Process process = pb.start();

                        process.waitFor();

                        found = true;

                        break;
                    }
                }

                // Command not found
                if (!found) {

                    System.out.println(
                            programName + ": command not found");
                }
            }
        }

        scanner.close();
    }
    // ==========================================
    // COMMAND PARSER
    // ==========================================

    static List<String> parseCommand(String command) {

        List<String> arguments = new ArrayList<>();

        String currentArgument = "";

        boolean insideQuote = false;

        for (int i = 0; i < command.length(); i++) {

            char c = command.charAt(i);

            // Single quote
            if (c == '\'') {

                insideQuote = !insideQuote;
            }

            // Space
            else if (c == ' ') {

                if (insideQuote) {

                    // Space inside quote is part of argument
                    currentArgument = currentArgument + " ";

                } else {

                    // Space outside quote separates arguments
                    if (!currentArgument.isEmpty()) {

                        arguments.add(currentArgument);

                        currentArgument = "";
                    }
                }
            }

            // Normal character
            else {

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