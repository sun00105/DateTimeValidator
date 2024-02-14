package test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import main.DateTimeValidator;

import static org.junit.jupiter.api.Assertions.*;


public class DateTimeValidatorTest {

    // Set up for testing that involves writing to the standard output, 'System.out'
    // Initializes current standard output stream so it can be stored after the test.
    private final PrintStream standardOutput = System.out;
    // Declare an in-memory output stream to capture the content that would be printed to the standard output.
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    // Declare an in-memory output stream to capture (redirect System.err) error messages
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();

    /**
     * Set up the testing environment by directing the standard output to outContent.
     */
    @BeforeEach
    public void setUpStreams() {
        // Replace the standard output stream with a new printStream that writes to the ByteArrayOutputStream
        // Capture what would be printed to the console.
        System.setOut(new PrintStream(outContent));
        // Redirect System.err to capture error messages
        System.setErr(new PrintStream(errContent));
    }

    /**
     *  Restore the original standard output stream after the test is complete
     */
    @AfterEach
    public void restoreStreams() {
        // Set the standard output stream back to its original value
        System.setOut(standardOutput);
        // Restore the original System.err
        System.setErr(System.err);
    }

    /**
     *  fileProcessor can read and write properly
     */
    @Test
    public void testFileProcessor() throws Exception {
        // Create a temporary source file
        Path sourceFilename = Files.createTempFile("testInput", ".txt");
        Files.write(sourceFilename, "2024-01-01T00:00:00Z\n2024-02-01T00:00:00Z".getBytes());

        // Create a temporary destination file
        Path destinationFile = Files.createTempFile("testOutput", ".txt");

        // Create an instance of the DataTimeValidator class
        // Invoke the fileProcessor method to process source file and writes the result to the destination file.
        DateTimeValidator dateTimeValidator = new DateTimeValidator();
        dateTimeValidator.fileProcessor(sourceFilename.toString(), destinationFile.toString());

        // Reads the destination file and check if the content match with the expected result
        try (BufferedReader reader = new BufferedReader(new FileReader(destinationFile.toFile()))) {
            assertTrue(reader.readLine().equals("2024-01-01T00:00:00Z"));
            assertTrue(reader.readLine().equals("2024-02-01T00:00:00Z"));
            assertFalse(reader.ready(), "There is no more lines in the file");
        }

        // Cleanup
        Files.delete(sourceFilename);
        Files.delete(destinationFile);
    }

    /**
     * Check only unique valid date times are stored in the destination file.
     * @throws Exception if the DatetimeValidator failed
     */
    @Test
    public void testDuplicateDatesRemoved() throws Exception {
        // Create a temporary source and destination file
        Path sourceFile = Files.createTempFile("inputFile", ".txt");
        Path destinationFile = Files.createTempFile("outputFile", ".txt");

        try {
            // Write duplicate date-time strings to the temporary source file
            Files.write(sourceFile, List.of(
                    "2022-01-01T12:00:00Z", // repeated
                    "2022-01-01T12:00:00Z", // repeated
                    "2022-02-01T15:30:00Z"
            ));

            // Create an instance of the DataTimeValidator class
            // Invoke the fileProcessor method to process source file and writes the result to the destination file.
            DateTimeValidator dateTimeValidator = new DateTimeValidator();
            dateTimeValidator.fileProcessor(sourceFile.toString(), destinationFile.toString());

            // Read the result from the destination file
            List<String> resultLines = Files.readAllLines(destinationFile);

            // Assert that duplicate dates are removed
            assertEquals(2, resultLines.size());
            assertEquals("2022-01-01T12:00:00Z", resultLines.get(0));
            assertEquals("2022-02-01T15:30:00Z", resultLines.get(1));
        } finally {
            // Clean up: delete temporary files
            Files.deleteIfExists(sourceFile);
            Files.deleteIfExists(destinationFile);
        }
    }

