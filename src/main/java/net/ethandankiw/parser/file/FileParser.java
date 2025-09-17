package net.ethandankiw.parser.file;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileParser {

	private static final Logger logger = LoggerFactory.getLogger(FileParser.class);


	private FileParser() {
	}


	public static @Nullable String parseFile(@NotNull Path filePath) {
		// Check if the file cannot be read from
		if (isFileUnreadable(filePath)) {
			return null;
		}

		try {
			// Parse the file contents to a single string
			return Files.readString(filePath);
		} catch (IOException e) {
			logger.error("Cannot read file {}: e", filePath, e);
			return null;
		}
	}


	public static List<String> parseFileLines(@NotNull Path filePath) {
		// Check if the file cannot be read from
		if (isFileUnreadable(filePath)) {
			return List.of();
		}

		try {
			// Parse the file contents to a list of lines
			return Files.readAllLines(filePath);
		} catch (IOException e) {
			logger.error("Cannot read file {}: e", filePath, e);
			return List.of();
		}
	}


	private static boolean isFileUnreadable(@NotNull Path filePath) {
		// Check if the file path points to a directory
		if (Files.isDirectory(filePath)) {
			logger.error("Cannot read from directory {}", filePath);
			return true;
		}

		// Check if the file path cannot be read from
		if (!Files.isReadable(filePath)) {
			logger.error("Cannot read file {}", filePath);
			return true;
		}

		// Otherwise return that the file path is readable
		return false;
	}
}
