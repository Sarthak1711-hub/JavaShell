import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class BuiltinCommands {

    private static Path previousDirectory = null;

    // ECHO COMMAND
    public static void echo(List<String> parts) throws Exception {

        int endIndex;
        String outputFile = null;
        String errorOutputFile = null;
        int redirectIndex = parts.indexOf(">");
        int errorRedirectIndex = parts.indexOf("2>");
        int appendRedirectIndex = parts.indexOf(">>");

        if (appendRedirectIndex != -1) {
            outputFile = parts.get(appendRedirectIndex + 1);

        } else if (redirectIndex != -1) {
            outputFile = parts.get(redirectIndex + 1);
        }

        if (errorRedirectIndex != -1) {
            errorOutputFile = parts.get(errorRedirectIndex + 1);
        }

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

        if (errorOutputFile != null) {
            errorWriter = new PrintWriter(errorOutputFile);
        }

        if (outputFile != null) {

            if (appendRedirectIndex != -1) {
                writer = new PrintWriter(new FileOutputStream(outputFile, true));

            } else {
                writer = new PrintWriter(outputFile);
            }
        }

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
    }

    // PWD COMMAND
    public static void pwd(Path currentDirectory) {
        System.out.println(currentDirectory);
    }

    // CD COMMAND
    public static Path cd(List<String> parts,Path currentDirectory,CommandCompleter completer) {

        if (parts.size() < 2) {
            return currentDirectory;
        }

        String inputPath = parts.get(1);
        if (inputPath.equals("-")) {

            if (previousDirectory == null) {
                System.out.println("cd: OLDPWD not set");
                return currentDirectory;
            }

            Path destination = previousDirectory;

            previousDirectory = currentDirectory;
            completer.setCurrentDirectory(destination);

            System.out.println(destination);

            return destination;
        }
        Path userPath = Paths.get(inputPath);

        Path destination;

        if (inputPath.equals("~")) {
            destination = Paths.get(System.getProperty("user.home"));

        } else if (userPath.isAbsolute()) {
            destination = userPath;

        } else {
            destination = currentDirectory.resolve(userPath).normalize();
        }

        if (Files.exists(destination) && Files.isDirectory(destination)) {
            previousDirectory = currentDirectory;
            completer.setCurrentDirectory(destination);
            return destination;

        } else {
            System.out.println("cd: " + inputPath + ": No such file or directory");

            return currentDirectory;
        }
    }

    // TYPE COMMAND
    public static void type(String argument) {

        if (argument.equals("exit")|| argument.equals("echo")|| argument.equals("type")|| argument.equals("pwd")|| argument.equals("cd")) {

            System.out.println(argument + " is a shell builtin");

        } else {

            String path = System.getenv("PATH");
            String[] directories = path.split(":");

            boolean found = false;

            for (int i = 0; i < directories.length; i++) {

                String directory = directories[i];

                Path fullPath = Paths.get(directory, argument);

                if (Files.exists(fullPath) && Files.isExecutable(fullPath)) {

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

    // EXIT COMMAND
    public static boolean exit(String commandName) {
        return commandName.equals("exit");
    }
}