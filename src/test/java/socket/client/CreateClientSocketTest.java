package socket.client;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import net.ethandankiw.socket.ClientSocketUtils;
import net.ethandankiw.socket.ServerSocketUtils;

class CreateClientSocketTest {

	// Define the host values for client socket creation
	private static final String VALID_HOST = "localhost";
	private static final String UNKNOWN_HOST = "externalhost";

	// Define the port values for client socket creation
	private static final Integer VALID_PORT = 8080;
	private static final Integer UNKNOWN_PORT = 8081;

	// Define the server socket to make the connection to
	private ServerSocket server = null;


	// Before each test, create a server socket connection
	@BeforeEach
	void setup() {
		// Create a server socket
		Optional<ServerSocket> optSocket = ServerSocketUtils.createSocket(VALID_PORT);

		// Validate that the server socket is created
		Assertions.assertTrue(optSocket.isPresent(), "Server socket does not " + "exist when it should");

		// Store the socket globally
		server = optSocket.get();
	}


	// After each test close the server socket connection
	@AfterEach
	void teardown() {
		// Attempt to close the server connection
		boolean success = ServerSocketUtils.closeConnection(server);

		// Validate that the server socket was closed
		Assertions.assertTrue(success, "Unable to close server connection");

		// Clear the server socket
		server = null;
	}


	// Test valid client port creation
	@Test
	void testValidCreation() {
		// Get the client socket
		Optional<Socket> optSocket = ClientSocketUtils.createSocket(VALID_HOST, VALID_PORT);

		// Check that the socket exists
		Assertions.assertTrue(optSocket.isPresent(), "Client socket should exist but doesn't");

		// Get the client socket
		Socket socket = optSocket.get();

		// Check that the socket exists
		Assertions.assertNotNull(socket, "Client socket should not be null " + "but was");

		// Check that the port of the client is correct
		int port = socket.getPort();
		Assertions.assertEquals(VALID_PORT, port,
				"Client socket should be " + "listening on port " + VALID_PORT + " but wasn't");

		// Close the client socket
		boolean success = ClientSocketUtils.closeConnection(socket);

		// Check that the close was successful
		Assertions.assertTrue(success, "Client socket should have closed but " + "didn't");
	}


	// Test valid client port creation but already in use
	@Test
	void testAlreadyInUse() {
		// Get the client socket
		Optional<Socket> optSocket = ClientSocketUtils.createSocket(VALID_HOST, VALID_PORT);

		// Check that the socket exists
		Assertions.assertTrue(optSocket.isPresent(), "Client socket should exist but doesn't");

		// Get the client socket
		Socket socket = optSocket.get();

		// Check that the socket exists
		Assertions.assertNotNull(socket, "Client socket should not be null " + "but was");

		// Check that the port of the client is correct
		int port = socket.getPort();
		Assertions.assertEquals(VALID_PORT, port,
				"Client socket should be " + "listening on port " + VALID_PORT + " but wasn't");

		// Get a duplicate client socket
		Optional<Socket> optDupeSocket = ClientSocketUtils.createSocket(VALID_HOST, VALID_PORT);

		// Check that the socket exists
		Assertions.assertTrue(optDupeSocket.isPresent(), "Client socket should exist but doesn't");

		// Get the client socket
		Socket dupeSocket = optDupeSocket.get();

		// Check that the socket exists
		Assertions.assertNotNull(dupeSocket, "Duplicate client socket should not be null " + "but was");

		// Check that the port of the client is correct
		int dupePort = dupeSocket.getPort();
		Assertions.assertEquals(VALID_PORT, dupePort,
				"Duplicate client socket should be " + "listening on port " + VALID_PORT + " but wasn't");

		// Check that both client are connected to the same server
		Assertions.assertEquals(port, dupePort, "Both clients should be " + "connected to the same server but aren't");

		// Close the client socket
		boolean success = ClientSocketUtils.closeConnection(socket);

		// Check that the close was successful
		Assertions.assertTrue(success, "Client socket should have closed but " + "didn't");

		// Close the client socket
		boolean dupeSuccess = ClientSocketUtils.closeConnection(dupeSocket);

		// Check that the close was successful
		Assertions.assertTrue(dupeSuccess, "Client socket should have closed but " + "didn't");
	}


	// Test a valid host, and unknown port
	@Test
	void testValidHostUnknownPort() {
		// Get the client socket
		Optional<Socket> optSocket = ClientSocketUtils.createSocket(VALID_HOST, UNKNOWN_PORT);

		// Check that the socket doesn't exist
		Assertions.assertTrue(optSocket.isEmpty(), "Client socket should not exist but does");
	}


	// Test an unknown host, and valid port
	@Test
	void testUnknownHostValidPort() {
		// Get the client socket
		Optional<Socket> optSocket = ClientSocketUtils.createSocket(UNKNOWN_HOST, VALID_PORT);

		// Check that the socket doesn't exist
		Assertions.assertTrue(optSocket.isEmpty(), "Client socket should not exist but does");
	}


	// Test an unknown host, and unknown port
	@Test
	void testUnknownHostUnknownPort() {
		// Get the client socket
		Optional<Socket> optSocket = ClientSocketUtils.createSocket(UNKNOWN_HOST, UNKNOWN_PORT);

		// Check that the socket doesn't exist
		Assertions.assertTrue(optSocket.isEmpty(), "Client socket should not exist but does");
	}
}
