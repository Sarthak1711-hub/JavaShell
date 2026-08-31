import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
                if (argument.equals("exit")
                        || argument.equals("echo")
                        || argument.equals("type")) {

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

                            System.out.println(
                                    argument + " is " + fullPath
                            );

                            found = true;

                            // Stop searching
                            break;
                        }
                    }

                    // If not found in any directory
                    if (!found) {

                        System.out.println(argument + ": not found");

                    }
                }

            }

            // Unknown command
            else {

                System.out.println(command + ": command not found");

            }
        }
    }
}