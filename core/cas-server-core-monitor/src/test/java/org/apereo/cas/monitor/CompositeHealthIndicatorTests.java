package org.apereo.cas.monitor;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.PingHealthIndicator;
import org.springframework.boot.actuate.health.Status;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CompositeHealthIndicatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Tag("Metrics")
public class CompositeHealthIndicatorTests {
    @Test
    public void verifyOperation() {
        val composite = new CompositeHealthIndicator(
            List.of(new DownHealthIndicator(Status.OUT_OF_SERVICE), new PingHealthIndicator())
        );
        val results = composite.health();
        assertEquals(Status.DOWN, results.getStatus());
    }

    @RequiredArgsConstructor
    public static class DownHealthIndicator extends AbstractHealthIndicator {
        private final Status status;

        protected void doHealthCheck(final Health.Builder builder) {
            builder.status(status);
        }
    }

}