    /**
     * Check only valid date times are stored in the destination file.
     * @throws Exception if the DatetimeValidator failed
     */
    @Test
    public void testInvalidDatesRemoved() throws Exception {
        // Create a temporary source and destination file
        Path sourceFile = Files.createTempFile("inputFile", ".txt");
        Path destinationFile = Files.createTempFile("outputFile", ".txt");

        try {
            // Write duplicate date-time strings to the temporary source file
            Files.write(sourceFile, List.of(
                    "2023-01-01 00:00:00Z", // Invalid separator between date and time
                    "2023-00-01T00:00:00Z", // Invalid month
                    "2023-01-32T00:00:00Z", // Invalid date
                    "24:00:00Z",  // missing date
                    "2022-11-30T24:01:01Z", //
                    "2023-01-01T00:00:00+24:00", // Invalid time zone offset
                    "2023-01-01T00:00:00+00:60",  // Invalid time zone minutes
                    "2022-02-01T15:30:00Z", // Valid datetime
                    "Invalid-Date-Time", // Invalid datetime
                    "2023-01-01T00:00:00Z12:00"  // Misplaced 'Z'
            ));

            // Create an instance of the DataTimeValidator class
            // Invoke the fileProcessor method to process source file and writes the result to the destination file.
            DateTimeValidator dateTimeValidator = new DateTimeValidator();
            dateTimeValidator.fileProcessor(sourceFile.toString(), destinationFile.toString());

            // Read the result from the destination file
            List<String> resultLines = Files.readAllLines(destinationFile);

            // Assert that duplicate dates are removed
            assertEquals(1, resultLines.size());
            assertEquals("2022-02-01T15:30:00Z", resultLines.get(0));
        } finally {
            // Clean up: delete temporary files
            Files.deleteIfExists(sourceFile);
            Files.deleteIfExists(destinationFile);
        }
    }

    /**
     *  fileProcessor failed to read due to non-existent source file
     */
    @Test
    public void testFileProcessorReadFailed() {

        // Create an instance of the DataTimeValidator class
        DateTimeValidator dateTimeValidator = new DateTimeValidator();

        // Initialize a fake path to an non-existent file
        String fakeFilePath = "path/to/non-existent-file.txt";

        try {
            // Invoke the fileProcessor method that prints to System.err
            dateTimeValidator.fileProcessor(fakeFilePath, "Output.txt");

            // Assert on the captured error message with its expected error message
            String expectedErrorMessage = "Error reading from source path: path\\to\\non-existent-file.txt (The system cannot find the path specified)";
            assertEquals(expectedErrorMessage, errContent.toString().trim());
        } catch (Exception e) {
            System.err.println("Exception occurred in testFileProcessorReadFailed testing: " + e.getMessage());
        }
    }

    /**
     * fileProcessor failed to write due to invalid file path
     */
    @Test
    public void testFileProcessorWriteFailed() {
        // Create an instance of the DataTimeValidator class
        DateTimeValidator dateTimeValidator = new DateTimeValidator();

        // Initialize an invalid path write to
        String invalidFilePath = "invalid/path/for/output.txt";

        try {
            // Invoke the fileProcessor method that prints to System.err
            dateTimeValidator.fileProcessor("testFile.txt", invalidFilePath);

            // Assert on the captured error message with its expected error message
            String expectedErrorMessage = "Error writing to destination path: invalid\\path\\for\\output.txt (The system cannot find the path specified)";
            assertEquals(expectedErrorMessage, errContent.toString().trim());
        } catch (Exception e) {
            System.err.println("Exception occurred in testFileProcessorWriteFailed testing: " + e.getMessage());
        }

    }

    /**
     *  Adhere to valid datetime string format
     */
    @Test
    public void testValidDateTime() {
        DateTimeValidator dateTimeValidator = new DateTimeValidator();
        String validDateTime = "2022-01-01T12:30:45Z";
        assertTrue(dateTimeValidator.isValidDateTime(validDateTime));
    }

