package org.apereo.cas.util.junit;

import module java.base;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.junit.jupiter.api.function.Executable;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.core.NestedExceptionUtils.getRootCause;

/**
 * This is {@link Assertions}. Allows expected type to be null.
 *
 * @author Timur Duehr
 * @since 6.1.0
 */
@UtilityClass
public class Assertions {

    /**
     * Check exception's root cause.
     *
     * @param expectedClass Expected exception type of root cause.
     * @param actual        Exception to be validated
     * @param <T>           Type of actual exception.
     */
    public static <T extends Throwable> void assertHasRootCause(final Class<? extends Throwable> expectedClass, final T actual) {
        assertTrue(expectedClass.isInstance(getRootCause(actual)));
    }

    /**
     * Assert procedure throws an exception with a specific root cause.
     *
     * @param expectedType     Expected exception type.
     * @param expectedRootType Expected exception root cause type.
     * @param executable       Procedure to assert thrown exception.
     * @param <T>              Type of exception to be expected.
     * @return Exception thrown in assertion process.
     */
    public static <T extends Throwable> T assertThrowsWithRootCause(
        final Class<T> expectedType,
        final Class<? extends Throwable> expectedRootType,
        final Executable executable) {
        val exception = assertThrows(expectedType, executable);
        assertHasRootCause(expectedRootType, exception);
        return exception;
    }

    /**
     * Null safe assertThrows and assertDoesNotThrow..
     *
     * @param expected   Expected exception type. +null+ if no exception should be thrown.
     * @param executable Procedure to assert thrown exception.
     * @param <T>        Type of exception to be expected.
     * @return Exception thrown in assertion process.
     */
    public static <T extends Throwable> T assertThrowsOrNot(final T expected, final Executable executable) {
        if (expected == null) {
            assertDoesNotThrow(executable);
            return null;
        }
        return (T) assertThrows(expected.getClass(), executable);
    }

}
