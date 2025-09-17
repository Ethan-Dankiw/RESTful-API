package parser.file;

import static org.mockito.Mockito.mockStatic;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import net.ethandankiw.file.FileReader;
import net.ethandankiw.parser.file.FileParser;

class ParseFileLinesTest {

	// Store the directory test files are located in
	private static final String DIRECTORY = "src/test/resources/parser/file";

	// Store the raw file paths for the test files
	private static final String READABLE_PATH = "readableFile.txt";

	// Store the raw contents of each file to compare
	private static final List<String> READABLE_CONTENTS = List.of("Hello World", "This is a test file.");

	// Initialise a file reader
	private static FileReader reader;


	// Before all test are run, initialise the file reader
	@BeforeAll
	static void setup() {
		// Initialise a file reader to read the test files
		reader = new FileReader();

		// Store the directory to read files from
		boolean success = reader.setDirectory(DIRECTORY);

		// Verify that the directory can be stored
		Assertions.assertTrue(success, "Unable to store test file directory");
	}


	// Test parsing lines from a readable file
	@Test
	void testParseFileLinesSuccess() {
		// Get the path to a readable test file
		Path readableFile = reader.getFile(READABLE_PATH);

		// Verify that the test file exists
		Assertions.assertNotNull(readableFile, "Readable file should not be null");

		// Parse the file lines
		List<String> result = FileParser.parseFileLines(readableFile);

		// Verify the parsed lines
		Assertions.assertEquals(READABLE_CONTENTS, result, "File lines should match expected content");
	}


	// Test parsing lines from a non-readable file
	@Test
	void testParseFileLinesNonReadable() {
		// Get the path to a non-readable test file
		Path unreadableDirectory = Paths.get(DIRECTORY);

		// Verify that the test file exists
		Assertions.assertNotNull(unreadableDirectory, "Non-readable directory should not be null");

		// Parse the file lines
		List<String> result = FileParser.parseFileLines(unreadableDirectory);

		// Verify that the result is an empty list
		Assertions.assertTrue(result.isEmpty(), "Non-readable file should return empty list");
	}


	// Test parsing lines from a file that triggers IOException
	@Test
	void testParseFileLinesIOException() {
		// Get the path to a readable test file
		Path readableFile = reader.getFile(READABLE_PATH);

		// Verify that the test file exists
		Assertions.assertNotNull(readableFile, "Readable file should not be null");

		// Mock the files class so that an invalid file slips through file reader
		MockedStatic<Files> mockedFiles = mockStatic(Files.class);

		// When the files class is checked to see if its readable, return true even though it cannot
		// This ensures an unreadable file has an invalid read attempt
		mockedFiles.when(() -> Files.isReadable(readableFile))
				   .thenReturn(true);
		mockedFiles.when(() -> Files.isDirectory(readableFile))
				   .thenReturn(false);

		// When the parser attempts to read all the lines from a file, throw a simulated IO Exception
		mockedFiles.when(() -> Files.readAllLines(readableFile))
				   .thenThrow(new IOException("Simulated IO Exception"));

		// Parse the file into a list of files
		List<String> result = FileParser.parseFileLines(readableFile);

		// Verify if the resulting parsed file has no contents
		Assertions.assertTrue(result.isEmpty(), "IOException should return empty list");

		// Closed the mocked files class
		mockedFiles.close();
	}
}
