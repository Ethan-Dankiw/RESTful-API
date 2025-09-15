package net.ethandankiw.socket;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.ethandankiw.utils.TimeUtils;

public class ClientSocketUtils {

	public static final Logger logger = LoggerFactory.getLogger(ClientSocketUtils.class);


	private ClientSocketUtils() {
	}


	/**
	 * Opens a socket connection from a client to a server.
	 *
	 * @param host The IP address or hostname of the server.
	 * @param port The port number of the server.
	 * @return An Optional containing the client socket if the connection is successful, otherwise an empty Optional.
	 */
	public static Optional<@NotNull Socket> createSocket(String host, @NotNull Integer port) {
		// Ensure a valid port
		if (port < 0 || port > 65535) {
			logger.error("Cannot create socket as port is out of bounds");
			return Optional.empty();
		}

		try {
			// Attempt to create a connection to the server socket
			Socket client = new Socket(host, port);
			logger.info("Successfully connected to server at {}:{}", host, port);

			// Return the socket
			return Optional.of(client);
		} catch (IOException e) {
			logger.error("Failed to connect to server at {}:{}. Error: {}", host, port, e.getMessage());

			// Default to no client socket
			return Optional.empty();
		}
	}


	public static boolean closeConnection(@NotNull Socket client) {
		try {
			// Attempt to close the client socket
			client.close();
			logger.info("Client socket has been closed");

			// Return if the close was successful
			return client.isClosed();
		} catch (IOException ioe) {
			logger.error("Unable to close client socket: {}", ioe.getMessage());
		}

		// Default to failure
		return false;
	}


	public static boolean setTimeout(@NotNull Socket client, @NotNull Integer duration, @NotNull TimeUnit unit)
			throws IllegalArgumentException {
		// Convert the duration to the correct time unit
		Integer timeout = TimeUtils.convertToMilliseconds(duration, unit);

		// Set the timeout on the socket
		return setTimeout(client, timeout);
	}


	public static boolean setTimeout(@NotNull Socket client, @NotNull Integer timeout) throws IllegalArgumentException {
		// If the duration is invalid
		if (timeout <= 0) {
			logger.error("Duration cannot be 0 or negative");
			return false;
		}

		try {
			// Set the socket timeout to the duration
			client.setSoTimeout(timeout);

			// Return a successful timeout change
			return true;
		} catch (SocketException ioe) {
			logger.error("Unable to set client socket timeout: {}", ioe.getMessage());
		}

		return false;
	}
}
