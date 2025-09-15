package utils.time;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import net.ethandankiw.utils.TimeUtils;

class ConvertToMillisecondsTest {

	// Define values to convert to milliseconds
	private static final Integer VALID_SECONDS = 3;
	private static final Integer ZERO_SECONDS = 0;
	private static final Integer NEGATIVE_SECONDS = -1;

	// Define values to convert to milliseconds
	private static final Integer VALID_MILLISECONDS = 3000;
	private static final Integer ZERO_MILLISECONDS = 0;
	private static final Integer NEGATIVE_MILLISECONDS = -1;


	// Valid conversion for seconds to milliseconds
	@Test
	void testValidSecondsConversion() {
		// Convert seconds to milliseconds
		Integer milliseconds = TimeUtils.convertToMilliseconds(VALID_SECONDS, TimeUnit.SECONDS);

		// Check that the conversion was successful
		Assertions.assertEquals(VALID_MILLISECONDS, milliseconds, "Conversion to milliseconds does not match expected");
	}


	// Valid conversion for milliseconds to milliseconds
	@Test
	void testValidMillisecondsConversion() {
		// Convert milliseconds to milliseconds
		Integer milliseconds = TimeUtils.convertToMilliseconds(VALID_MILLISECONDS, TimeUnit.MILLISECONDS);

		// Check that the conversion was successful
		Assertions.assertEquals(VALID_MILLISECONDS, milliseconds, "Conversion to milliseconds does not match expected");
	}


	// Test conversion of 0 seconds to milliseconds
	@Test
	void testZeroSecondsConversion() {
		// Convert seconds to milliseconds
		Integer milliseconds = TimeUtils.convertToMilliseconds(ZERO_SECONDS, TimeUnit.SECONDS);

		// Check that the conversion was successful
		Assertions.assertEquals(ZERO_MILLISECONDS, milliseconds,
				"Conversion from 0 milliseconds should be 0 " + "milliseconds");
	}


	// Test conversion of 0 milliseconds to milliseconds
	@Test
	void testZeroMillisecondsConversion() {
		// Convert milliseconds to milliseconds
		Integer milliseconds = TimeUtils.convertToMilliseconds(ZERO_MILLISECONDS, TimeUnit.MILLISECONDS);

		// Check that the conversion was successful
		Assertions.assertEquals(ZERO_MILLISECONDS, milliseconds,
				"Conversion from 0 seconds should be 0 " + "milliseconds");
	}


	// Test conversion of negative seconds to milliseconds
	@Test
	void testNegativeSecondsConversion() {
		// Convert seconds to milliseconds
		Integer milliseconds = TimeUtils.convertToMilliseconds(NEGATIVE_SECONDS, TimeUnit.SECONDS);

		// Check that the conversion was successful
		Assertions.assertEquals(ZERO_MILLISECONDS, milliseconds,
				"Conversion from negative seconds should be 0 " + "milliseconds");
	}


	// Test conversion of negative milliseconds to milliseconds
	@Test
	void testNegativeMillisecondsConversion() {
		// Convert milliseconds to milliseconds
		Integer milliseconds = TimeUtils.convertToMilliseconds(NEGATIVE_MILLISECONDS, TimeUnit.MILLISECONDS);

		// Check that the conversion was successful
		Assertions.assertEquals(ZERO_MILLISECONDS, milliseconds,
				"Conversion from negative milliseconds should be 0 " + "milliseconds");
	}


	// Test invalid conversion unit
	@Test
	void testInvalidUnitConversion() {
		// Assert that an IllegalArgumentException is thrown when converting DAYS
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			// The code that is expected to throw the exception
			TimeUtils.convertToMilliseconds(VALID_SECONDS, TimeUnit.DAYS);
		});
	}

}
