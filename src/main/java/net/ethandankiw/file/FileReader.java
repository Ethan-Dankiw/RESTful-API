package net.ethandankiw.file;

import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileReader {

	private static final Logger logger = LoggerFactory.getLogger(FileReader.class);

	private Path directory;


	public boolean setDirectory(@NotNull String directory) {
		try {
			// Locate the directory in the file system
			Path dir = Paths.get(directory);

			// Check if the path exists
			if (!Files.exists(dir)) {
				logger.error("Directory does not exist");
				return false;
			}

			// Check if it is a directory
			if (!Files.isDirectory(dir)) {
				logger.error("Path is not a directory");
				return false;
			}

			// Load the directory
			this.directory = dir;

			// Return a successful assignment
			return true;
		} catch (InvalidPathException ipe) {
			logger.error("Invalid Path", ipe);
			return false;
		}
	}


	public @Nullable Path getFile(@NotNull String fileName) {
		// Check if the directory exists
		if (directory == null) {
			logger.error("Unable to get file as parent directory has not be set");
			return null;
		}

		// Resolve the file relative to the loaded directory
		Path filePath = directory.resolve(fileName);

		// Check if the file exists
		if (!Files.exists(filePath)) {
			logger.error("File does not exist: {}", filePath);
			return null;
		}

		// Optionally, check if it is a regular file
		if (!Files.isRegularFile(filePath)) {
			logger.error("Path is not a regular file: {}", filePath);
			return null;
		}

		return filePath;
	}
}
