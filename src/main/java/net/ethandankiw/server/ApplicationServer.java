package net.ethandankiw.server;

import java.net.ServerSocket;
import java.net.SocketException;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.ethandankiw.socket.ServerSocketUtils;

public class ApplicationServer {

	public static final Logger logger = LoggerFactory.getLogger(ApplicationServer.class);

	// Define the port to create the server socket on
	private static final Integer PORT = 8080;


	public static void main(String[] args) throws SocketException {
		// Create a server socket
		Optional<ServerSocket> optSocket = ServerSocketUtils.createSocket(PORT);

		// If the server socket doesn't exist
		if (optSocket.isEmpty()) {
			String msg = "Unable to create server socket";
			logger.error(msg);
			throw new SocketException(msg);
		}

		// Get the server socket
		ServerSocket socket = optSocket.get();

		// Check that the socket is listening on the correct port
		int listeningOn = socket.getLocalPort();
		if (listeningOn != PORT) {
			String msg = String.format("Server socket listening on port %d " + "instead of %d", listeningOn, PORT);
			logger.error(msg);
			throw new SocketException(msg);
		}

		// Attempt to close the server socket
		boolean success = ServerSocketUtils.closeConnection(socket);

		// If the close attempt was not successful
		if (!success) {
			String msg = "Unable to close server socket";
			logger.error(msg);
			throw new SocketException(msg);
		}
	}
}