package socket.server;

import java.net.ServerSocket;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import net.ethandankiw.socket.ServerSocketUtils;

class CreateServerSocketTest {

	// Define the port values for server socket creation
	private static final Integer VALID_PORT = 8080;
	private static final Integer NEGATIVE_PORT = -1;
	private static final Integer LARGE_PORT = 90000;


	// Test valid server port creation
	@Test
	void testValidCreation() {
		// Get the server socket
		Optional<ServerSocket> optSocket = ServerSocketUtils.createSocket(VALID_PORT);

		// Check that the socket exists
		Assertions.assertTrue(optSocket.isPresent(), "Server socket should exist but doesn't");

		// Get the server socket
		ServerSocket socket = optSocket.get();

		// Check that the socket exists
		Assertions.assertNotNull(socket, "Server socket should not be null " + "but was");

		// Check that the port of the server is correct
		int port = socket.getLocalPort();
		Assertions.assertEquals(VALID_PORT, port,
				"Server socket should be " + "listening on port " + VALID_PORT + " but wasn't");

		// Close the server socket
		boolean success = ServerSocketUtils.closeConnection(socket);

		// Check that the close was successful
		Assertions.assertTrue(success, "Server socket should have closed but " + "didn't");
	}


	// Test valid server port creation but already in use
	@Test
	void testAlreadyInUse() {
		// Get the server socket
		Optional<ServerSocket> optSocket = ServerSocketUtils.createSocket(VALID_PORT);

		// Check that the socket exists
		Assertions.assertTrue(optSocket.isPresent(), "Server socket should exist but doesn't");

		// Get the server socket
		ServerSocket socket = optSocket.get();

		// Check that the socket exists
		Assertions.assertNotNull(socket, "Server socket should not be null " + "but was");

		// Check that the port of the server is correct
		int port = socket.getLocalPort();
		Assertions.assertEquals(VALID_PORT, port,
				"Server socket should be " + "listening on port " + VALID_PORT + " but wasn't");

		// Get a duplicate server socket
		Optional<ServerSocket> optDupeSocket = ServerSocketUtils.createSocket(VALID_PORT);

		// Check that the duplicate socket doesn't exist
		Assertions.assertTrue(optDupeSocket.isEmpty(), "Server socket should not exist but does");

		// Close the server socket
		boolean success = ServerSocketUtils.closeConnection(socket);

		// Check that the close was successful
		Assertions.assertTrue(success, "Server socket should have closed but " + "didn't");
	}


	// Test an invalid port that is too small
	@Test
	void testNegativePort() {
		// Get the server socket
		Optional<ServerSocket> optSocket = ServerSocketUtils.createSocket(NEGATIVE_PORT);

		// Check that the socket doesn't exist
		Assertions.assertTrue(optSocket.isEmpty(), "Server socket should not exist but does");
	}


	// Test an invalid port that is too big
	@Test
	void testLargePort() {
		// Get the server socket
		Optional<ServerSocket> optSocket = ServerSocketUtils.createSocket(LARGE_PORT);

		// Check that the socket doesn't exist
		Assertions.assertTrue(optSocket.isEmpty(), "Server socket should not exist but does");
	}
}
