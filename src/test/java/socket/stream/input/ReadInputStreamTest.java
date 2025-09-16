package socket.stream.input;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

class ReadInputStreamTest {

	// Define the byte to be written to the client socket (server-side or client)
	private static final Integer VALID_BYTE = 42;

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


	// Test that a server-side client socket can read data from a client
	@Test
	void testServerSideClientInputStreamRead() throws InterruptedException, IllegalArgumentException {
		// Latch to synchronize the client and server threads
		CountDownLatch readCompletedLatch = new CountDownLatch(1);

		// Start a new thread for accepting client connections
		Thread serverThread = new Thread(() -> {
			// Accept a connection from a client
			Optional<Socket> optSocket = ServerSocketUtils.acceptConnection(server);

			// Check that the socket exists
			Assertions.assertTrue(optSocket.isPresent(), "Client socket should exist but doesn't");

			// Get the client socket
			Socket socket = optSocket.get();

			// Verify that the client is connected
			Assertions.assertTrue(socket.isConnected(), "Client should be connected but wasn't");

			// Attempt to get the InputStream from the client socket.
			Optional<InputStream> optStream = InputStreamUtils.getInputStream(socket);

			// Verify that the stream exists
			Assertions.assertTrue(optStream.isPresent(), "Client input stream should exist but doesn't");

			// Get the input stream for the client socket
			InputStream stream = optStream.get();

			// Verify that no exception is thrown
			Assertions.assertDoesNotThrow(() -> {
				// Read from the stream
				byte[] bytes = InputStreamUtils.readInputStream(stream);

				// Verify that only one byte was written
				Assertions.assertEquals(1, bytes.length, "More than one byte was read from client socket input stream");

				// Get the first byte
				byte data = bytes[0];

				// Verify that the correct byte was written
				Assertions.assertEquals(VALID_BYTE, data, "Incorrect byte was written to client socket");
			});

			// Count down the latch after the read attempt
			readCompletedLatch.countDown();

			// Close the client socket
			boolean serverClientClosed = ClientSocketUtils.closeConnection(socket);

			// Check that the close was successful
			Assertions.assertTrue(serverClientClosed, "Server-side client socket should have closed but didn't");
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
			Assertions.assertTrue(socket.isConnected(), "Client should be connected but wasn't");

			// Verify that write did not fail
			Assertions.assertDoesNotThrow(() -> {
				OutputStream stream = socket.getOutputStream();
				// Write a single byte to the output stream to avoid a timeout
				stream.write(VALID_BYTE);
				// Close the output socket
				stream.close();
			});

			// Close the client socket
			boolean clientClosed = ClientSocketUtils.closeConnection(socket);

			// Check that the close was successful
			Assertions.assertTrue(clientClosed, "Client socket should have closed but didn't");
		});

		// Start the server thread
		serverThread.start();

		// Start the client thread
		clientThread.start();

		// Wait for the server to read and finish the test
		boolean earlyFinish = readCompletedLatch.await(1000, TimeUnit.MILLISECONDS);

		// Assert that the test completed
		Assertions.assertTrue(earlyFinish, "Read operation should have completed.");
	}


