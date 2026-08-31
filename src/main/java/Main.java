import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws Exception {

        Scanner scanner = new Scanner(System.in);

        while (true) {

            System.out.print("$ ");

            String command = scanner.nextLine();

            if (command.equals("exit")) {

                break;

            } else if (command.startsWith("echo ")) {

                System.out.println(command.substring(5));

            } else if (command.startsWith("type ")) {

                String[] parts = command.split(" ");

                String argument = parts[1];

                if (argument.equals("exit") || argument.equals("echo") || argument.equals("type")) {

                    System.out.println(argument + " is a shell builtin");

                } else {

                    System.out.println(argument + ": not found");

                }

            } else {

                System.out.println(command + ": command not found");

            }
        }
    }
}