package socket.stream.input;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import net.ethandankiw.socket.ClientSocketUtils;
import net.ethandankiw.socket.InputStreamUtils;
import net.ethandankiw.socket.ServerSocketUtils;

class CloseInputStreamTest {

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


	// Test that the input stream can be closed successfully
	@Test
	void testValidInputStreamClose() {
		// Create a mock InputStream
		InputStream mockStream = Mockito.mock(InputStream.class);

		// Attempt to close the mock stream
		boolean success = InputStreamUtils.closeInputStream(mockStream);

		// Verify that the stream was successfully closed
		Assertions.assertTrue(success, "Input stream should have been closed but wasn't");
	}


	// Test that closing the server-side client's input stream unblocks the client's read operation
	@Test
	void testServerSideCloseStopsClientRead() throws InterruptedException {
		AtomicReference<Socket> clientSocketRef = new AtomicReference<>();

		// Start a server thread to accept a connection and close the input stream
		Thread serverThread = new Thread(() -> {
			// Accept a client connection
			Optional<Socket> optClientSocket = ServerSocketUtils.acceptConnection(server);
			Assertions.assertTrue(optClientSocket.isPresent(), "Server failed to accept client connection.");

			Socket serverSideClientSocket = optClientSocket.get();
			Assertions.assertTrue(serverSideClientSocket.isConnected(), "Client should be connected");

			// Get the input stream and close it
			Optional<InputStream> optStream = InputStreamUtils.getInputStream(serverSideClientSocket);
			Assertions.assertTrue(optStream.isPresent(), "Input stream should exist");
			InputStream stream = optStream.get();

			// Close the stream, which should unblock the client's read operation
			boolean closeSuccess = InputStreamUtils.closeInputStream(stream);
			Assertions.assertTrue(closeSuccess, "Stream should have closed successfully");


			// Close the client socket
			boolean clientSuccess = ClientSocketUtils.closeConnection(serverSideClientSocket);

			// Check that the close was successful
			Assertions.assertTrue(clientSuccess, "Client socket should have closed but didn't");
		});

		// Start a client thread to connect and perform a blocking read
		Thread clientThread = new Thread(() -> {
			Optional<Socket> optSocket = ClientSocketUtils.createSocket(VALID_HOST, VALID_PORT);
			Assertions.assertTrue(optSocket.isPresent(), "Client socket should exist");
			Socket socket = optSocket.get();

			// Store the client socket to close in teardown
			clientSocketRef.set(socket);

			// Get the input stream and attempt to read
			Optional<InputStream> optStream = InputStreamUtils.getInputStream(socket);
			Assertions.assertTrue(optStream.isPresent(), "Input stream should exist");
			InputStream stream = optStream.get();

			// Blocking read operation, should be interrupted by the server closing the stream
			Assertions.assertDoesNotThrow(() -> {
				InputStreamUtils.readInputStream(stream);
			});

			// Close the client socket
			boolean clientSuccess = ClientSocketUtils.closeConnection(socket);

			// Check that the close was successful
			Assertions.assertTrue(clientSuccess, "Client socket should have closed but didn't");
		});

		serverThread.start();
		clientThread.start();

		// Wait for the test to complete
		serverThread.join(1000);
		clientThread.join(1000);

		Assertions.assertFalse(serverThread.isAlive(), "Server thread should have finished");
		Assertions.assertFalse(clientThread.isAlive(), "Client thread should have finished");


		// Close the client socket
		boolean clientSuccess = ClientSocketUtils.closeConnection(clientSocketRef.get());

		// Check that the close was successful
		Assertions.assertTrue(clientSuccess, "Client socket should have closed but didn't");
	}


	// Test that closing the client's input stream stops the server's blocking read operation
	@Test
	void testClientCloseStopsServerRead() throws InterruptedException {
		AtomicReference<Socket> clientSocketRef = new AtomicReference<>();

		// Start a server thread to accept a connection and perform a blocking read
		Thread serverThread = new Thread(() -> {
			Optional<Socket> optClientSocket = ServerSocketUtils.acceptConnection(server);
			Assertions.assertTrue(optClientSocket.isPresent(), "Server failed to accept client connection.");

			Socket serverSideClientSocket = optClientSocket.get();
			Assertions.assertTrue(serverSideClientSocket.isConnected(), "Client should be connected");

			// Get the input stream
			Optional<InputStream> optStream = InputStreamUtils.getInputStream(serverSideClientSocket);
			Assertions.assertTrue(optStream.isPresent(), "Input stream should exist");
			InputStream stream = optStream.get();

			// Blocking read operation, should be interrupted by the client closing the stream
			Assertions.assertDoesNotThrow(() -> {
				InputStreamUtils.readInputStream(stream);
			});

			// Close the socket to clean up resources
			boolean clientSuccess = ClientSocketUtils.closeConnection(serverSideClientSocket);

			// Check that the close was successful
			Assertions.assertTrue(clientSuccess, "Client socket should have closed but didn't");
		});

		// Start a client thread to connect and close its own input stream
		Thread clientThread = new Thread(() -> {
			Optional<Socket> optSocket = ClientSocketUtils.createSocket(VALID_HOST, VALID_PORT);
			Assertions.assertTrue(optSocket.isPresent(), "Client socket should exist");
			Socket socket = optSocket.get();

			clientSocketRef.set(socket);

			// Close the client socket
			boolean clientSuccess = ClientSocketUtils.closeConnection(socket);

			// Check that the close was successful
			Assertions.assertTrue(clientSuccess, "Client socket should have closed but didn't");
		});

		serverThread.start();
		clientThread.start();

		serverThread.join(1000);
		clientThread.join(1000);

		Assertions.assertFalse(serverThread.isAlive(), "Server thread should have finished");
		Assertions.assertFalse(clientThread.isAlive(), "Client thread should have finished");

		// Close the client socket
		boolean clientSuccess = ClientSocketUtils.closeConnection(clientSocketRef.get());

		// Check that the close was successful
		Assertions.assertTrue(clientSuccess, "Client socket should have closed but didn't");
	}


	// Test that closing an already closed input stream causes an IOException
	@Test
	void testDuplicateCloseCausesIOException() throws IOException {
		// Create a mock InputStream
		InputStream mockStream = Mockito.mock(InputStream.class);

		// Mock the close() method to throw an IOException on a duplicate call
		Mockito.doNothing().doThrow(new IOException("Simulated duplicate close error")).when(mockStream).close();

		// Attempt to close the stream for the first time
		boolean firstCloseSuccess = InputStreamUtils.closeInputStream(mockStream);
		Assertions.assertTrue(firstCloseSuccess, "First close should have been successful");

		// Attempt to close the stream again, which should fail
		boolean secondCloseSuccess = InputStreamUtils.closeInputStream(mockStream);
		Assertions.assertFalse(secondCloseSuccess, "Second close should have failed with IOException");
	}
}