package net.ethandankiw.socket;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
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
}