	// Test that a client socket can read data from a server socket
	@Test
	void testClientInputStreamRead() throws InterruptedException, IllegalArgumentException {
		// Latch to synchronize the client and server threads
		CountDownLatch readCompletedLatch = new CountDownLatch(1);

		// Start a new thread for accepting client connections
		Thread serverThread = new Thread(() -> {
			// Accept a connection from a client
			Optional<Socket> optSocket = ServerSocketUtils.acceptConnection(server);

			// Check that the socket exists
			Assertions.assertTrue(optSocket.isPresent(), "Client socket should exist but doesn't");

			// Get the client socket
			Socket socket = optSocket.get();

			// Verify that the client is connected
			Assertions.assertTrue(socket.isConnected(), "Client should be connected but wasn't");

			// Verify that write did not fail
			Assertions.assertDoesNotThrow(() -> {
				OutputStream stream = socket.getOutputStream();
				// Write a single byte to the output stream to avoid a timeout
				stream.write(VALID_BYTE);
				// Close the output socket
				stream.close();
			});

			// Close the client socket
			boolean clientSuccess = ClientSocketUtils.closeConnection(socket);

			// Check that the close was successful
			Assertions.assertTrue(clientSuccess, "Client socket should have closed but didn't");
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
			Assertions.assertTrue(socket.isConnected(), "Client should be connected but wasn't");

			// Attempt to get the InputStream from the client socket.
			Optional<InputStream> optStream = InputStreamUtils.getInputStream(socket);

			// Verify that the stream exists
			Assertions.assertTrue(optStream.isPresent(), "Client input stream should exist but doesn't");

			// Get the input stream for the client socket
			InputStream stream = optStream.get();

			// Verify that no exception is thrown
			Assertions.assertDoesNotThrow(() -> {
				// Read from the stream
				byte[] bytes = InputStreamUtils.readInputStream(stream);

				// Verify that only one byte was written
				Assertions.assertEquals(1, bytes.length, "More than one byte was read from client socket input stream");

				// Get the first byte
				byte data = bytes[0];

				// Verify that the correct byte was written
				Assertions.assertEquals(VALID_BYTE, data, "Incorrect byte was written to client socket");
			});

			// Close the input stream
			boolean streamClosed = InputStreamUtils.closeInputStream(stream);

			// Verify that the stream was closed
			Assertions.assertTrue(streamClosed, "Client input stream should be closed but wasn't");

			// Count down the latch after the read attempt
			readCompletedLatch.countDown();

			// Close the client socket
			boolean clientSuccess = ClientSocketUtils.closeConnection(socket);

			// Check that the close was successful
			Assertions.assertTrue(clientSuccess, "Client socket should have closed but " + "didn't");
		});

		// Start the server thread
		serverThread.start();

		// Start the client thread
		clientThread.start();

		// Wait for the client to read and finish the test
		boolean earlyFinish = readCompletedLatch.await(5000, TimeUnit.MILLISECONDS);

		// Assert that the test completed
		Assertions.assertTrue(earlyFinish, "Read operation should have completed.");
	}// Test that a server-side client socket can read data from a client
	@Test
	void testServerSideIOException() throws InterruptedException, IllegalArgumentException {
		// Latch to synchronize the client and server threads
		CountDownLatch readCompletedLatch = new CountDownLatch(1);

		// Start a new thread for accepting client connections
		Thread serverThread = new Thread(() -> {
			// Accept a connection from a client
			Optional<Socket> optSocket = ServerSocketUtils.acceptConnection(server);

			// Check that the socket exists
			Assertions.assertTrue(optSocket.isPresent(), "Client socket should exist but doesn't");

			// Get the client socket
			Socket socket = optSocket.get();

			// Verify that the client is connected
			Assertions.assertTrue(socket.isConnected(), "Client should be connected but wasn't");

			// Attempt to get the InputStream from the client socket.
			Optional<InputStream> optStream = InputStreamUtils.getInputStream(socket);

			// Verify that the stream exists
			Assertions.assertTrue(optStream.isPresent(), "Client input stream should exist but doesn't");

			// Get the input stream for the client socket
			InputStream stream = optStream.get();

			// Close the input stream
			boolean streamClosed = InputStreamUtils.closeInputStream(stream);

			// Verify that the stream was closed
			Assertions.assertTrue(streamClosed, "Client input stream should be closed but wasn't");

			// Verify that no exception is thrown
			Assertions.assertDoesNotThrow(() -> {
				// Read from the stream
				byte[] bytes = InputStreamUtils.readInputStream(stream);

				// Verify that only one byte was written
				Assertions.assertEquals(0, bytes.length, "Zero bytes should be read from server-side client socket "
						+ "input stream");
			});

			// Count down the latch after the read attempt
			readCompletedLatch.countDown();

			// Close the client socket
			boolean serverClientClosed = ClientSocketUtils.closeConnection(socket);

			// Check that the close was successful
			Assertions.assertTrue(serverClientClosed, "Server-side client socket should have closed but didn't");
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
			Assertions.assertTrue(socket.isConnected(), "Client should be connected but wasn't");

			// Close the client socket
			boolean clientClosed = ClientSocketUtils.closeConnection(socket);

			// Check that the close was successful
			Assertions.assertTrue(clientClosed, "Client socket should have closed but didn't");
		});

		// Start the server thread
		serverThread.start();

		// Start the client thread
		clientThread.start();

		// Wait for the server to read and finish the test
		boolean earlyFinish = readCompletedLatch.await(1000, TimeUnit.MILLISECONDS);

		// Assert that the test completed
		Assertions.assertTrue(earlyFinish, "Read operation should have completed.");
	}


	// Test that a client socket handles an already closed input stream
	@Test
	void testIOExceptionOnClient() throws IllegalArgumentException {
		// Latch to synchronize the client and server threads
		CountDownLatch readCompletedLatch = new CountDownLatch(1);

		// Accept a connection from a client
		Optional<Socket> optSocket = ClientSocketUtils.createSocket(VALID_HOST, VALID_PORT);

		// Check that the socket exists
		Assertions.assertTrue(optSocket.isPresent(), "Client socket should exist but doesn't");

		// Get the client socket
		Socket socket = optSocket.get();

		// Verify that the client is connected
		Assertions.assertTrue(socket.isConnected(), "Client should be connected but wasn't");

		// Attempt to get the InputStream from the client socket.
		Optional<InputStream> optStream = InputStreamUtils.getInputStream(socket);

		// Verify that the stream exists
		Assertions.assertTrue(optStream.isPresent(), "Client input stream should exist but doesn't");

		// Get the input stream for the client socket
		InputStream stream = optStream.get();

		// Close the input stream
		boolean streamClosed = InputStreamUtils.closeInputStream(stream);

		// Verify that the stream was closed
		Assertions.assertTrue(streamClosed, "Client input stream should be closed but wasn't");

		// Verify that no exception is thrown
		Assertions.assertDoesNotThrow(() -> {
			// Read from the stream
			byte[] bytes = InputStreamUtils.readInputStream(stream);

			// Verify that only one byte was written
			Assertions.assertEquals(0, bytes.length, "Zero bytes should be read from client socket input stream");
		});

		// Count down the latch after the read attempt
		readCompletedLatch.countDown();

		// Close the client socket
		boolean clientSuccess = ClientSocketUtils.closeConnection(socket);

		// Check that the close was successful
		Assertions.assertTrue(clientSuccess, "Client socket should have closed but " + "didn't");
	}
}
