# JavaShell 🐚

A Unix-like shell built from scratch in Java that interprets and executes commands, featuring built-in commands, PATH resolution, and external program execution.

**JavaShell** is a lightweight command-line shell implementation developed as part of the [Codecrafters "Build Your Own Shell"](https://codecrafters.io/challenges/shell) challenge.

## ✨ Features

**Core Functionality:**
- Interactive REPL with shell prompt
- Command parsing and execution
- Built-in commands: `exit`, `echo`, `type`
- PATH environment variable resolution for executable discovery
- External program execution via Java's `ProcessBuilder`
- Support for command arguments and flags

**Built-in Commands:**
- `exit` – Terminate the shell
- `echo [args]` – Print arguments to stdout
- `type [command]` – Show if a command is a builtin or display its path

## 🎓 Concepts Explored

This project demonstrates practical implementation of:
- **REPL Pattern** – Read-Eval-Print Loop architecture
- **Command Parsing** – Tokenizing and parsing user input
- **Environment Variables** – Accessing and using system environment
- **PATH Resolution** – Searching for executables across directories
- **Process Management** – Spawning and managing child processes
- **File System Operations** – Java NIO for file checks and permissions
- **Java NIO APIs** – `Path`, `Paths`, `Files` for filesystem access
- **ProcessBuilder** – Cross-platform process creation

## 🛠️ Technologies

- **Language:** Java 11+
- **Build Tool:** Maven
- **Key Libraries:** Java NIO, ProcessBuilder
- **Version Control:** Git

## 📋 Prerequisites

- Java 11 or higher
- Maven 3.6+
- Git
- Unix-like environment (Linux, macOS) or WSL on Windows

Verify your installation:
```bash
java -version
mvn -version
```

## 🚀 Getting Started

### Clone the Repository

```bash
git clone https://github.com/Sarthak1711-hub/JavaShell.git
cd JavaShell
```

### Build with Maven

```bash
mvn clean compile
```

### Run JavaShell

```bash
mvn exec:java -Dexec.mainClass="Main"
```

Or compile and run directly:
```bash
mvn compile
java -cp target/classes Main
```

## 💻 Usage

Once running, JavaShell will display a prompt and wait for commands:

```bash
$ echo Hello, World!
Hello, World!

$ type echo
echo is a shell builtin

$ type ls
ls is /bin/ls

$ ls -la
(displays directory listing)

$ exit
```

### Command Examples

**Built-in Commands:**
```bash
$ echo This is a test
This is a test

$ type exit
exit is a shell builtin

$ type nonexistent
nonexistent: not found
```

**External Commands:**
```bash
$ pwd
/home/user/JavaShell

$ echo "File listing:" && ls -l
File listing:
(system ls output)
```

## 📁 Project Structure

```
JavaShell/
├── src/
│   └── main/
│       └── java/
│           └── Main.java          # Main shell loop and command handler
├── pom.xml                        # Maven configuration
├── README.md                      # This file
└── .gitignore                     # Git ignore rules
```

**Main.java Overview:**
- `main()` – Initializes REPL loop and command scanner
- Built-in command handlers for `exit`, `echo`, `type`
- PATH resolution logic for external commands
- ProcessBuilder integration for program execution

## 🔄 How It Works

1. **Command Input** – Scanner reads user input line by line
2. **Parsing** – Command is split into program name and arguments
3. **Builtin Check** – First checks if command is a shell builtin
4. **PATH Search** – If not builtin, searches PATH directories for executable
5. **Execution** – Launches program via ProcessBuilder and inherits I/O
6. **Output** – Child process output displays directly to user

## 🧪 Testing

Run the test suite:
```bash
mvn test
```

Codecrafters provides automated tests for each stage of implementation. Pass all stage tests:
```bash
ccr test
```

## 🤝 Contributing

Contributions are welcome! To contribute:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📝 License

This project is licensed under the MIT License – see the LICENSE file for details.

## 🔗 Resources

- [Codecrafters Shell Challenge](https://codecrafters.io/challenges/shell)
- [Java NIO Documentation](https://docs.oracle.com/javase/tutorial/nio/)
- [ProcessBuilder JavaDoc](https://docs.oracle.com/javase/11/docs/api/java.base/java/lang/ProcessBuilder.html)
- [Unix Shell Concepts](https://www.gnu.org/software/bash/manual/)

## 📧 Questions?

Feel free to open an issue on GitHub or reach out with questions about the implementation!
