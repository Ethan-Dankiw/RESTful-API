package net.ethandankiw.socket;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.ethandankiw.utils.TimeUtils;

public class ServerSocketUtils {

	private static final Logger logger = LoggerFactory.getLogger(ServerSocketUtils.class);


	private ServerSocketUtils() {
	}


	public static Optional<@NotNull ServerSocket> createSocket(@NotNull Integer port) {
		// Ensure a valid port
		if (port < 0 || port > 65535) {
			logger.error("Cannot create socket as port is out of bounds");
			return Optional.empty();
		}

		try {
			// Attempt to create a server socket
			ServerSocket socket = new ServerSocket(port);
			logger.info("Server started: {}", socket.getInetAddress());

			// Return the socket
			return Optional.of(socket);
		} catch (IOException ioe) {
			logger.error("Unable to create server socket: {}", ioe.getMessage());

			// Default to no server
			return Optional.empty();
		}
	}


	public static Optional<@NotNull Socket> acceptConnection(@NotNull ServerSocket server) {
		try {
			// Attempt to accept a connection from a client
			Socket client = server.accept();
			logger.info("Client connected: {}", client.getInetAddress());

			return Optional.of(client);
		} catch (IOException ioe) {
			logger.warn("Unable to make a connection to the client: {}", ioe.getMessage());
		}

		// Default to no client connection
		return Optional.empty();
	}


	public static boolean closeConnection(@NotNull ServerSocket server) {
		// If the server is already closed
		if (server.isClosed()) {
			return true;
		}

		try {
			// Attempt to close the server socket
			server.close();
			logger.info("Server socket has been closed");

			// Return if the close was successful
			return server.isClosed();
		} catch (IOException ioe) {
			logger.error("Unable to close server socket: {}", ioe.getMessage());
		}

		// Default to failure
		return false;
	}


	public static boolean setTimeout(@NotNull ServerSocket server, @NotNull Integer duration, @NotNull TimeUnit unit)
			throws IllegalArgumentException {
		// Convert the duration to the correct time unit
		Integer timeout = TimeUtils.convertToMilliseconds(duration, unit);

		// Set the timeout on the socket
		return setTimeout(server, timeout);
	}


	public static boolean setTimeout(@NotNull ServerSocket server, @NotNull Integer timeout)
			throws IllegalArgumentException {
		// If the duration is invalid
		if (timeout <= 0) {
			logger.error("Duration cannot be 0 or negative");
			return false;
		}

		try {
			// Set the socket timeout to the duration
			server.setSoTimeout(timeout);

			// Return a successful timeout change
			return true;
		} catch (SocketException ioe) {
			logger.error("Unable to set server socket timeout: {}", ioe.getMessage());
		}

		return false;
	}
}
