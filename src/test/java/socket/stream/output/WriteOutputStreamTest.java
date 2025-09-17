package socket.stream.output;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
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

class WriteOutputStreamTest {

	// Host used by client socket creation
	private static final String VALID_HOST = "localhost";

	// Server port for these tests
	private static final Integer VALID_PORT = 8082;

	// Test string that will be written to streams in tests
	private static final String TEST_STRING = "hello";

	// ServerSocket created before each test
	private ServerSocket server = null;

	// Bind the server before each test so clients can connect
	@BeforeEach
	void setup() {
		// Create the ServerSocket via project wrapper
		Optional<ServerSocket> optSocket = ServerSocketUtils.createSocket(VALID_PORT);

		// Ensure server creation succeeded
		Assertions.assertTrue(optSocket.isPresent(), "Server socket does not exist when it should");

		// Store the server for the test
		server = optSocket.get();
	}

	// Close the server socket after every test
	@AfterEach
	void teardown() {
		// Use the project wrapper to close the server
		boolean success = ServerSocketUtils.closeConnection(server);

		// Expect the close to succeed
		Assertions.assertTrue(success, "Unable to close server connection");

		// Clear reference
		server = null;
	}

	// Test writing to a server-side client's OutputStream (server writes)
	@Test
	void testServerSideWriteOutputStream() throws InterruptedException {
		// Latch to coordinate completion
		CountDownLatch latch = new CountDownLatch(1);

		// Server thread: accept connection then write using the utility
		Thread serverThread = new Thread(() -> {
			// Accept the client's connection
			Optional<Socket> optSocket = ServerSocketUtils.acceptConnection(server);

			// Ensure we got the accepted socket
			Assertions.assertTrue(optSocket.isPresent(), "Client socket should exist but doesn't");

			// Extract the accepted socket
			Socket socket = optSocket.get();

			// Obtain the output stream for the accepted socket via utility
			Optional<OutputStream> optStream = OutputStreamUtils.getOutputStream(socket);

			// Ensure a stream is present
			Assertions.assertTrue(optStream.isPresent(), "Output stream should exist but doesn't");

			// Attempt to write to the stream and assert no exception is thrown
			Assertions.assertDoesNotThrow(() -> {
				// Perform the write using the utility under test
				int written = OutputStreamUtils.writeOutputStream(optStream.get(), TEST_STRING);

				// Verify the number of bytes written equals the UTF-8 length of the string
				Assertions.assertEquals(TEST_STRING.getBytes(StandardCharsets.UTF_8).length, written,
						"Incorrect number of bytes written");
			});

			// Signal completion
			latch.countDown();

			// Close the accepted socket via wrapper
			boolean closed = ClientSocketUtils.closeConnection(socket);
			Assertions.assertTrue(closed, "Server-side client socket should have closed but didn't");
		});

		// Client thread: create a client connection so the server can accept
		Thread clientThread = new Thread(() -> {
			// Create and verify a client socket
			Optional<Socket> optSocket = ClientSocketUtils.createSocket(VALID_HOST, VALID_PORT);
			Assertions.assertTrue(optSocket.isPresent(), "Client socket should exist but doesn't");

			// Close the client socket immediately; server will have accepted
			boolean clientClosed = ClientSocketUtils.closeConnection(optSocket.get());
			Assertions.assertTrue(clientClosed, "Client socket should have closed but didn't");
		});

		// Start the server and client threads
		serverThread.start();
		clientThread.start();

		// Wait for server thread to finish its check
		boolean completed = latch.await(1000, TimeUnit.MILLISECONDS);
		Assertions.assertTrue(completed, "Write operation should have completed.");
	}

	// Test writing from client-side OutputStream (client writes)
	@Test
	void testClientSideWriteOutputStream() throws InterruptedException {
		// Latch for synchronization
		CountDownLatch latch = new CountDownLatch(1);

		// Server thread: accept then do nothing (just so client can connect)
		Thread serverThread = new Thread(() -> {
			// Accept connection from client
			Optional<Socket> optSocket = ServerSocketUtils.acceptConnection(server);

			// Ensure accepted socket exists
			Assertions.assertTrue(optSocket.isPresent(), "Client socket should exist but doesn't");

			// Close the server-side accepted socket quickly
			boolean closed = ClientSocketUtils.closeConnection(optSocket.get());
			Assertions.assertTrue(closed, "Server-side client socket should have closed but didn't");

			// Notify the test that server finished accept (client may perform writing)
			latch.countDown();
		});

		// Client thread: connect and write using utility
		Thread clientThread = new Thread(() -> {
			// Create client socket and ensure it exists
			Optional<Socket> optSocket = ClientSocketUtils.createSocket(VALID_HOST, VALID_PORT);
			Assertions.assertTrue(optSocket.isPresent(), "Client socket should exist but doesn't");

			// Obtain the client's output stream via utility
			Optional<OutputStream> optStream = OutputStreamUtils.getOutputStream(optSocket.get());
			Assertions.assertTrue(optStream.isPresent(), "Output stream should exist but doesn't");

			// Try writing using the utility and verify no exception
			Assertions.assertDoesNotThrow(() -> {
				int written = OutputStreamUtils.writeOutputStream(optStream.get(), TEST_STRING);
				Assertions.assertEquals(TEST_STRING.getBytes(StandardCharsets.UTF_8).length, written,
						"Incorrect number of bytes written");
			});

			// Close the client socket
			boolean closed = ClientSocketUtils.closeConnection(optSocket.get());
			Assertions.assertTrue(closed, "Client socket should have closed but didn't");
		});

		// Start server and client threads
		serverThread.start();
		clientThread.start();

		// Wait up to 1s for the server-side accept and for the client to write
		boolean completed = latch.await(1000, TimeUnit.MILLISECONDS);
		Assertions.assertTrue(completed, "Client write operation should have completed.");
	}

