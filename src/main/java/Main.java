import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("$ ");
            String command = scanner.nextLine();
            if (command.equals("exit")) {
                break;
            }
            if (command.startsWith("echo")) {
                System.out.println(command.substring(5));
            }
            // System.out.println(command + ": command not found");
        }
    }
}
