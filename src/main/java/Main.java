import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        System.out.print("$ ");
        Scanner scanner = new Scanner(System.in);
        while (true) {
            // Display the prompt
            System.out.print("$");
            // Read the command
            String command = scanner.nextLine();
            // Print the error message
            System.out.println(command + ": command not found");
        }

    }
}
