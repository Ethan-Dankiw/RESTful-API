package net.ethandankiw.socket;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OutputStreamUtils {

	public static final Logger logger = LoggerFactory.getLogger(OutputStreamUtils.class);


	private OutputStreamUtils() {
	}


	public static Optional<OutputStream> getOutputStream(@NotNull Socket client) {
		try {
			// Get the output stream
			OutputStream stream = client.getOutputStream();

			// Return the stream
			return Optional.of(stream);
		} catch (IOException ioe) {
			logger.error("Unable to get client socket output stream: {}", ioe.getMessage());
		}

		// Default to no stream
		return Optional.empty();
	}


	public static int writeOutputStream(@NotNull OutputStream stream, @NotNull String str)
			throws SocketTimeoutException {
		// If there is no data to write
		if (str.isBlank()) {
			return 0;
		}

		// Convert the string to an array of bytes
		byte[] data = str.getBytes(StandardCharsets.UTF_8);

		// Write the bytes to the output stream
		return writeOutputStreamBytes(stream, data);
	}


	public static boolean closeOutputStream(@NotNull OutputStream stream) {
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


	private static int writeOutputStreamBytes(@NotNull OutputStream stream, byte[] data) throws SocketTimeoutException {
		try {
			// Write all the data bytes to the socket
			stream.write(data, 0, data.length);

			// Return a successful write
			return data.length;
		} catch (SocketTimeoutException ste) {
			// Re-throw the timeout exception so it can be handled by the caller
			throw ste;
		} catch (IOException ioe) {
			logger.error("Unable to read from client socket input stream: {}", ioe.getMessage());
		}

		// Default to an empty byte array
		return 0;
	}
}
