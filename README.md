## File System Simulator (Kotlin)

This project is a Kotlin-based file system simulator created for the **Operating Systems 2** course in the **Computer Engineering** program at **Instituto Federal do Sul de Minas Gerais (Poços de Caldas campus)**.

See the Portuguese version in `README.pt.md`.

### Objective

The system simulates a binary storage layer to abstract disk reads and enables Linux-style commands such as `ls`, `mkdir`, and `cat`. It was designed with configurable bit-length choices to explore system constraints like:

- storage space limits
- maximum file name size
- maximum file size

### Features

- graphical interface inspired by a Linux terminal
- command history and terminal-like execution
- file creation, listing, reading, and deletion simulation

### Project structure

Source code is located in `src/` and is fully migrated to Kotlin.

### Build and run (Gradle)

```bash
./gradlew build
./gradlew run
```
