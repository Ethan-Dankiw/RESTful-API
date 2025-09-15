package net.ethandankiw.utils;

import java.util.concurrent.TimeUnit;

import org.jetbrains.annotations.NotNull;

public class TimeUtils {

	private TimeUtils() {
	}


	public static @NotNull Integer convertToMilliseconds(int value, @NotNull TimeUnit unit)
			throws IllegalArgumentException {
		// If the value to convert is 0
		if (value <= 0) {
			return 0;
		}

		// Define the converted duration value
		int duration;

		// Convert the duration value to milliseconds based on the provided unit
		switch (unit) {
			case MILLISECONDS -> duration = value;
			case SECONDS -> duration = value * 1000;
			default -> throw new IllegalArgumentException("Invalid unit conversion for timeout duration");
		}

		// Return the converted value in milliseconds
		return duration;
	}

}
