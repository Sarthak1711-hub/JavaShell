import java.nio.file.Files;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.io.PrintWriter;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.Completer;
import org.jline.reader.Candidate;
import org.jline.reader.ParsedLine;

public class Main {

    public static void main(String[] args) throws Exception {

        Scanner scanner = new Scanner(System.in);

        Path currentDirectory = Paths.get(System.getProperty("user.dir"));

        MyCompleter completer = new MyCompleter(currentDirectory);

        LineReader reader = LineReaderBuilder.builder().completer(completer).option(LineReader.Option.AUTO_LIST, false)
                .build();

        while (true) {

            String command = reader.readLine("$ ");

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

                        writer = new PrintWriter(
                                new java.io.FileOutputStream(outputFile, true));

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

                    destination = Paths.get(System.getProperty("user.home"));

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
                    completer.setCurrentDirectory(currentDirectory);

                } else {

                    System.out.println(
                            "cd: " + inputPath
                                    + ": No such file or directory");
                }

            } else {

                String programName = parts.get(0);

                String outputFile = null;
                String errorOutputFile = null;

                int redirectIndex = parts.indexOf(">");
                int appendRedirectIndex = parts.indexOf(">>");
                int errorRedirectIndex = parts.indexOf("2>");
                int errorAppendRedirectIndex = parts.indexOf("2>>");

                boolean appendOutput = appendRedirectIndex != -1;
                boolean appendError = errorAppendRedirectIndex != -1;

                List<String> commandParts;

                if (appendRedirectIndex != -1) {

                    outputFile = parts.get(appendRedirectIndex + 1);

                    commandParts = new ArrayList<>(
                            parts.subList(0, appendRedirectIndex));

                } else if (redirectIndex != -1) {

                    outputFile = parts.get(redirectIndex + 1);

                    commandParts = new ArrayList<>(
                            parts.subList(0, redirectIndex));

                } else if (errorAppendRedirectIndex != -1) {

                    errorOutputFile = parts.get(errorAppendRedirectIndex + 1);

                    commandParts = new ArrayList<>(
                            parts.subList(0, errorAppendRedirectIndex));

                } else if (errorRedirectIndex != -1) {

                    errorOutputFile = parts.get(errorRedirectIndex + 1);

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

                    Path fullPath = Paths.get(directory, programName);

                    if (Files.exists(fullPath)
                            && Files.isExecutable(fullPath)) {

                        ProcessBuilder pb = new ProcessBuilder(commandParts);

                        if (outputFile != null) {

                            if (appendOutput) {

                                pb.redirectOutput(
                                        ProcessBuilder.Redirect.appendTo(
                                                new java.io.File(outputFile)));

                            } else {

                                pb.redirectOutput(
                                        new java.io.File(outputFile));
                            }

                        } else if (errorOutputFile != null) {

                            if (appendError) {

                                pb.redirectError(
                                        ProcessBuilder.Redirect.appendTo(
                                                new java.io.File(errorOutputFile)));

                            } else {

                                pb.redirectError(
                                        new java.io.File(errorOutputFile));
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

    static class MyCompleter implements Completer {

        private String previousWord = "";
        private int tabCount = 0;
        private Path currentDirectory;

        MyCompleter(Path currentDirectory) {
            this.currentDirectory = currentDirectory;
        }

        void setCurrentDirectory(Path newDirectory) {
            this.currentDirectory = newDirectory;
        }

        @Override
        public void complete(LineReader reader, ParsedLine line,
                List<Candidate> candidates) {

            String word = line.word();

            // Reset TAB count when the input changes.
            if (!word.equals(previousWord)) {
                tabCount = 0;
                previousWord = word;
            }

            // File and directory completion.
            if (line.wordIndex() > 0) {

                // Nested directory completion: ls folder/fi<TAB>
                if (word.contains("/")) {

                    int index = word.lastIndexOf("/");

                    String directoryPath = word.substring(0, index + 1);
                    String prefix = word.substring(index + 1);

                    Path directory = currentDirectory
                            .resolve(directoryPath)
                            .normalize();

                    List<Path> matchingFiles = new ArrayList<>();

                    try (DirectoryStream<Path> files = Files.newDirectoryStream(directory)) {

                        for (Path file : files) {

                            String fileName = file.getFileName().toString();

                            if (fileName.startsWith(prefix)) {
                                matchingFiles.add(file);
                            }
                        }

                    } catch (Exception e) {
                        System.out.print("\u0007");
                        System.out.flush();
                        return;
                    }

                    // Exactly one match.
                    if (matchingFiles.size() == 1) {

                        Path match = matchingFiles.get(0);

                        String matchName = directoryPath
                                + match.getFileName().toString();

                        if (Files.isDirectory(match)) {

                            matchName += "/";

                            candidates.add(new Candidate(
                                    matchName, matchName,
                                    null, null, null, null, true));

                        } else {

                            candidates.add(new Candidate(
                                    matchName, matchName,
                                    null, null, " ", null, true));
                        }

                        return;
                    }

                    // Multiple matches.
                    if (matchingFiles.size() > 1) {

                        tabCount++;

                        // First TAB: ring the bell.
                        if (tabCount == 1) {

                            System.out.print("\u0007");
                            System.out.flush();
                            return;
                        }

                        // Second and subsequent TABs: display matches.
                        Collections.sort(matchingFiles);

                        StringBuilder output = new StringBuilder();

                        for (Path match : matchingFiles) {

                            String matchName = directoryPath
                                    + match.getFileName().toString();

                            if (Files.isDirectory(match)) {
                                matchName += "/";
                            }

                            output.append(matchName).append("  ");
                        }

                        reader.printAbove(output.toString());
                        return;
                    }

                    // No matches.
                    System.out.print("\u0007");
                    System.out.flush();
                    return;
                }

                // Current-directory completion: ls fi<TAB>
                List<Path> matchingFiles = new ArrayList<>();

                try (var files = Files.list(currentDirectory)) {

                    for (Path file : files.toList()) {

                        String fileName = file.getFileName().toString();

                        if (fileName.startsWith(word)) {
                            matchingFiles.add(file);
                        }
                    }

                } catch (Exception e) {

                    System.out.print("\u0007");
                    System.out.flush();
                    return;
                }

                // Exactly one match.
                if (matchingFiles.size() == 1) {

                    Path match = matchingFiles.get(0);

                    String matchName = match.getFileName().toString();

                    if (Files.isDirectory(match)) {

                        matchName += "/";

                        candidates.add(new Candidate(
                                matchName, matchName,
                                null, null, null, null, true));

                    } else {

                        candidates.add(new Candidate(
                                matchName, matchName,
                                null, null, " ", null, true));
                    }

                    return;
                }

                // Multiple matches.
                if (matchingFiles.size() > 1) {

                    tabCount++;

                    // First TAB: ring the bell.
                    if (tabCount == 1) {

                        System.out.print("\u0007");
                        System.out.flush();
                        return;
                    }

                    // Second and subsequent TABs: display matches.
                    Collections.sort(matchingFiles);

                    StringBuilder output = new StringBuilder();

                    for (Path match : matchingFiles) {

                        String matchName = match.getFileName().toString();

                        if (Files.isDirectory(match)) {
                            matchName += "/";
                        }

                        output.append(matchName).append("  ");
                    }

                    reader.printAbove(output.toString());
                    return;
                }

                // No matches.
                System.out.print("\u0007");
                System.out.flush();
                return;
            }

            // Command-name completion.
            List<String> matches = new ArrayList<>();

            String path = System.getenv("PATH");
            String[] directories = path.split(":");

            for (String directory : directories) {

                Path dir = Paths.get(directory);

                if (!Files.exists(dir) || !Files.isDirectory(dir)) {
                    continue;
                }

                try {
                    List<Path> files = Files.list(dir).toList();

                    for (Path file : files) {

                        String fileName = file.getFileName().toString();

                        if (fileName.startsWith(word)
                                && Files.isRegularFile(file)
                                && Files.isExecutable(file)) {

                            if (!matches.contains(fileName)) {
                                matches.add(fileName);
                            }
                        }
                    }

                } catch (Exception e) {
                    // Ignore unreadable directories.
                }
            }

            Collections.sort(matches);

            // No command matches.
            if (matches.isEmpty()) {

                System.out.print("\u0007");
                System.out.flush();
                tabCount = 0;
                return;
            }

            // Exactly one command match.
            if (matches.size() == 1) {

                String match = matches.get(0);

                candidates.add(new Candidate(
                        match, match, null, null, " ", null, true));

                tabCount = 0;
                return;
            }

            // Find the longest common prefix.
            String commonPrefix = matches.get(0);

            for (int i = 1; i < matches.size(); i++) {
                commonPrefix = findCommonPrefix(
                        commonPrefix, matches.get(i));
            }

            // Extend input if possible.
            if (commonPrefix.length() > word.length()) {

                candidates.add(new Candidate(
                        commonPrefix, commonPrefix,
                        null, null, null, null, true));

                tabCount = 0;
                return;
            }

            // First TAB: ring the bell.
            tabCount++;

            if (tabCount == 1) {

                System.out.print("\u0007");
                System.out.flush();
                return;
            }

            // Second TAB: display matching commands.
            for (String match : matches) {

                candidates.add(new Candidate(
                        match, match, null, null, null, null, true));
            }

            tabCount = 0;
        }

        private String findCommonPrefix(String first, String second) {

            int length = Math.min(first.length(), second.length());
            int i = 0;

            while (i < length
                    && first.charAt(i) == second.charAt(i)) {
                i++;
            }

            return first.substring(0, i);
        }
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

                        if (command.charAt(i + 1) == '"' || command.charAt(i + 1) == '\\') {

                            currentArgument = currentArgument + command.charAt(i + 1);

                            i++;

                        } else {

                            currentArgument = currentArgument + c;
                        }

                    } else {

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

            } else if (c == '\'') {

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