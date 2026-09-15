import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;
import java.io.PrintWriter;

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

                int endIndex;
                String outputFile = null;
                String errorOutputFile = null;

                // Find redirection operators
                int redirectIndex = parts.indexOf(">");
                int errorRedirectIndex = parts.indexOf("2>");
                int appendRedirectIndex = parts.indexOf(">>");

                // Get stdout filename
                if (appendRedirectIndex != -1) {

                    outputFile = parts.get(appendRedirectIndex + 1);

                } else if (redirectIndex != -1) {

                    outputFile = parts.get(redirectIndex + 1);
                }

                // Get stderr filename
                if (errorRedirectIndex != -1) {

                    errorOutputFile = parts.get(errorRedirectIndex + 1);
                }

                // Find where echo arguments should stop
                if (appendRedirectIndex != -1) {

                    endIndex = appendRedirectIndex;

                } else if (redirectIndex != -1) {

                    endIndex = redirectIndex;

                } else if (errorRedirectIndex != -1) {

                    endIndex = errorRedirectIndex;

                } else {

                    endIndex = parts.size();
                }

                PrintWriter writer = null;
                PrintWriter errorWriter = null;

                // Create stderr file
                if (errorOutputFile != null) {

                    errorWriter = new PrintWriter(errorOutputFile);
                }

                // Create stdout file
                if (outputFile != null) {

                    if (appendRedirectIndex != -1) {

                        writer = new PrintWriter(
                                new java.io.FileOutputStream(
                                        outputFile, true));

                    } else {

                        writer = new PrintWriter(outputFile);
                    }
                }

                // Print echo arguments
                for (int i = 1; i < endIndex; i++) {

                    if (i > 1) {

                        if (writer != null) {
                            writer.print(" ");
                        } else {
                            System.out.print(" ");
                        }
                    }

                    if (writer != null) {

                        writer.print(parts.get(i));

                    } else {

                        System.out.print(parts.get(i));
                    }
                }

                if (writer != null) {

                    writer.println();
                    writer.close();

                } else {

                    System.out.println();
                }

                if (errorWriter != null) {

                    errorWriter.close();
                }

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

                            System.out.println(
                                    argument + " is " + fullPath);

                            found = true;

                            break;
                        }
                    }

                    if (!found) {

                        System.out.println(
                                argument + ": not found");
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

                String outputFile = null;
                String errorOutputFile = null;

                // Find redirection operators
                int redirectIndex = parts.indexOf(">");
                int appendRedirectIndex = parts.indexOf(">>");
                int errorRedirectIndex = parts.indexOf("2>");
                int errorAppendRedirectIndex = parts.indexOf("2>>");

                boolean appendOutput =
                        appendRedirectIndex != -1;

                boolean appendError =
                        errorAppendRedirectIndex != -1;

                List<String> commandParts;

                // Handle stdout append
                if (appendRedirectIndex != -1) {

                    outputFile =
                            parts.get(appendRedirectIndex + 1);

                    commandParts = new ArrayList<>(
                            parts.subList(0, appendRedirectIndex));

                // Handle stdout overwrite
                } else if (redirectIndex != -1) {

                    outputFile =
                            parts.get(redirectIndex + 1);

                    commandParts = new ArrayList<>(
                            parts.subList(0, redirectIndex));

                // Handle stderr append
                } else if (errorAppendRedirectIndex != -1) {

                    errorOutputFile =
                            parts.get(errorAppendRedirectIndex + 1);

                    commandParts = new ArrayList<>(
                            parts.subList(0, errorAppendRedirectIndex));

                // Handle stderr overwrite
                } else if (errorRedirectIndex != -1) {

                    errorOutputFile =
                            parts.get(errorRedirectIndex + 1);

                    commandParts = new ArrayList<>(
                            parts.subList(0, errorRedirectIndex));

                } else {

                    commandParts = parts;
                }

                String path = System.getenv("PATH");

                String[] directories = path.split(":");

                boolean found = false;

                for (int i = 0; i < directories.length; i++) {

                    String directory = directories[i];

                    Path fullPath =
                            Paths.get(directory, programName);

                    if (Files.exists(fullPath)
                            && Files.isExecutable(fullPath)) {

                        ProcessBuilder pb =
                                new ProcessBuilder(commandParts);

                        // Configure stdout
                        if (outputFile != null) {

                            if (appendOutput) {

                                pb.redirectOutput(
                                        ProcessBuilder.Redirect.appendTo(
                                                new java.io.File(outputFile)));

                            } else {

                                pb.redirectOutput(
                                        new java.io.File(outputFile));
                            }

                        // Configure stderr
                        } else if (errorOutputFile != null) {

                            if (appendError) {

                                pb.redirectError(
                                        ProcessBuilder.Redirect.appendTo(
                                                new java.io.File(
                                                        errorOutputFile)));

                            } else {

                                pb.redirectError(
                                        new java.io.File(
                                                errorOutputFile));
                            }

                        } else {

                            pb.inheritIO();
                        }

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

        boolean insideSingleQuote = false;
        boolean insideDoubleQuote = false;

        for (int i = 0; i < command.length(); i++) {

            char c = command.charAt(i);

            if (c == '\\' && insideSingleQuote == false) {

                if (insideDoubleQuote) {

                    if (i + 1 < command.length()) {

                        if (command.charAt(i + 1) == '"'
                                || command.charAt(i + 1) == '\\') {

                            currentArgument =
                                    currentArgument
                                            + command.charAt(i + 1);

                            i++;

                        } else {

                            currentArgument =
                                    currentArgument + c;
                        }

                    } else {

                        currentArgument =
                                currentArgument + c;
                    }

                } else {

                    if (i + 1 < command.length()) {

                        currentArgument =
                                currentArgument
                                        + command.charAt(i + 1);

                        i++;

                    } else {

                        currentArgument =
                                currentArgument + c;
                    }
                }

            } else if (c == '\'') {

                if (insideDoubleQuote) {

                    currentArgument =
                            currentArgument + c;

                } else {

                    insideSingleQuote =
                            !insideSingleQuote;
                }

            } else if (c == '"') {

                if (insideSingleQuote) {

                    currentArgument =
                            currentArgument + c;

                } else {

                    insideDoubleQuote =
                            !insideDoubleQuote;
                }

            } else if (c == ' ' || c == '\t') {

                if (insideSingleQuote
                        || insideDoubleQuote) {

                    currentArgument =
                            currentArgument + c;

                } else {

                    if (!currentArgument.isEmpty()) {

                        arguments.add(currentArgument);

                        currentArgument = "";
                    }
                }

            } else {

                currentArgument =
                        currentArgument + c;
            }
        }

        if (!currentArgument.isEmpty()) {

            arguments.add(currentArgument);
        }

        return arguments;
    }
}