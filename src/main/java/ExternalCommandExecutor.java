import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ExternalCommandExecutor {

    public static void execute(List<String> parts) throws Exception {

        String programName = parts.get(0);

        String outputFile = null;
        String errorOutputFile = null;

        int redirectIndex = parts.indexOf(">");
        int appendRedirectIndex = parts.indexOf(">>");
        int errorRedirectIndex = parts.indexOf("2>");
        int errorAppendRedirectIndex = parts.indexOf("2>>");

        boolean appendOutput = appendRedirectIndex != -1;
        boolean appendError = errorAppendRedirectIndex != -1;

        int commandEndIndex = parts.size();

        if (appendRedirectIndex != -1) {
            outputFile = parts.get(appendRedirectIndex + 1);
            commandEndIndex = appendRedirectIndex;

        } else if (redirectIndex != -1) {
            outputFile = parts.get(redirectIndex + 1);
            commandEndIndex = redirectIndex;

        } else if (errorAppendRedirectIndex != -1) {
            errorOutputFile = parts.get(errorAppendRedirectIndex + 1);
            commandEndIndex = errorAppendRedirectIndex;

        } else if (errorRedirectIndex != -1) {
            errorOutputFile = parts.get(errorRedirectIndex + 1);
            commandEndIndex = errorRedirectIndex;
        }

        List<String> commandParts = new ArrayList<>();

        for (int i = 0; i < commandEndIndex; i++) {
            commandParts.add(parts.get(i));
        }

        String path = System.getenv("PATH");

        if (path == null || path.isEmpty()) {
            System.out.println(programName + ": command not found");
            return;
        }

        String[] directories = path.split(":");
        boolean found = false;

        for (int i = 0; i < directories.length; i++) {

            Path fullPath = Paths.get(directories[i], programName);

            if (Files.exists(fullPath) && Files.isExecutable(fullPath)) {

                ProcessBuilder processBuilder =
                        new ProcessBuilder(commandParts);

                if (outputFile != null) {

                    File file = new File(outputFile);

                    if (appendOutput) {
                        processBuilder.redirectOutput(
                                ProcessBuilder.Redirect.appendTo(file));
                    } else {
                        processBuilder.redirectOutput(file);
                    }

                } else if (errorOutputFile != null) {

                    File file = new File(errorOutputFile);

                    if (appendError) {
                        processBuilder.redirectError(
                                ProcessBuilder.Redirect.appendTo(file));
                    } else {
                        processBuilder.redirectError(file);
                    }

                } else {
                    processBuilder.inheritIO();
                }

                Process process = processBuilder.start();
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
