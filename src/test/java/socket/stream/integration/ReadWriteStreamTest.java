package socket.stream.integration;

import java.io.InputStream;
import java.io.OutputStream;
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
import net.ethandankiw.socket.InputStreamUtils;
import net.ethandankiw.socket.ServerSocketUtils;

/**
 * Integration tests to verify bidirectional communication between a client socket and a server-side client socket.
 * These tests check that: - The client can write to the server-side socket and the server can read it - The server-side
 * socket can write back to the client and the client can read it
 */
class ReadWriteStreamTest {

	// The byte value used for writing from client to server
	private static final int CLIENT_TO_SERVER_BYTE = 77;

	// The byte value used for writing from server to client
	private static final int SERVER_TO_CLIENT_BYTE = 88;

	// Host used for client socket creation
	private static final String VALID_HOST = "localhost";

	// Port used for server socket creation
	private static final int VALID_PORT = 9090;

	// Server socket instance
	private ServerSocket server = null;


	/**
	 * Before each test, create a server socket listening on VALID_PORT.
	 */
	@BeforeEach
	void setup() {
		// Attempt to create a server socket
		Optional<ServerSocket> optServer = ServerSocketUtils.createSocket(VALID_PORT);

		// Assert that the server socket was created
		Assertions.assertTrue(optServer.isPresent(), "Server socket was not created");

		// Store the server socket
		server = optServer.get();
	}


	/**
	 * After each test, close the server socket.
	 */
	@AfterEach
	void teardown() {
		// Attempt to close the server socket
		boolean closed = ServerSocketUtils.closeConnection(server);

		// Verify that the server socket closed successfully
		Assertions.assertTrue(closed, "Server socket was not closed");

		// Clear reference
		server = null;
	}


	/**
	 * Test that a client can write data to a server-side socket and the server-side socket can successfully read it.
	 */
	@Test
	void testClientWritingToServerAndServerReadingFromClient() throws InterruptedException {
		// Latch to synchronise the completion of the read operation
		CountDownLatch latch = new CountDownLatch(1);

		// Start the server thread to accept a connection and read data
		Thread serverThread = new Thread(() -> {
			// Accept the incoming client connection
			Optional<Socket> optSocket = ServerSocketUtils.acceptConnection(server);

			// Assert that the socket is present
			Assertions.assertTrue(optSocket.isPresent(), "Server did not accept a client connection");

			// Retrieve the server-side client socket
			Socket serverClientSocket = optSocket.get();

			// Assert that the server-side client socket is connected
			Assertions.assertTrue(serverClientSocket.isConnected(), "Server-side client socket not connected");

			// Attempt to get the input stream from the server-side client socket
			Optional<InputStream> optInStream = InputStreamUtils.getInputStream(serverClientSocket);

			// Assert that the input stream exists
			Assertions.assertTrue(optInStream.isPresent(), "Server-side input stream does not exist");

			// Retrieve the input stream
			InputStream inStream = optInStream.get();

			// Assert no exception when reading
			Assertions.assertDoesNotThrow(() -> {
				// Read the bytes sent by the client
				byte[] bytes = InputStreamUtils.readInputStream(inStream);

				// Verify only one byte was read
				Assertions.assertEquals(1, bytes.length, "Server read an unexpected number of bytes");

				// Verify the correct byte was received
				Assertions.assertEquals(CLIENT_TO_SERVER_BYTE, bytes[0], "Incorrect byte received from client");
			});

			// Close the server-side client socket
			boolean closed = ClientSocketUtils.closeConnection(serverClientSocket);

			// Assert it closed successfully
			Assertions.assertTrue(closed, "Server-side client socket not closed");

			// Signal that read is complete
			latch.countDown();
		});

		// Start the client thread to write data to the server
		Thread clientThread = new Thread(() -> {
			// Create a client socket
			Optional<Socket> optSocket = ClientSocketUtils.createSocket(VALID_HOST, VALID_PORT);

			// Assert that the client socket was created
			Assertions.assertTrue(optSocket.isPresent(), "Client socket not created");

			// Retrieve the client socket
			Socket clientSocket = optSocket.get();

			// Assert that the client socket is connected
			Assertions.assertTrue(clientSocket.isConnected(), "Client socket not connected");

			// Assert no exception when writing
			Assertions.assertDoesNotThrow(() -> {
				// Retrieve the output stream from the client socket
				OutputStream outStream = clientSocket.getOutputStream();

				// Write a single byte to the server
				outStream.write(CLIENT_TO_SERVER_BYTE);

				// Close the output stream to signal end of transmission
				outStream.close();
			});

			// Close the client socket
			boolean closed = ClientSocketUtils.closeConnection(clientSocket);

			// Assert it closed successfully
			Assertions.assertTrue(closed, "Client socket not closed");
		});

		// Start both threads
		serverThread.start();
		clientThread.start();

		// Wait for the latch to ensure server read is complete
		boolean completed = latch.await(2000, TimeUnit.MILLISECONDS);

		// Assert that the operation completed
		Assertions.assertTrue(completed, "Server did not complete reading from client");
	}


