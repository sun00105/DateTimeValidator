# DateTime Validator

This program reads a large list of date-time values from a file and writes to a separate file the list of unique, valid date-time values (no duplicates). A valid date-time value matches the following format (ISO 8601): YYYY-MM-DDThh:mm:ssTZD

## How to Run the Code

1. Open a command prompt or terminal.

2. Navigate to the root directory of the project.

    ```sh
    cd path/to/DateTimeValidator
    ```

3. Compile the Java source file.

    ```sh
    javac src/main/DateTimeValidator.java
    ```

4. Run the application, providing input and output file paths as command-line arguments.

    ```sh
    java -cp src main.DateTimeValidator <inputFilePath.txt> <outputFilePath.txt>
    ```

   Replace `<inputFilePath.txt>` with the path to your input file containing date-time strings, and `<outputFilePath.txt>` with the desired output file path.

## Example Usage

Here is an example command to run the application with sample file paths:

```sh
java -cp src main.DateTimeValidator "testFile.txt" "output.txt"
```

