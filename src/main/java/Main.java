import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws Exception {

        Scanner scanner = new Scanner(System.in);

        while (true) {

            System.out.print("$ ");

            String command = scanner.nextLine();

            // exit builtin
            if (command.equals("exit")) {

                break;

            }

            // echo builtin
            else if (command.startsWith("echo ")) {

                System.out.println(command.substring(5));

            }

            // type builtin
            else if (command.startsWith("type ")) {

                String[] parts = command.split(" ");

                String argument = parts[1];

                // Check builtins
                if (argument.equals("exit") ||
                    argument.equals("echo") ||
                    argument.equals("type")) {

                    System.out.println(argument + " is a shell builtin");

                } else {

                    String path = System.getenv("PATH");
                    String[] directories = path.split(":");

                    boolean found = false;

                    for (int i = 0; i < directories.length; i++) {

                        Path fullPath = Paths.get(directories[i], argument);

                        if (Files.exists(fullPath) &&
                            Files.isExecutable(fullPath)) {

                            System.out.println(argument + " is " + fullPath);

                            found = true;
                            break;
                        }
                    }

                    if (!found) {

                        System.out.println(argument + ": not found");

                    }
                }

            }

            // External command
            else {

                // Split command into program + arguments
                String[] parts = command.split(" ");

                String programName = parts[0];

                String path = System.getenv("PATH");

                String[] directories = path.split(":");

                boolean found = false;

                for (String directory : directories) {

                    Path fullPath = Paths.get(directory, programName);

                    if (Files.exists(fullPath) &&
                        Files.isExecutable(fullPath)) {

                        // Create command array
                        String[] processCommand = new String[parts.length];

                        // First item = full path to program
                        processCommand[0] = fullPath.toString();

                        // Remaining items = arguments
                        for (int i = 1; i < parts.length; i++) {

                            processCommand[i] = parts[i];

                        }

                        ProcessBuilder processBuilder =
                                new ProcessBuilder(processCommand);

                        // Let external program use same terminal
                        processBuilder.inheritIO();

                        Process process = processBuilder.start();

                        // Wait for program to finish
                        process.waitFor();

                        found = true;

                        break;
                    }
                }

                if (!found) {

                    System.out.println(command + ": command not found");

                }
            }
        }
    }
}