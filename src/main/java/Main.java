import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws Exception {

        Scanner scanner = new Scanner(System.in);
        Path currentDirectory = Paths.get(System.getProperty("user.dir"));
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
                if (argument.equals("exit") || argument.equals("echo") || argument.equals("type")) {
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
                        if (Files.exists(fullPath) && Files.isExecutable(fullPath)) {

                            System.out.println(argument + " is " + fullPath);

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

            } else if (command.equals("pwd")) {

                // System.out.println(System.getProperty("user.dir"));
                System.out.println(currentDirectory);

            } else if (command.startsWith("cd ")) {

                String[] parts = command.split(" ");

                Path userPath = Paths.get(parts[1]);
                Path destination;

                if (parts[1].equals("~")) {
                    userPath = Paths.get(System.getProperty("user.home"));
                }

                if (userPath.isAbsolute()) {

                    destination = userPath;
                } else {
                    destination = currentDirectory.resolve(userPath).normalize();
                }
                if (Files.exists(destination) && Files.isDirectory(destination)) {
                    currentDirectory = destination;
                } else {
                    System.out.println("cd: " + parts[1] + ": No such file or directory");
                }

            }

            // Unknown command
            else {

                String[] parts = command.split(" ");

                String programName = parts[0];

                String path = System.getenv("PATH");

                String[] directories = path.split(":");

                boolean found = false;

                for (int i = 0; i < directories.length; i++) {

                    String directory = directories[i];

                    Path fullPath = Paths.get(directory, programName);

                    if (Files.exists(fullPath) && Files.isExecutable(fullPath)) {

                        ProcessBuilder pb = new ProcessBuilder(parts);
                        pb.inheritIO();
                        Process process = pb.start();
                        process.waitFor();

                        found = true;

                        break;
                    }
                }

                if (!found) {

                    System.out.println(programName + ": command not found");

                }
            }
        }
    }
}