
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.DirectoryStream;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.io.BufferedReader;
import java.io.IOException;
import java.lang.ProcessBuilder;
import java.io.InputStreamReader;

import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;
import org.jline.reader.Candidate;

public class CommandCompleter implements Completer {

    private String previousWord = "";
    private int tabCount = 0;
    private Path currentDirectory;
    private HashMap<String, String> completions;

    public CommandCompleter(Path currentDirectory, HashMap<String, String> completions) {
        this.currentDirectory = currentDirectory;
        this.completions = completions;
    }

    public void setCurrentDirectory(Path newDirectory) {
        this.currentDirectory = newDirectory;
    }

    @Override
    public void complete(LineReader reader, ParsedLine line,
            List<Candidate> candidates) {

        String word = line.word();

        System.out.println("WORD = " + line.word());
        System.out.println("WORD INDEX = " + line.wordIndex());
        System.out.println("WORDS = " + line.words());
        if (!word.equals(previousWord)) {
            tabCount = 0;
            previousWord = word;
        }

        // Command-name completion
        if (line.wordIndex() == 0) {

            List<String> matches = new ArrayList<>();

            List<String> builtins = List.of("echo", "exit");

            for (String builtin : builtins) {
                if (builtin.startsWith(word)) {
                    matches.add(builtin);
                }
            }

            String path = System.getenv("PATH");

            if (path != null && !path.isEmpty()) {

                String[] directories = path.split(":");

                for (String directoryPath : directories) {

                    Path directory = Paths.get(directoryPath);

                    if (!Files.exists(directory)
                            || !Files.isDirectory(directory)) {
                        continue;
                    }

                    try (DirectoryStream<Path> files = Files.newDirectoryStream(directory)) {

                        for (Path file : files) {

                            String fileName = file.getFileName().toString();

                            if (fileName.startsWith(word)
                                    && Files.isRegularFile(file)
                                    && Files.isExecutable(file)
                                    && !matches.contains(fileName)) {

                                matches.add(fileName);
                            }
                        }

                    } catch (Exception e) {
                        // Ignore unreadable directories
                    }
                }
            }

            Collections.sort(matches);

            if (matches.isEmpty()) {
                tabCount = 0;
                ringBell();
                return;
            }

            if (matches.size() == 1) {

                String match = matches.get(0);

                candidates.add(new Candidate(
                        match, match, null, null, " ", null, true));

                tabCount = 0;
                return;
            }

            String commonPrefix = matches.get(0);

            for (int i = 1; i < matches.size(); i++) {
                commonPrefix = findCommonPrefix(
                        commonPrefix, matches.get(i));
            }

            if (commonPrefix.length() > word.length()) {

                candidates.add(new Candidate(
                        commonPrefix, commonPrefix,
                        null, null, null, null, true));

                tabCount = 0;
                return;
            }

            tabCount++;

            if (tabCount == 1) {
                ringBell();
                return;
            }

            StringBuilder output = new StringBuilder();

            for (String match : matches) {
                output.append(match).append("  ");
            }

            reader.printAbove(output.toString());
            tabCount = 0;
            return;
        }

        try {

            if (line.wordIndex() >= 1) {

                String targetCommandName = line.words().get(0);

                System.out.println("TARGET COMMAND = " + targetCommandName);
                System.out.println("REGISTERED COMPLETIONS = " + completions);
                if (completions.containsKey(targetCommandName)) {

                    String scriptPath = completions.get(targetCommandName);
                    String currentWord = line.word();
                    String previousWord = "";

                    if (line.wordIndex() > 0) {
                        previousWord = line.words().get(line.wordIndex() - 1);
                    }

                    ProcessBuilder pb = new ProcessBuilder(
                            scriptPath,
                            targetCommandName,
                            currentWord,
                            previousWord);
                    

                    pb.environment().put("USERNAME", "Sarthak");
                    Process completeProcess = pb.start();

                    BufferedReader buffReader = new BufferedReader(
                            new InputStreamReader(completeProcess.getInputStream()));

                    String result;

                    while ((result = buffReader.readLine()) != null) {
                        candidates.add(new Candidate(
                                result, result, null, null, " ", null, true));
                    }

                    completeProcess.waitFor();
                    buffReader.close();

                    if (candidates.isEmpty()) {
                        ringBell();
                    }

                    return;
                }
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        // File and directory completion for every argument
        String directoryPath = "";
        String prefix = word;

        if (word.contains("/")) {
            int index = word.lastIndexOf("/");
            directoryPath = word.substring(0, index + 1);
            prefix = word.substring(index + 1);
        }

        Path directory = currentDirectory.resolve(directoryPath).normalize();

        List<Path> matchingFiles = new ArrayList<>();

        try (DirectoryStream<Path> files = Files.newDirectoryStream(directory)) {

            for (Path file : files) {

                String fileName = file.getFileName().toString();

                if (fileName.startsWith(prefix)) {
                    matchingFiles.add(file);
                }
            }

        } catch (Exception e) {
            tabCount = 0;
            ringBell();
            return;
        }

        Collections.sort(matchingFiles);

        // No matches
        if (matchingFiles.isEmpty()) {
            tabCount = 0;
            ringBell();
            return;
        }

        // Exactly one match
        if (matchingFiles.size() == 1) {

            Path match = matchingFiles.get(0);

            String matchName = directoryPath + match.getFileName().toString();

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

            tabCount = 0;
            return;
        }

        // Multiple matches: longest common prefix
        String commonPrefix = matchingFiles.get(0).getFileName().toString();

        for (int i = 1; i < matchingFiles.size(); i++) {

            commonPrefix = findCommonPrefix(
                    commonPrefix,
                    matchingFiles.get(i).getFileName().toString());
        }

        if (commonPrefix.length() > prefix.length()) {

            String completion = directoryPath + commonPrefix;

            candidates.add(new Candidate(
                    completion, completion,
                    null, null, null, null, true));

            tabCount = 0;
            return;
        }

        // First TAB: bell
        tabCount++;

        if (tabCount == 1) {
            ringBell();
            return;
        }

        // Subsequent TAB: list matches
        StringBuilder output = new StringBuilder();

        for (Path match : matchingFiles) {

            String matchName = directoryPath + match.getFileName().toString();

            if (Files.isDirectory(match)) {
                matchName += "/";
            }

            output.append(matchName).append("  ");
        }

        reader.printAbove(output.toString());
        tabCount = 0;
    }

    private void ringBell() {
        System.out.print("\u0007");
        System.out.flush();
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