    /**
     * Failed to adhere datetime format: incorrect length
     */
    @Test
    public void testInvalidDateTimeLength() {
        DateTimeValidator dateTimeValidator = new DateTimeValidator();
        String invalidDateTime = "2022-01-01T12:30:4";
        assertFalse(dateTimeValidator.isValidDateTime(invalidDateTime));
    }

    /**
     * Failed to adhere datetime format: incorrect delimiter
     */
    @Test
    public void testInvalidDateTimeDelimiter() {
        DateTimeValidator dateTimeValidator = new DateTimeValidator();
        String invalidDateTime = "2022/01/01T12:30:45Z";
        assertFalse(dateTimeValidator.isValidDateTime(invalidDateTime));
    }

    /**
     *  Invalid datetime due to incorrect date and time range
     */
    @Test
    public void testInvalidDateTimeRange() {
        DateTimeValidator dateTimeValidator = new DateTimeValidator();
        String invalidDateTime = "2022-13-41T25:30:45Z"; // Invalid date and time range
        assertFalse(dateTimeValidator.isValidDateTime(invalidDateTime));
    }

    /**
     * Invalid datetime due to incorrect date and time values
     */
    @Test
    public void testInvalidDateTimeValues() {
        DateTimeValidator dateTimeValidator = new DateTimeValidator();
        String invalidDateTime = "2X22-X1-X1T12:3X:45ZZ"; // Invalid date and time
        assertFalse(dateTimeValidator.isValidDateTime(invalidDateTime));
    }

    /**
     * Invalid datetime due to time zone
     */
    @Test
    public void testInvalidTimeZone() {
        DateTimeValidator dateTimeValidator = new DateTimeValidator();
        String invalidDateTime = "2022-01-01T12:30:45X"; // Invalid time zone
        assertFalse(dateTimeValidator.isValidDateTime(invalidDateTime));
    }

    /**
     * Invalid datetime string due to parsing failure
     */
    @Test
    public void testInvalidDateTimeParsing() {
        DateTimeValidator dateTimeValidator = new DateTimeValidator();
        String invalidDateTime = "invalid-date-time"; // Not a valid date-time
        assertFalse(dateTimeValidator.isValidDateTime(invalidDateTime));
    }

    /**
     *  Input string that causes NumberFormatException
     */
    @Test
    public void testNumberFormatException() {
        DateTimeValidator dateTimeValidator = new DateTimeValidator();
        String invalidDateTime = "2022-01-01T12:30:4X"; // Invalid seconds part
        assertFalse(dateTimeValidator.isValidDateTime(invalidDateTime));
    }

    /**
     *  Input string that causes IndexOutOfBoundsException
     */
    @Test
    public void testIndexOutOfBoundsException() {
        DateTimeValidator dateTimeValidator = new DateTimeValidator();
        String invalidDateTime = "2022-01-01T12:30:45ZZ"; // Shorter than 20 characters
        assertFalse(dateTimeValidator.isValidDateTime(invalidDateTime));
    }


    /**
     *  Main method with Valid Arguments
     */
    @Test
    public void testMainMethodWithValidArguments() {
        String[] args = {"input.txt", "output.txt"};

        DateTimeValidator.main(args);

        // Assert on the output
        String expectedOutput = "";
        assertEquals(expectedOutput, outContent.toString().trim());
    }

    /**
     *  Main method with Invalid number of argument
     */
    @Test
    public void testMainMethodWithInsufficientArguments() {
        // Expect two arguments, but one argument is given.
        String[] args = {"input.txt"};

        DateTimeValidator.main(args);

        // Assert on the output directly
        String expectedOutput = "Usage: java -cp src main.DateTimeValidator <Source Path> <Destination Path>";
        assertEquals(expectedOutput, outContent.toString().trim());
    }



}
