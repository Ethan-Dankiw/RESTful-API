package socket.stream.output;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

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

class CloseOutputStreamTest {

	// Host for client socket creation
	private static final String VALID_HOST = "localhost";

	// Port for the server used by these tests
	private static final Integer VALID_PORT = 8083;

	// ServerSocket instance for tests
	private ServerSocket server = null;

	// Create the server before each test
	@BeforeEach
	void setup() {
		// Create ServerSocket using project wrapper
		Optional<ServerSocket> optSocket = ServerSocketUtils.createSocket(VALID_PORT);

		// Ensure creation succeeded
		Assertions.assertTrue(optSocket.isPresent(), "Server socket does not exist when it should");

		// Store the server
		server = optSocket.get();
	}

	// Close the server after each test
	@AfterEach
	void teardown() {
		// Close the server socket using wrapper
		boolean success = ServerSocketUtils.closeConnection(server);

		// Verify close succeeded
		Assertions.assertTrue(success, "Unable to close server connection");

		// Null out reference
		server = null;
	}

	// Test closing an OutputStream obtained from the server-side accepted socket
	@Test
	void testServerSideCloseOutputStream() throws InterruptedException {
		// Latch to coordinate the threads
		CountDownLatch latch = new CountDownLatch(1);

		// Server thread: accept then close the accepted socket's OutputStream
		Thread serverThread = new Thread(() -> {
			// Accept client connection
			Optional<Socket> optSocket = ServerSocketUtils.acceptConnection(server);

			// Ensure accepted socket exists
			Assertions.assertTrue(optSocket.isPresent(), "Client socket should exist but doesn't");

			// Extract the accepted socket
			Socket socket = optSocket.get();

			// Obtain the OutputStream via the utility
			Optional<OutputStream> optStream = OutputStreamUtils.getOutputStream(socket);

			// Ensure a stream was returned
			Assertions.assertTrue(optStream.isPresent(), "Output stream should exist but doesn't");

			// Close the output stream via the utility and assert it returns true
			boolean closed = OutputStreamUtils.closeOutputStream(optStream.get());
			Assertions.assertTrue(closed, "Output stream should be closed");

			// Signal completion
			latch.countDown();

			// Close accepted socket
			boolean clientClosed = ClientSocketUtils.closeConnection(socket);
			Assertions.assertTrue(clientClosed, "Server-side client socket should have closed but didn't");
		});

		// Client thread: create a client connection so server can accept
		Thread clientThread = new Thread(() -> {
			Optional<Socket> optSocket = ClientSocketUtils.createSocket(VALID_HOST, VALID_PORT);
			Assertions.assertTrue(optSocket.isPresent(), "Client socket should exist but doesn't");
			boolean closed = ClientSocketUtils.closeConnection(optSocket.get());
			Assertions.assertTrue(closed, "Client socket should have closed but didn't");
		});

		// Start threads
		serverThread.start();
		clientThread.start();

		// Wait for server thread completion
		boolean completed = latch.await(1000, TimeUnit.MILLISECONDS);
		Assertions.assertTrue(completed, "Close output stream operation should have completed.");
	}

	// Test closing an OutputStream on the client-side
	@Test
	void testClientSideCloseOutputStream() {
		// Create and verify a client socket
		Optional<Socket> optSocket = ClientSocketUtils.createSocket(VALID_HOST, VALID_PORT);
		Assertions.assertTrue(optSocket.isPresent(), "Client socket should exist but doesn't");

		// Extract the socket
		Socket socket = optSocket.get();

		// Obtain the client's OutputStream using the utility
		Optional<OutputStream> optStream = OutputStreamUtils.getOutputStream(socket);

		// Ensure we got the stream
		Assertions.assertTrue(optStream.isPresent(), "Output stream should exist but doesn't");

		// Close the output stream via the utility and check true is returned
		boolean closed = OutputStreamUtils.closeOutputStream(optStream.get());
		Assertions.assertTrue(closed, "Output stream should be closed");

		// Close the socket as cleanup
		boolean socketClosed = ClientSocketUtils.closeConnection(socket);
		Assertions.assertTrue(socketClosed, "Client socket should have closed but didn't");
	}

	// Test closing an already closed OutputStream — should return true (close() is idempotent)
	@Test
	void testCloseAlreadyClosedOutputStream() throws IOException {
		// Create a client socket and ensure it exists
		Optional<Socket> optSocket = ClientSocketUtils.createSocket(VALID_HOST, VALID_PORT);
		Assertions.assertTrue(optSocket.isPresent(), "Client socket should exist but doesn't");

		// Get the socket
		Socket socket = optSocket.get();

		// Get the socket's OutputStream directly
		OutputStream out = socket.getOutputStream();

		// Close the raw OutputStream directly to simulate "already closed"
		out.close();

		// Now call the utility close method on the already-closed stream; utility should succeed
		boolean closed = OutputStreamUtils.closeOutputStream(out);
		Assertions.assertTrue(closed, "Closing an already closed stream should still return true");

		// Cleanup socket
		boolean socketClosed = ClientSocketUtils.closeConnection(socket);
		Assertions.assertTrue(socketClosed, "Client socket should have closed but didn't");
	}

	// Test that attempting to get/close an OutputStream for a closed socket results in empty optional/cleanup
	@Test
	void testCloseOutputStreamForClosedSocket() throws Exception {
		// Create and connect a client socket
		Optional<Socket> optSocket = ClientSocketUtils.createSocket(VALID_HOST, VALID_PORT);
		Assertions.assertTrue(optSocket.isPresent(), "Client socket should exist but doesn't");

		// Grab the socket and then close it to simulate the closed-socket scenario
		Socket socket = optSocket.get();
		socket.close();

		// Attempt to retrieve the OutputStream for the closed socket via utility
		Optional<OutputStream> optStream = OutputStreamUtils.getOutputStream(socket);

		// The utility should return Optional.empty() when the socket is closed
		Assertions.assertTrue(optStream.isEmpty(), "Output stream should be empty for closed socket");
	}

	// Test that an IOException thrown by OutputStream.close() results in the utility returning false
	@Test
	void testIOExceptionOnClose() throws IOException {
		// Create a mock OutputStream that throws an IOException when close() is called
		OutputStream mockOut = mock(OutputStream.class);

		// Configure the mock to throw on close()
		doThrow(new IOException("fail")).when(mockOut).close();

		// Attempt to close the mock via the utility — should return false due to the IOException
		boolean closed = OutputStreamUtils.closeOutputStream(mockOut);
		Assertions.assertFalse(closed, "IOException should return false on close");
	}
}
