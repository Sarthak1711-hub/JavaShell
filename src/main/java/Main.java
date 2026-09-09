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

        List<String> arguments = new ArrayList<>();

        String currentArgument = "";
        String result = "";

        boolean insideSingleQuote = false;
        boolean insideDoubleQuote = false;

        for (int i = 0; i < command.length(); i++) {

            char c = command.charAt(i);

            if (c == '\\') {
                if (i + 1 < command.length()) {
                    currentArgument = currentArgument + command.charAt(i + 1);
                    i++;
                }

            }

            else if (c == '\'') {

                if (insideDoubleQuote) {

                    currentArgument = currentArgument + c;

                } else {

                    insideSingleQuote = !insideSingleQuote;
                }

            } else if (c == '"') {

                if (insideSingleQuote) {

                    currentArgument = currentArgument + c;

                } else {

                    insideDoubleQuote = !insideDoubleQuote;
                }

            } else if (c == ' ' || c == '\t') {

                if (insideSingleQuote || insideDoubleQuote) {

                    currentArgument = currentArgument + c;

                } else {

                    if (!currentArgument.isEmpty()) {

                        arguments.add(currentArgument);

                        currentArgument = "";
                    }
                }

            } else {

                currentArgument = currentArgument + c;
            }
        }

        if (!currentArgument.isEmpty()) {

            arguments.add(currentArgument);
        }

        return arguments;
    }
}