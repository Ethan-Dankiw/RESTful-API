package socket.stream.input;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import net.ethandankiw.socket.ClientSocketUtils;
import net.ethandankiw.socket.InputStreamUtils;
import net.ethandankiw.socket.ServerSocketUtils;

class GetInputStreamTest {

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


	// Test a valid fetch of the client socket input stream
	@Test
	void testFetchValidInputStream() throws InterruptedException {
		// Latch to synchronize the client and server threads
		CountDownLatch clientConnectedLatch = new CountDownLatch(1);
		AtomicReference<Socket> clientSocketRef = new AtomicReference<>();

		// Start a new server thread for accepting client connections
		Thread serverThread = new Thread(() -> {
			// Accept a connection from a client
			Optional<Socket> optSocket = ServerSocketUtils.acceptConnection(server);

			// Check that the socket exists
			Assertions.assertTrue(optSocket.isPresent(), "Client socket should exist but doesn't");

			// Get the client socket
			Socket socket = optSocket.get();

			// Verify that the client is connected
			Assertions.assertTrue(socket.isConnected(), "Client should be connected");

			// Store the server side client socket
			clientSocketRef.set(socket);
			clientConnectedLatch.countDown();
		});

		// Start the server thread
		serverThread.start();

		// Create a connection to the server
		Optional<Socket> optSocket = ClientSocketUtils.createSocket(VALID_HOST, VALID_PORT);

		// Wait for the client and server to connect
		boolean earlyFinish = clientConnectedLatch.await(5, TimeUnit.SECONDS);

		// Assert that the timeout did not occur
		Assertions.assertTrue(earlyFinish, "Creating a connection to the server should not have timed out");

		// Check that the socket exists
		Assertions.assertTrue(optSocket.isPresent(), "Client socket should exist but doesn't");

		// Get the client socket
		Socket socket = optSocket.get();

		// Verify that the client is connected
		boolean connected = socket.isConnected();
		Assertions.assertTrue(connected, "Client should be connected");

		// Attempt to get the InputStream from the client socket.
		Optional<InputStream> optStream = InputStreamUtils.getInputStream(socket);

		// Verify that the stream exists
		Assertions.assertTrue(optStream.isPresent(), "Client input stream should exist but doesn't");

		// Close the client socket
		boolean clientSuccess = ClientSocketUtils.closeConnection(socket);

		// Check that the close was successful
		Assertions.assertTrue(clientSuccess, "Client socket should have closed but didn't");

		// Attempt to close the server side client socket
		boolean success = ClientSocketUtils.closeConnection(clientSocketRef.get());

		// Check if the connection was closed
		Assertions.assertTrue(success, "Server-side client socket should have closed but didn't");
	}


	// Test an invalid fetch of the input stream for a closed client socket
	@Test
	void testInvalidConnection() {
		// Create a connection to the server
		Optional<Socket> optSocket = ClientSocketUtils.createSocket(VALID_HOST, VALID_PORT);

		// Check that the socket exists
		Assertions.assertTrue(optSocket.isPresent(), "Client socket should exist but doesn't");

		// Get the client socket
		Socket socket = optSocket.get();

		// Verify that the client is connected
		boolean connected = socket.isConnected();
		Assertions.assertTrue(connected, "Client should be connected");

		// Close the client socket
		boolean clientSuccess = ClientSocketUtils.closeConnection(socket);

		// Check that the close was successful
		Assertions.assertTrue(clientSuccess, "Client socket should have closed but didn't");

		// Attempt to get the InputStream from the closed socket
		Optional<InputStream> optStream = InputStreamUtils.getInputStream(socket);

		// Assert that the stream is not present
		Assertions.assertTrue(optStream.isEmpty(), "Input stream should not exist for a closed connection");
	}


	// Test an invalid fetch of the closed client socket input stream
	@Test
	void testFetchClosedInputStream() {
		// Create a connection to the server
		Optional<Socket> optSocket = ClientSocketUtils.createSocket(VALID_HOST, VALID_PORT);

		// Check that the socket exists
		Assertions.assertTrue(optSocket.isPresent(), "Client socket should exist but doesn't");

		// Get the client socket
		Socket socket = optSocket.get();

		// Verify that the client is connected
		boolean connected = socket.isConnected();
		Assertions.assertTrue(connected, "Client should be connected");

		// Attempt to get the input stream from the client socket
		Optional<InputStream> optStream = InputStreamUtils.getInputStream(socket);

		// Verify that the stream exists
		Assertions.assertTrue(optStream.isPresent(), "Client input stream should exist but doesn't");

		// Get the input stream
		InputStream stream = optStream.get();

		// Close the input stream
		boolean streamClosed = InputStreamUtils.closeInputStream(stream);

		// Verify that the stream was closed
		Assertions.assertTrue(streamClosed, "Client input stream should be closed but wasn't");

		// Attempt to get the closed input stream from the client socket
		Optional<InputStream> optClosedStream = InputStreamUtils.getInputStream(socket);

		// Verify that the stream exists
		Assertions.assertTrue(optClosedStream.isEmpty(), "Client input stream should not exist but does");

		// Close the client socket
		boolean clientSuccess = ClientSocketUtils.closeConnection(socket);

		// Check that the close was successful
		Assertions.assertTrue(clientSuccess, "Client socket should have closed but didn't");
	}


	// Test the IO exception
	@Test
	void testIOException() throws IOException {
		// Mock the socket class
		Socket socket = Mockito.mock(Socket.class);

		// When the input stream is fetched from the socket, throw a simulated IO exception
		Mockito.doThrow(new IOException("Simulated I/O error"))
			   .when(socket)
			   .getInputStream();

		// Attempt to close the mocked input stream
		Optional<InputStream> optStream = InputStreamUtils.getInputStream(socket);

		// Verify that the input stream does not exist
		Assertions.assertTrue(optStream.isEmpty(), "Input stream should not exist but does");
	}
}