	/**
	 * Test that the server-side socket can write data back to the client and the client can successfully read it.
	 */
	@Test
	void testServerWritingToClientAndClientReadingFromServer() throws InterruptedException {
		// Latch to synchronise the completion of the read operation
		CountDownLatch latch = new CountDownLatch(1);

		// Start the server thread to accept connection and write data
		Thread serverThread = new Thread(() -> {
			// Accept the incoming client connection
			Optional<Socket> optSocket = ServerSocketUtils.acceptConnection(server);

			// Assert that the socket is present
			Assertions.assertTrue(optSocket.isPresent(), "Server did not accept a client connection");

			// Retrieve the server-side client socket
			Socket serverClientSocket = optSocket.get();

			// Assert that the server-side client socket is connected
			Assertions.assertTrue(serverClientSocket.isConnected(), "Server-side client socket not connected");

			// Assert no exception when writing
			Assertions.assertDoesNotThrow(() -> {
				// Retrieve the output stream from the server-side client socket
				OutputStream outStream = serverClientSocket.getOutputStream();

				// Write a single byte to the client
				outStream.write(SERVER_TO_CLIENT_BYTE);

				// Close the output stream
				outStream.close();
			});

			// Close the server-side client socket
			boolean closed = ClientSocketUtils.closeConnection(serverClientSocket);

			// Assert it closed successfully
			Assertions.assertTrue(closed, "Server-side client socket not closed");
		});

		// Start the client thread to read data from the server
		Thread clientThread = new Thread(() -> {
			// Create a client socket
			Optional<Socket> optSocket = ClientSocketUtils.createSocket(VALID_HOST, VALID_PORT);

			// Assert that the client socket was created
			Assertions.assertTrue(optSocket.isPresent(), "Client socket not created");

			// Retrieve the client socket
			Socket clientSocket = optSocket.get();

			// Assert that the client socket is connected
			Assertions.assertTrue(clientSocket.isConnected(), "Client socket not connected");

			// Attempt to get the input stream from the client socket
			Optional<InputStream> optInStream = InputStreamUtils.getInputStream(clientSocket);

			// Assert that the input stream exists
			Assertions.assertTrue(optInStream.isPresent(), "Client input stream does not exist");

			// Retrieve the input stream
			InputStream inStream = optInStream.get();

			// Assert no exception when reading
			Assertions.assertDoesNotThrow(() -> {
				// Read the bytes sent by the server
				byte[] bytes = InputStreamUtils.readInputStream(inStream);

				// Verify only one byte was read
				Assertions.assertEquals(1, bytes.length, "Client read an unexpected number of bytes");

				// Verify the correct byte was received
				Assertions.assertEquals(SERVER_TO_CLIENT_BYTE, bytes[0], "Incorrect byte received from server");
			});

			// Close the client socket
			boolean closed = ClientSocketUtils.closeConnection(clientSocket);

			// Assert it closed successfully
			Assertions.assertTrue(closed, "Client socket not closed");

			// Signal that read is complete
			latch.countDown();
		});

		// Start both threads
		serverThread.start();
		clientThread.start();

		// Wait for the latch to ensure client read is complete
		boolean completed = latch.await(2000, TimeUnit.MILLISECONDS);

		// Assert that the operation completed
		Assertions.assertTrue(completed, "Client did not complete reading from server");
	}
}
