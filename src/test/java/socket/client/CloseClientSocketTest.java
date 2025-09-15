package socket.client;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import net.ethandankiw.socket.ClientSocketUtils;
import net.ethandankiw.socket.ServerSocketUtils;

class CloseClientSocketTest {
	// Define the host values for client socket creation
	private static final String VALID_HOST = "localhost";

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


	// Test valid close
	@Test
	void testValidClose() {
		// Get the client socket
		Optional<Socket> optSocket = ClientSocketUtils.createSocket(VALID_HOST, VALID_PORT);

		// Check that the socket exists
		Assertions.assertTrue(optSocket.isPresent(), "Client socket should exist but doesn't");

		// Get the client socket
		Socket socket = optSocket.get();

		// Check that the socket exists
		Assertions.assertNotNull(socket, "Client socket should not be null but was");

		// Verify that the client is connected
		Assertions.assertTrue(socket.isConnected(), "Client should be connected");

		// Attempt to close the connection
		boolean closeSuccess = ClientSocketUtils.closeConnection(socket);

		// Check if the connection was closed
		Assertions.assertTrue(closeSuccess, "Socket should have closed but didn't");
	}


	// Test duplicate close
	@Test
	void testDuplicateClose() {
		// Get the client socket
		Optional<Socket> optSocket = ClientSocketUtils.createSocket(VALID_HOST, VALID_PORT);

		// Check that the socket exists
		Assertions.assertTrue(optSocket.isPresent(), "Client socket should exist but doesn't");

		// Get the client socket
		Socket socket = optSocket.get();

		// Check that the socket exists
		Assertions.assertNotNull(socket, "Client socket should not be null but was");

		// Verify that the client is connected
		Assertions.assertTrue(socket.isConnected(), "Client should be connected");

		// Attempt to close the connection
		boolean closeSuccess = ClientSocketUtils.closeConnection(socket);

		// Check if the connection was closed
		Assertions.assertTrue(closeSuccess, "Socket should have closed but didn't");

		// Attempt to close the connection
		boolean dupeCloseSuccess = ClientSocketUtils.closeConnection(socket);

		// Check if the connection was closed
		Assertions.assertTrue(dupeCloseSuccess, "Socket should have closed but didn't");
	}


	// Test IO Exception
	@Test
	void testIOException() throws IOException {
		// Clock the client socket class
		Socket mockSocket = Mockito.mock(Socket.class);

		// When the server socket is closed, throw a simulated IO exception
		Mockito.doThrow(new IOException("Simulated I/O error"))
			   .when(mockSocket)
			   .close();

		// Attempt to close the mocked server socket
		boolean success = ClientSocketUtils.closeConnection(mockSocket);

		// Check that the close attempt failed
		Assertions.assertFalse(success, "Close should have failed but didn't");
	}
}
