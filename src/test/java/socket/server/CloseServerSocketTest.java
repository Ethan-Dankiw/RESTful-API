package socket.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import net.ethandankiw.socket.ServerSocketUtils;

class CloseServerSocketTest {

	// Define the port values for server socket creation
	private static final Integer VALID_PORT = 8080;

	// Define the server socket to make the connection to
	private ServerSocket server = null;


	// Before each test, create a server socket connection
	@BeforeEach
	void setup() {
		// Create a server socket
		Optional<ServerSocket> optSocket = ServerSocketUtils.createSocket(VALID_PORT);

		// Validate that the server socket is created
		Assertions.assertTrue(optSocket.isPresent(), "Server socket does not exist when it should");

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


	// Test valid deletion
	@Test
	void testValidDeletion() {
		// Attempt to close the connection
		boolean success = ServerSocketUtils.closeConnection(server);

		// Check if the connection was closed
		Assertions.assertTrue(success, "Socket should have closed but didn't");
	}


	// Test duplicate deletion
	@Test
	void testDuplicateDeletion() {
		// Attempt to close the connection
		boolean success = ServerSocketUtils.closeConnection(server);

		// Check if the connection was closed
		Assertions.assertTrue(success, "Socket should have closed but didn't");

		// Reattempt to close the connection
		boolean dupeSuccess = ServerSocketUtils.closeConnection(server);

		// Check if the connection was closed
		Assertions.assertTrue(dupeSuccess, "Socket should have closed but didn't");
	}


	// Test IO Exception
	@Test
	void testIOException() throws IOException {
		// Clock the server socket class
		ServerSocket mockServerSocket = Mockito.mock(ServerSocket.class);

		// When the server socket is closed, throw a simulated IO exception
		Mockito.doThrow(new IOException("Simulated I/O error"))
			   .when(mockServerSocket)
			   .close();

		// Attempt to close the mocked server socket
		boolean success = ServerSocketUtils.closeConnection(mockServerSocket);

		// Check that the close attempt failed
		Assertions.assertFalse(success, "Close should have failed but didn't");
	}
}
