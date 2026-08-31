import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("$ ");
            String command = scanner.nextLine();
            if (command.equals("exit")) {
                break;
            } else if (command.startsWith("echo")) {
                System.out.println(command.substring(5));
            } else {

                System.out.println(command + ": command not found");
            }
            if(command.equals("type")){
                if(command.equals("exit") || command.startsWith("echo")){
                    System.out.println(command + " is a shell builtin");
                }
                else{
                    System.out.println(command + ": not found");
                }
            }
        }
    }
}
