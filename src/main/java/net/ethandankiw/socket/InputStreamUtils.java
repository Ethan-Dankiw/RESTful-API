package net.ethandankiw.socket;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InputStreamUtils {

	public static final Logger logger = LoggerFactory.getLogger(InputStreamUtils.class);


	private InputStreamUtils() {
	}


	public static Optional<InputStream> getInputStream(@NotNull Socket client) {
		try {
			// Get the input stream
			InputStream stream = client.getInputStream();

			// Return the stream
			return Optional.of(stream);
		} catch (IOException ioe) {
			logger.error("Unable to get client socket input stream: {}", ioe.getMessage());
		}

		// Default to no stream
		return Optional.empty();
	}


	public static byte[] readInputStream(@NotNull InputStream stream) throws SocketTimeoutException {
		try {
			// Read all the data from the stream
			return stream.readAllBytes();
		} catch (SocketTimeoutException ste) {
			// Re-throw the timeout exception so it can be handled by the caller
			throw ste;
		} catch (IOException ioe) {
			logger.error("Unable to read from client socket input stream: {}", ioe.getMessage());
		}

		// Default to an empty byte array
		return new byte[] {};
	}


	public static boolean closeInputStream(@NotNull InputStream stream) {
		try {
			// Close the input stream
			stream.close();

			// Return that the stream was closed successfully
			return true;
		} catch (IOException ioe) {
			logger.error("Unable to close client socket input stream: {}", ioe.getMessage());
		}

		// Default to failed stream close
		return false;
	}
}
