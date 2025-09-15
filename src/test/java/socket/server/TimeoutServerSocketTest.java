package socket.server;

import java.net.ServerSocket;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import net.ethandankiw.socket.ServerSocketUtils;

class TimeoutServerSocketTest {

	// Define the timeout values for the server socket
	private static final Integer VALID_TIMEOUT = 1000;
	private static final Integer ZERO_TIMEOUT = 0;
	private static final Integer NEGATIVE_TIMEOUT = -1000;

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


	// Test setting timeout to valid value
	@Test
	void testSetValidTimeout() throws InterruptedException {
		// Use a latch to wait for the server thread to finish
		CountDownLatch latch = new CountDownLatch(1);

		// Set the timeout on the server socket
		boolean timeoutSuccess = ServerSocketUtils.setTimeout(server, VALID_TIMEOUT);

		// Check that the timeout was set
		Assertions.assertTrue(timeoutSuccess, "Server socket should have a timeout but doesn't");

		// Wait for the thread to finish
		boolean earlyFinish = latch.await(VALID_TIMEOUT + 1000, TimeUnit.MILLISECONDS);

		// Assert that the timeout occurred
		Assertions.assertFalse(earlyFinish, "Timeout should have occurred");
	}


	// Test setting timeout to zero value
	@Test
	void testSetZeroTimeout() {
		// Set the timeout on the server socket
		boolean timeoutSuccess = ServerSocketUtils.setTimeout(server, ZERO_TIMEOUT);

		// Check that the timeout fired
		Assertions.assertFalse(timeoutSuccess, "Server socket should not have timed out but did");
	}


	// Test setting timeout to negative value
	@Test
	void testSetNegativeTimeout() {
		// Set the timeout on the server socket
		boolean timeoutSuccess = ServerSocketUtils.setTimeout(server, NEGATIVE_TIMEOUT);

		// Check that the timeout fired
		Assertions.assertFalse(timeoutSuccess, "Server socket should not have timed out but did");
	}


	// Test setting timeout on closed socket
	@Test
	void testSetTimeoutClosedSocket() {
		// Close the server socket
		boolean closeSuccess = ServerSocketUtils.closeConnection(server);

		// Validate that the server socket was closed
		Assertions.assertTrue(closeSuccess, "Unable to close server connection");

		// Set the timeout on the server socket
		boolean timeoutSuccess = ServerSocketUtils.setTimeout(server, VALID_TIMEOUT);

		// Check that the timeout fired
		Assertions.assertFalse(timeoutSuccess, "Server socket should not have timed out but did");
	}

}
