package main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class DateTimeValidator {

    public static boolean isValidDateTime(String dateTime) {
        // Check if dateTime is non-empty and conforms to the format "YYYY-MM-DDThh:mm:ssZ".
        if (dateTime == null || dateTime.length() < 20) {
            return false;
        }

        try {
            // break down dateTime format "YYYY-MM-DDThh:mm:ssZ"
            int year = Integer.parseInt(dateTime.substring(0, 4));
            int month = Integer.parseInt(dateTime.substring(5, 7));
            int day = Integer.parseInt(dateTime.substring(8, 10));
            int hour = Integer.parseInt(dateTime.substring(11, 13));
            int minute = Integer.parseInt(dateTime.substring(14, 16));
            int second = Integer.parseInt(dateTime.substring(17, 19));
            String timeZone = dateTime.substring(19);

            // Check if dateTime conforms to the format "YYYY-MM-DDThh:mm:ssZ".
            if (dateTime.charAt(4) != '-' || dateTime.charAt(7) != '-' ||
                    dateTime.charAt(10) != 'T' || dateTime.charAt(13) != ':' ||
                    dateTime.charAt(16) != ':') {
                return false;
            }

            // Basic checks for the date and time ranges.
            if (month < 1 || month > 12)
                return false;
            if (day < 1 || day > 31)
                return false;
            if (hour < 0 || hour > 23)
                return false;
            if (minute < 0 || minute > 59)
                return false;
            if (second < 0 || second > 59)
                return false;

            // Check time Zone formatting "Thh:mm:ssZ"
            if (!timeZone.equals("Z") && !timeZone.matches("[-+][0-1][0-9]:[0-5][0-9]:[0-5][0-9]") && !timeZone.matches("[-+]2[0-3]:[0-5][0-9]:[0-5][0-9]")) {
                return false;
            }

            // If all checks passed, the format is considered valid.
            return true;
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            // Return false if any conversion fails or string is improperly structured
            return false;
        }
    }

    public static void fileProcessor(String sourcePath, String destinationPath) {

        // Initialize a hashset to store valid date times
        Set<String> validDateTimes = new HashSet<>();

        // Read from source file
        try (BufferedReader reader = new BufferedReader(new FileReader(sourcePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (isValidDateTime(line)) {
                    // Add to validDateTimes set if the line is a valid date time
                    validDateTimes.add(line);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading from source path: " + e.getMessage());
        }

        // Write to destination file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(destinationPath))) {
            for (String validDateTime : validDateTimes) {
                // For every valid datetime, write it to the destination file
                writer.write(validDateTime);
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error writing to destination path: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        // Check if the command-line arguments are less than 2
        if (args.length < 2) {
            // Print a usage message if command-line arguments are less than 2
            System.out.println("Usage: java -cp src main.DateTimeValidator <Source Path> <Destination Path>");
            return; //Exit the program.
        }

        // Assign the first command-line argument to sourcePath
        String sourcePath = args[0];
        // Assign the second command-line argument to destinationPath
        String destinationPath = args[1];

        fileProcessor(sourcePath, destinationPath);

    }
}
