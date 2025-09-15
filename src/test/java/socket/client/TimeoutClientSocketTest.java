package socket.client;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import net.ethandankiw.socket.ClientSocketUtils;
import net.ethandankiw.socket.ServerSocketUtils;

class TimeoutClientSocketTest {

	// Define the host values for client socket creation
	private static final String VALID_HOST = "localhost";

	// Define the port values for server socket creation
	private static final Integer VALID_PORT = 8080;

	// Define the timeout values for the server socket
	private static final Integer VALID_TIMEOUT = 1;
	private static final Integer ZERO_TIMEOUT = 0;
	private static final Integer NEGATIVE_TIMEOUT = -1;

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


	// Test setting timeout to valid value
	@Test
	void testSetValidTimeout() throws InterruptedException {
		// Use a latch to wait for the server thread to finish
		CountDownLatch latch = new CountDownLatch(1);

		// Get the client socket
		Optional<Socket> optSocket = ClientSocketUtils.createSocket(VALID_HOST, VALID_PORT);

		// Check that the socket exists
		Assertions.assertTrue(optSocket.isPresent(), "Client socket should exist but doesn't");

		// Get the client socket
		Socket socket = optSocket.get();

		// Check that the socket exists
		Assertions.assertNotNull(socket, "Client socket should not be null " + "but was");

		// Verify that the client is connected
		Assertions.assertTrue(socket.isConnected(), "Client should be connected");

		// Set the timeout on the server socket
		boolean timeoutSuccess = ClientSocketUtils.setTimeout(socket, VALID_TIMEOUT, TimeUnit.SECONDS);

		// Check that the timeout was set
		Assertions.assertTrue(timeoutSuccess, "Client socket should have a timeout but doesn't");

		// Wait for the thread to finish
		boolean earlyFinish = latch.await(VALID_TIMEOUT + 1000, TimeUnit.MILLISECONDS);

		// Assert that the timeout occurred
		Assertions.assertFalse(earlyFinish, "Timeout should have occurred");

		// Attempt to close the connection
		boolean closeSuccess = ClientSocketUtils.closeConnection(socket);

		// Check if the connection was closed
		Assertions.assertTrue(closeSuccess, "Socket should have closed but didn't");
	}


	// Test setting timeout to zero value
	@Test
	void testSetZeroTimeout() {
		// Get the client socket
		Optional<Socket> optSocket = ClientSocketUtils.createSocket(VALID_HOST, VALID_PORT);

		// Check that the socket exists
		Assertions.assertTrue(optSocket.isPresent(), "Client socket should exist but doesn't");

		// Get the client socket
		Socket socket = optSocket.get();

		// Check that the socket exists
		Assertions.assertNotNull(socket, "Client socket should not be null " + "but was");

		// Verify that the client is connected
		Assertions.assertTrue(socket.isConnected(), "Client should be connected");

		// Set the timeout on the server socket
		boolean timeoutSuccess = ClientSocketUtils.setTimeout(socket, ZERO_TIMEOUT, TimeUnit.SECONDS);

		// Check that the timeout was set
		Assertions.assertFalse(timeoutSuccess, "Client socket not have set timeout but did");

		// Attempt to close the connection
		boolean closeSuccess = ClientSocketUtils.closeConnection(socket);

		// Check if the connection was closed
		Assertions.assertTrue(closeSuccess, "Socket should have closed but didn't");
	}


	// Test setting timeout to negative value
	@Test
	void testSetNegativeTimeout() {
		// Get the client socket
		Optional<Socket> optSocket = ClientSocketUtils.createSocket(VALID_HOST, VALID_PORT);

		// Check that the socket exists
		Assertions.assertTrue(optSocket.isPresent(), "Client socket should exist but doesn't");

		// Get the client socket
		Socket socket = optSocket.get();

		// Check that the socket exists
		Assertions.assertNotNull(socket, "Client socket should not be null " + "but was");

		// Verify that the client is connected
		Assertions.assertTrue(socket.isConnected(), "Client should be connected");

		// Set the timeout on the server socket
		boolean timeoutSuccess = ClientSocketUtils.setTimeout(socket, NEGATIVE_TIMEOUT, TimeUnit.SECONDS);

		// Check that the timeout was set
		Assertions.assertFalse(timeoutSuccess, "Client socket not have set timeout but did");

		// Attempt to close the connection
		boolean closeSuccess = ClientSocketUtils.closeConnection(socket);

		// Check if the connection was closed
		Assertions.assertTrue(closeSuccess, "Socket should have closed but didn't");
	}


	// Test setting timeout on closed socket
	@Test
	void testSetTimeoutClosedSocket() {
		// Get the client socket
		Optional<Socket> optSocket = ClientSocketUtils.createSocket(VALID_HOST, VALID_PORT);

		// Check that the socket exists
		Assertions.assertTrue(optSocket.isPresent(), "Client socket should exist but doesn't");

		// Get the client socket
		Socket socket = optSocket.get();

		// Check that the socket exists
		Assertions.assertNotNull(socket, "Client socket should not be null " + "but was");

		// Verify that the client is connected
		Assertions.assertTrue(socket.isConnected(), "Client should be connected");

		// Attempt to close the connection
		boolean closeSuccess = ClientSocketUtils.closeConnection(socket);

		// Check if the connection was closed
		Assertions.assertTrue(closeSuccess, "Socket should have closed but didn't");

		// Set the timeout on the server socket
		boolean timeoutSuccess = ClientSocketUtils.setTimeout(socket, VALID_TIMEOUT, TimeUnit.SECONDS);

		// Check that the timeout was set
		Assertions.assertFalse(timeoutSuccess, "Client socket not have set timeout but did");
	}

}
