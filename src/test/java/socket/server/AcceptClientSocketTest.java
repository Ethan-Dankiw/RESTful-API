package socket.server;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import net.ethandankiw.socket.ClientSocketUtils;
import net.ethandankiw.socket.InputStreamUtils;
import net.ethandankiw.socket.ServerSocketUtils;

class AcceptClientSocketTest {

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


	// Test valid accept
	@Test
	void testValidAccept() {
		// Start a new thread for accepting client connections
		new Thread(() -> {
			// Accept a connection from a client
			Optional<Socket> optSocket = ServerSocketUtils.acceptConnection(server);

			// Check that the socket exists
			Assertions.assertTrue(optSocket.isPresent(), "Client socket should exist but doesn't");

			// Get the client socket
			Socket socket = optSocket.get();

			// Verify that the client is connected
			Assertions.assertTrue(socket.isConnected(), "Client should be connected");

			// Attempt to close the client socket
			boolean success = ClientSocketUtils.closeConnection(socket);

			// Check if the connection was closed
			Assertions.assertTrue(success, "Socket should have closed but didn't");
		}).start();

		// Create a client socket to the server
		Optional<Socket> optSocket = ClientSocketUtils.createSocket(VALID_HOST, VALID_PORT);

		// Check that the socket exists
		Assertions.assertTrue(optSocket.isPresent(), "Client socket should exist but doesn't");

		// Get the client socket
		Socket socket = optSocket.get();

		// Verify that the client is connected to the server
		Assertions.assertTrue(socket.isConnected(), "Client should be able to connect");
	}


	// Test an IO exception when accepting a connection from a client
	@Test
	void testIOException() throws IOException {
		// Close the server socket before calling acceptConnection to force an IOException
		server.close();

		// Now call the method, which should handle the closed socket
		Optional<Socket> optClient = ServerSocketUtils.acceptConnection(server);

		// Assert that the method returns an empty Optional, indicating failure
		Assertions.assertTrue(optClient.isEmpty(), "Connection should fail when server is closed");
	}


	// Test a client timeout exception when there is no client
	@Test
	void testInvalidConnectionTimeout() throws InterruptedException, IllegalArgumentException {
		// Define a test timeout
		int timeout = 1;

		// Set the timeout for server connections
		boolean success = ServerSocketUtils.setTimeout(server, timeout, TimeUnit.SECONDS);

		// Check that the timeout was set
		Assertions.assertTrue(success, "Server socket should have a timeout but doesn't");

		// Use a latch to wait for the server thread to finish
		CountDownLatch latch = new CountDownLatch(1);

		// Use an AtomicReference to capture the result from the thread
		AtomicReference<Optional<Socket>> clientSocketRef = new AtomicReference<>();

		// Start a new thread for accepting client connections
		new Thread(() -> {
			Optional<Socket> optSocket = ServerSocketUtils.acceptConnection(server);

			// Socket should exist
			Assertions.assertTrue(optSocket.isEmpty(), "Client socket should NOT exist");

			// Store the response and signal that the thread is finished
			clientSocketRef.set(optSocket);
			latch.countDown();
		}).start();

		// Wait for the thread to finish
		boolean finished = latch.await((timeout * 1000) + 1000, TimeUnit.MILLISECONDS);

		// Assert that the thread finished (due to the timeout)
		Assertions.assertTrue(finished, "Server thread should have finished due to timeout");

		// Get the possible client
		Optional<Socket> optSocket = clientSocketRef.get();

		// The result should be empty
		Assertions.assertTrue(optSocket.isEmpty(), "Client socket should NOT exist after timeout");
	}


	// Test a client timeout exception when there is a client connected
	@Test
	void testConnectedClientTimeout() throws InterruptedException, IllegalArgumentException {
		int timeout = 1;

		// Latch to synchronize the client and server threads
		CountDownLatch clientConnectedLatch = new CountDownLatch(1);
		CountDownLatch readAttemptedLatch = new CountDownLatch(1);
		AtomicReference<Socket> clientSocketRef = new AtomicReference<>();

		// Start a new thread for accepting client connections
		Thread serverThread = new Thread(() -> {
			// Accept a connection from a client
			Optional<Socket> optSocket = ServerSocketUtils.acceptConnection(server);

			// Check that the socket exists
			Assertions.assertTrue(optSocket.isPresent(), "Client socket should exist but doesn't");

			// Get the client socket
			Socket socket = optSocket.get();

			// Set the timeout for the client socket
			boolean success = ClientSocketUtils.setTimeout(socket, timeout, TimeUnit.SECONDS);

			// Check that the timeout was set
			Assertions.assertTrue(success, "Client socket should have a timeout but doesn't");

			// Verify that the client is connected
			boolean connected = socket.isConnected();
			Assertions.assertTrue(connected, "Client should be connected");

			// Store the client socket for external reference
			clientSocketRef.set(optSocket.get());
			clientConnectedLatch.countDown();
		});

		// Start a new thread for to connect to the server
		Thread clientThread = new Thread(() -> {
			// Accept a connection from a client
			Optional<Socket> optSocket = ClientSocketUtils.createSocket(VALID_HOST, VALID_PORT);

			// Check that the socket exists
			Assertions.assertTrue(optSocket.isPresent(), "Client socket should exist but doesn't");

			// Get the client socket
			Socket socket = optSocket.get();

			// Verify that the client is connected
			boolean connected = socket.isConnected();
			Assertions.assertTrue(connected, "Client should be connected");

			// Set the timeout for the client socket
			boolean success = ClientSocketUtils.setTimeout(socket, timeout, TimeUnit.SECONDS);

			// Check that the timeout was set
			Assertions.assertTrue(success, "Client socket should have a timeout but doesn't");

			// Attempt to get the InputStream from the client socket.
			Optional<InputStream> optStream = InputStreamUtils.getInputStream(socket);

			// Verify that the stream exists
			Assertions.assertTrue(optStream.isPresent(), "Client input stream should exist but doesn't");

			// Get the input stream for the client socket
			InputStream stream = optStream.get();

			// Verify that a socket timeout exception is thrown
			Assertions.assertThrows(SocketTimeoutException.class, () -> {
				// Read from the stream
				InputStreamUtils.readInputStream(stream);
			});

			// Count down the latch after the read attempt
			readAttemptedLatch.countDown();

			// Close the client socket
			boolean clientSuccess = ClientSocketUtils.closeConnection(socket);

			// Check that the close was successful
			Assertions.assertTrue(clientSuccess, "Client socket should have closed but " + "didn't");
		});

		// Start the server thread
		serverThread.start();

		// Start the client thread
		clientThread.start();

		// Wait for the client to attempt the read and fail
		boolean earlyFinish = readAttemptedLatch.await((timeout * 1000) + 1000, TimeUnit.MILLISECONDS);

		// Assert that the timeout occurred and the test completed
		Assertions.assertTrue(earlyFinish, "Read operation should have timed out.");

		// Attempt to close the client socket
		boolean success = ClientSocketUtils.closeConnection(clientSocketRef.get());

		// Check if the connection was closed
		Assertions.assertTrue(success, "Socket should have closed but didn't");
	}
}
