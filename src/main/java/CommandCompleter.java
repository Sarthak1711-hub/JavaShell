import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.DirectoryStream;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;
import org.jline.reader.Candidate;

public class CommandCompleter implements Completer {

    private String previousWord = "";
    private int tabCount = 0;
    private Path currentDirectory;

    public CommandCompleter(Path currentDirectory) {
        this.currentDirectory = currentDirectory;
    }

    public void setCurrentDirectory(Path newDirectory) {
        this.currentDirectory = newDirectory;
    }

    @Override
    public void complete(LineReader reader, ParsedLine line,
            List<Candidate> candidates) {

        String word = line.word();

        if (!word.equals(previousWord)) {
            tabCount = 0;
            previousWord = word;
        }

        // File and directory completion.
        if (line.wordIndex() > 0) {

            String directoryPath = "";
            String prefix = word;

            // Check whether the user typed a directory path.
            if (word.contains("/")) {
                int index = word.lastIndexOf("/");
                directoryPath = word.substring(0, index + 1);
                prefix = word.substring(index + 1);
            }

            Path directory = currentDirectory.resolve(directoryPath).normalize();
            List<Path> matchingFiles = new ArrayList<>();

            try (DirectoryStream<Path> files =
                    Files.newDirectoryStream(directory)) {

                for (Path file : files) {
                    String fileName = file.getFileName().toString();

                    if (fileName.startsWith(prefix)) {
                        matchingFiles.add(file);
                    }
                }

            } catch (Exception e) {
                ringBell();
                return;
            }

            Collections.sort(matchingFiles);

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

                if (tabCount == 1) {
                    ringBell();
                    return;
                }

                StringBuilder output = new StringBuilder();

                for (int i = 0; i < matchingFiles.size(); i++) {
                    Path match = matchingFiles.get(i);

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
            ringBell();
            return;
        }

        // Command-name completion.
        List<String> matches = new ArrayList<>();

        String path = System.getenv("PATH");

        if (path == null || path.isEmpty()) {
            ringBell();
            return;
        }

        String[] directories = path.split(":");

        for (int i = 0; i < directories.length; i++) {

            Path directory = Paths.get(directories[i]);

            if (!Files.exists(directory) || !Files.isDirectory(directory)) {
                continue;
            }

            try (DirectoryStream<Path> files =
                    Files.newDirectoryStream(directory)) {

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
                // Ignore directories that cannot be read.
            }
        }

        Collections.sort(matches);

        // No command matches.
        if (matches.isEmpty()) {
            ringBell();
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

        // Extend the typed input when a longer common prefix exists.
        if (commonPrefix.length() > word.length()) {

            candidates.add(new Candidate(
                    commonPrefix, commonPrefix,
                    null, null, null, null, true));

            tabCount = 0;
            return;
        }

        // First TAB rings the bell; second TAB lists matches.
        tabCount++;

        if (tabCount == 1) {
            ringBell();
            return;
        }

        for (int i = 0; i < matches.size(); i++) {
            String match = matches.get(i);

            candidates.add(new Candidate(
                    match, match, null, null, null, null, true));
        }

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