	// Test that writing a blank/whitespace-only string returns 0 and does not write bytes
	@Test
	void testWriteBlankString() {
		// Create a client socket connected to the server
		Optional<Socket> optSocket = ClientSocketUtils.createSocket(VALID_HOST, VALID_PORT);
		Assertions.assertTrue(optSocket.isPresent(), "Client socket should exist but doesn't");

		// Extract the socket
		Socket socket = optSocket.get();

		// Obtain the OutputStream via the utility
		Optional<OutputStream> optStream = OutputStreamUtils.getOutputStream(socket);
		Assertions.assertTrue(optStream.isPresent(), "Output stream should exist but doesn't");

		// Write a blank string using the utility and assert it returns 0 bytes written
		Assertions.assertDoesNotThrow(() -> {
			int written = OutputStreamUtils.writeOutputStream(optStream.get(), "   ");
			Assertions.assertEquals(0, written, "Blank string should result in zero bytes written");
		});

		// Cleanup socket
		boolean closed = ClientSocketUtils.closeConnection(socket);
		Assertions.assertTrue(closed, "Client socket should have closed but didn't");
	}

	// Test writing to a stream that we simulate as "closed" by making write() throw IOException
	@Test
	void testWriteToClosedOutputStreamSimulatedWithIOException() throws IOException {
		// Create a Mockito mock for OutputStream that throws IOException on write
		OutputStream mockOut = mock(OutputStream.class);

		// When write(...) is invoked, simulate a "stream closed" IOException
		doThrow(new IOException("stream closed")).when(mockOut).write(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.anyInt(),
				org.mockito.ArgumentMatchers.anyInt());

		// Call the utility; it should catch the IOException and return 0 bytes written
		Assertions.assertDoesNotThrow(() -> {
			int written = OutputStreamUtils.writeOutputStream(mockOut, TEST_STRING);
			Assertions.assertEquals(0, written, "IOException (simulated closed) should result in zero bytes written");
		});
	}

	// Test the behavior when trying to obtain an OutputStream for a closed socket (no write)
	@Test
	void testWriteToOutputStreamForClosedSocket() throws Exception {
		// Create and connect a client socket
		Optional<Socket> optSocket = ClientSocketUtils.createSocket(VALID_HOST, VALID_PORT);
		Assertions.assertTrue(optSocket.isPresent(), "Client socket should exist but doesn't");

		// Close the socket to simulate a closed-socket scenario
		Socket socket = optSocket.get();
		socket.close();

		// Attempt to get the OutputStream via utility; closed socket should not produce a stream
		Optional<OutputStream> optStream = OutputStreamUtils.getOutputStream(socket);

		// Validate that the Optional is empty for a closed socket
		Assertions.assertTrue(optStream.isEmpty(), "Output stream should be empty for closed socket");
	}

	// Test that a SocketTimeoutException thrown by the underlying OutputStream is rethrown
	@Test
	void testSocketTimeoutExceptionOnWrite() throws IOException {
		// Create a mock OutputStream that throws SocketTimeoutException when write is called
		OutputStream mockOut = mock(OutputStream.class);

		// Simulate a timeout being thrown by the stream on write
		doThrow(new SocketTimeoutException("timeout")).when(mockOut).write(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.anyInt(),
				org.mockito.ArgumentMatchers.anyInt());

		// The utility is expected to rethrow SocketTimeoutException — assert it is thrown
		Assertions.assertThrows(SocketTimeoutException.class,
				() -> OutputStreamUtils.writeOutputStream(mockOut, TEST_STRING),
				"SocketTimeoutException should be thrown");
	}

	// Test that a general IOException on write is swallowed by the utility and yields 0 bytes written
	@Test
	void testIOExceptionOnWrite() throws IOException {
		// Create a mock OutputStream that throws an IOException when write is called
		OutputStream mockOut = mock(OutputStream.class);

		// Simulate an IOException during write
		doThrow(new IOException("io fail")).when(mockOut).write(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.anyInt(),
				org.mockito.ArgumentMatchers.anyInt());

		// The utility should handle the IOException and return 0 bytes written (no exception)
		Assertions.assertDoesNotThrow(() -> {
			int written = OutputStreamUtils.writeOutputStream(mockOut, TEST_STRING);
			Assertions.assertEquals(0, written, "IOException should result in zero bytes written");
		});
	}
}
