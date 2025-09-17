package socket.stream.output;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
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
import net.ethandankiw.socket.OutputStreamUtils;
import net.ethandankiw.socket.ServerSocketUtils;

class GetOutputStreamTest {

	// Host for clients to connect to the server
	private static final String VALID_HOST = "localhost";

	// Port the ServerSocket will listen on for these tests
	private static final Integer VALID_PORT = 8081;

	// ServerSocket instance used by each test and created in @BeforeEach
	private ServerSocket server = null;


	// Create the server before each test so the port is bound and ready
	@BeforeEach
	void setup() {
		// Attempt to create a server socket using the project's ServerSocketUtils wrapper
		Optional<ServerSocket> optSocket = ServerSocketUtils.createSocket(VALID_PORT);

		// Fail the test immediately if server creation failed
		Assertions.assertTrue(optSocket.isPresent(), "Server socket does not exist when it should");

		// Store the server socket for use in the test
		server = optSocket.get();
	}


	// Close and wipe the server after each test so ports free up for other tests
	@AfterEach
	void teardown() {
		// Try to close the server socket using the project's wrapper
		boolean success = ServerSocketUtils.closeConnection(server);

		// Verify the server socket closed successfully
		Assertions.assertTrue(success, "Unable to close server connection");

		// Clear the reference for safety
		server = null;
	}


	// Test that the server-side accepted client socket can provide an OutputStream
	@Test
	void testServerSideClientGetOutputStream() throws InterruptedException {
		// Latch used to block until server thread has verified the stream
		CountDownLatch latch = new CountDownLatch(1);

		// Server-side thread: accept the connection and retrieve its OutputStream
		Thread serverThread = new Thread(() -> {
			// Accept a pending connection on the server
			Optional<Socket> optSocket = ServerSocketUtils.acceptConnection(server);

			// Ensure the accept() returned a socket
			Assertions.assertTrue(optSocket.isPresent(), "Client socket should exist but doesn't");

			// Grab the accepted socket
			Socket socket = optSocket.get();

			// Ensure the socket reports it's connected
			Assertions.assertTrue(socket.isConnected(), "Client should be connected but wasn't");

			// Attempt to get the output stream via the utility being tested
			Optional<OutputStream> optStream = OutputStreamUtils.getOutputStream(socket);

			// Verify the returned Optional contains a stream (happy path)
			Assertions.assertTrue(optStream.isPresent(), "Output stream should exist but doesn't");

			// Signal the main test thread that the server-side check is complete
			latch.countDown();

			// Close the accepted connection using the project's ClientSocketUtils wrapper
			boolean closed = ClientSocketUtils.closeConnection(socket);

			// Ensure the server-side client socket close succeeded
			Assertions.assertTrue(closed, "Server-side client socket should have closed but didn't");
		});

		// Client-side thread: connect to the server so the server can accept
		Thread clientThread = new Thread(() -> {
			// Create a client socket and connect to our server
			Optional<Socket> optSocket = ClientSocketUtils.createSocket(VALID_HOST, VALID_PORT);

			// Validate the client socket was created
			Assertions.assertTrue(optSocket.isPresent(), "Client socket should exist but doesn't");

			// Grab the client socket
			Socket socket = optSocket.get();

			// Ensure it is connected
			Assertions.assertTrue(socket.isConnected(), "Client should be connected but wasn't");

			// Close the client socket (server thread will still have its accepted socket)
			boolean clientClosed = ClientSocketUtils.closeConnection(socket);

			// Verify the client closed cleanly
			Assertions.assertTrue(clientClosed, "Client socket should have closed but didn't");
		});

		// Start server thread to wait for the client
		serverThread.start();

		// Start client thread to connect to the server
		clientThread.start();

		// Wait up to 1s for the server thread to confirm it obtained the stream
		boolean completed = latch.await(1000, TimeUnit.MILLISECONDS);

		// Verify the server-side path completed before the timeout
		Assertions.assertTrue(completed, "Get output stream operation should have completed.");
	}


