![Java](https://img.shields.io/badge/Java-11+-007396?style=flat-square&logo=openjdk)
![Maven](https://img.shields.io/badge/Maven-3.6+-C71A36?style=flat-square&logo=apachemaven)
![NIO](https://img.shields.io/badge/NIO-ProcessBuilder-FFD700?style=flat-square)
![REPL](https://img.shields.io/badge/Pattern-REPL-blueviolet?style=flat-square)
![License](https://img.shields.io/badge/License-MIT-green?style=flat-square)

# 🐚 JavaShell

> A Unix-like shell built from scratch in Java — engineered for command parsing, process management, and executable discovery across the system PATH.

Built with **Java 11+ · Maven · Java NIO**

---

## 📌 Overview

JavaShell is a lightweight, feature-complete command-line shell that reads user commands, resolves executables, and spawns child processes. It demonstrates core OS and Java concepts through a clean, production-grade implementation.

Unlike shells bundled with the system, every aspect of JavaShell is built manually — from the REPL loop to PATH resolution to process creation via `ProcessBuilder`.

| Aspect | Behavior |
|---|---|
| 🔄 **Command Loop** | Interactive REPL with persistent prompt |
| 🔍 **Command Resolution** | Checks builtins first, then searches PATH directories |
| ⚙️ **Process Execution** | Spawns external programs with arguments via ProcessBuilder |
| 🛡️ **Error Handling** | Graceful "command not found" responses |

---

## ✨ Features

| Feature | Details |
|---|---|
| 🔁 **Interactive REPL** | Persistent shell prompt, line-by-line command processing |
| 🔧 **Built-in Commands** | `exit`, `echo`, `type` — hardcoded in the shell |
| 🌍 **PATH Resolution** | Searches `$PATH` environment variable for executables |
| 📂 **File Executable Check** | Uses Java NIO to verify file existence and execute permissions |
| 🚀 **External Program Execution** | Spawns any system binary with arguments using ProcessBuilder |
| 📝 **Command Parsing** | Splits input into program name and arguments |

---

## 🛠️ Tech Stack

| Layer | Technology | Purpose |
|---|---|---|
| Runtime | Java 11+ | JVM-based execution |
| Build Tool | Maven 3.6+ | Compilation, dependency management |
| I/O | Java Scanner | Command input and prompt display |
| Filesystem | Java NIO (`Path`, `Paths`, `Files`) | Executable discovery and permission checks |
| Process Control | `ProcessBuilder` | Cross-platform child process spawning |
| Environment | System environment variables | PATH lookup at runtime |

---

## 🗂️ Project Structure

```
JavaShell/
│
├── src/
│   └── main/
│       └── java/
│           └── Main.java           # Single-file implementation
├── pom.xml                         # Maven build configuration
├── README.md                       # This file
└── .gitignore
```

**Main.java Breakdown:**
- **REPL Loop** (`while (true)`) – Reads and processes commands indefinitely
- **Builtin Handlers** – `exit`, `echo`, `type` command logic
- **PATH Resolution** – Iterates through `PATH` directories to find executables
- **ProcessBuilder Integration** – Launches external programs and inherits I/O streams

---

## 📐 Architecture

Command execution follows a single-file, linear flow optimized for clarity:

```
    User Input (Scanner)
         │
         ▼
    Parse Command
    (split by space)
         │
         ▼
    ┌──────────────────┐
    │ Is Builtin?      │
    └────┬───────┬─────┘
         │       │
        YES      NO
         │       │
         ▼       ▼
    Execute   Search PATH
    Builtin    Directories
         │       │
         │       ▼
         │   ┌────────────┐
         │   │ Found?     │
         │   └──┬──────┬──┘
         │      │      │
         │     YES     NO
         │      │      │
         │      ▼      ▼
         │   Execute  Print Error
         │   Program
         │      │
         └──────┼──────────┐
                │          │
                ▼          ▼
           Display Output Continue Loop
```

---

## ⚙️ Getting Started

### Prerequisites

- **Java 11+** — Check: `java -version`
- **Maven 3.6+** — Check: `mvn -version`
- **Git** — For cloning the repository
- **Unix-like Environment** — Linux, macOS, or WSL on Windows

### 1 — Clone the Repository

```bash
git clone https://github.com/Sarthak1711-hub/JavaShell.git
cd JavaShell
```

### 2 — Build with Maven

```bash
mvn clean compile
```

### 3 — Run JavaShell

**Option A: Via Maven**
```bash
mvn exec:java -Dexec.mainClass="Main"
```

**Option B: Direct Execution**
```bash
mvn compile
java -cp target/classes Main
```

Server starts and displays:
```
$
```

---

## 🔌 Command Reference

### Built-in Commands

| Command | Syntax | Description | Example |
|---|---|---|---|
| `exit` | `exit` | Terminate the shell | `$ exit` |
| `echo` | `echo [text]` | Print text to stdout | `$ echo Hello, World!` |
| `type` | `type [command]` | Show command type (builtin or path) | `$ type ls` |

### External Commands

Any command not recognized as a builtin is treated as an external program:

```bash
$ pwd
$ ls -la
$ grep "pattern" file.txt
$ cat /etc/hosts
```

---

## 💻 Usage Examples

### Built-in Commands

**echo**
```bash
$ echo Hello, World!
Hello, World!

$ echo This is JavaShell
This is JavaShell
```

**type**
```bash
$ type echo
echo is a shell builtin

$ type ls
ls is /bin/ls

$ type nonexistent
nonexistent: not found
```

**exit**
```bash
$ exit
(shell terminates)
```

### External Commands

**System Utilities**
```bash
$ pwd
/home/user/JavaShell

$ ls -l
(directory listing)

$ whoami
username

$ date
Tue Sep 01 15:30:45 IST 2026
```

---

## 🔄 Command Execution Flow

A detailed walkthrough of what happens when you type a command:

```
  $ ls -la /tmp
       │
       ▼
  1. Scanner reads line: "ls -la /tmp"
       │
       ▼
  2. Split into parts: ["ls", "-la", "/tmp"]
       │
       ▼
  3. Check if "ls" is a builtin
     └─ exit? No
     └─ echo? No
     └─ type? No
       │
       ▼
  4. Get PATH environment variable
     └─ Split by ":" into directories
       │
       ▼
  5. Search each directory:
     ├─ /usr/local/bin/ls ? No
     ├─ /usr/bin/ls ? YES ✓
       │
       ▼
  6. Create ProcessBuilder with full command
     └─ ["/usr/bin/ls", "-la", "/tmp"]
       │
       ▼
  7. Inherit I/O streams
     └─ Child process output → Terminal
       │
       ▼
  8. Execute & wait for completion
     └─ Process.waitFor()
       │
       ▼
  ✅ Prompt returns for next command
```

---

## 🎓 Design Patterns & Concepts

| Concept | Implementation |
|---|---|
| **REPL** | Infinite loop with Scanner — prompt → input → process → repeat |
| **Command Dispatch** | If-else chain routing to builtin or external handlers |
| **PATH Resolution** | Linear search through `$PATH` directories via `Files.exists()` and `Files.isExecutable()` |
| **Process Management** | `ProcessBuilder` with `inheritIO()` for transparent I/O |
| **Environment Access** | `System.getenv()` for Java access to system variables |
| **File System Checks** | Java NIO (`Path`, `Paths`, `Files`) for executable discovery |

---

## 🧪 Testing

Run Codecrafters automated tests:

```bash
ccr test
```

Or verify manually with common shell commands:
```bash
$ type echo
echo is a shell builtin

$ pwd
/path/to/current/directory

$ exit
```

---

## 📄 License

MIT License — Open-source and free to use for learning and personal projects.

---

> Built by **Sarthak** — MCA Student, Amity University Noida