	// Test retrieving an OutputStream from the client-side socket (no server-side action required)
	@Test
	void testClientGetOutputStream() {
		// Create a client socket and connect to the already-listening server
		Optional<Socket> optSocket = ClientSocketUtils.createSocket(VALID_HOST, VALID_PORT);

		// Ensure the client socket was created
		Assertions.assertTrue(optSocket.isPresent(), "Client socket should exist but doesn't");

		// Extract the socket
		Socket socket = optSocket.get();

		// Confirm the socket reports it is connected
		Assertions.assertTrue(socket.isConnected(), "Client should be connected but wasn't");

		// Use the utility to obtain the OutputStream for the client socket
		Optional<OutputStream> optStream = OutputStreamUtils.getOutputStream(socket);

		// The stream should be present in the normal case
		Assertions.assertTrue(optStream.isPresent(), "Output stream should exist but doesn't");

		// Close the client socket as cleanup
		boolean closed = ClientSocketUtils.closeConnection(socket);

		// Confirm the client socket closed
		Assertions.assertTrue(closed, "Client socket should have closed but didn't");
	}


	// Test that asking for an OutputStream on a closed socket returns an empty Optional
	@Test
	void testClosedSocketGetOutputStream() throws Exception {
		// Create and connect a client socket to the server
		Optional<Socket> optSocket = ClientSocketUtils.createSocket(VALID_HOST, VALID_PORT);

		// Ensure creation succeeded
		Assertions.assertTrue(optSocket.isPresent(), "Client socket should exist but doesn't");

		// Grab the client socket
		Socket socket = optSocket.get();

		// Close the client socket to simulate a "closed socket" scenario
		socket.close();

		// Try to obtain an OutputStream using the utility; closed socket should not produce one
		Optional<OutputStream> optStream = OutputStreamUtils.getOutputStream(socket);

		// Validate the returned Optional is empty when the socket is closed
		Assertions.assertTrue(optStream.isEmpty(), "Output stream should be empty for closed socket");
	}


	// Test that if socket.getOutputStream() throws an IOException the utility returns Optional.empty()
	@Test
	void testGetOutputStreamIOException() throws IOException {
		// Create a Mockito mock of Socket
		Socket mockSocket = mock(Socket.class);

		// Configure the mock to throw IOException when getOutputStream() is called
		when(mockSocket.getOutputStream()).thenThrow(new IOException("simulated IO failure"));

		// Call the utility which should catch the exception and return Optional.empty()
		Optional<OutputStream> optStream = OutputStreamUtils.getOutputStream(mockSocket);

		// Assert that the utility returned an empty Optional in this error case
		Assertions.assertTrue(optStream.isEmpty(), "Output stream should be empty when getOutputStream throws IOException");
	}


	// Test retrieving an OutputStream, closing that stream, and ensuring close succeeds (closed output stream case)
	@Test
	void testGetThenCloseOutputStream() {
		// Create a client socket connected to the server
		Optional<Socket> optSocket = ClientSocketUtils.createSocket(VALID_HOST, VALID_PORT);

		// Ensure the socket exists
		Assertions.assertTrue(optSocket.isPresent(), "Client socket should exist but doesn't");

		// Grab the socket
		Socket socket = optSocket.get();

		// Obtain its OutputStream using the utility
		Optional<OutputStream> optStream = OutputStreamUtils.getOutputStream(socket);

		// Ensure we got a stream
		Assertions.assertTrue(optStream.isPresent(), "Output stream should exist but doesn't");

		// Close the output stream via the utility and check it returns true
		boolean closed = OutputStreamUtils.closeOutputStream(optStream.get());
		Assertions.assertTrue(closed, "Closing the obtained output stream should succeed");

		// Cleanup: close the connected socket
		boolean socketClosed = ClientSocketUtils.closeConnection(socket);
		Assertions.assertTrue(socketClosed, "Client socket should have closed but didn't");
	}
}
