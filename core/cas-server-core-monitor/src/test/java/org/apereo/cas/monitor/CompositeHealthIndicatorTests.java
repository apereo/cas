package org.apereo.cas.monitor;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.health.contributor.AbstractHealthIndicator;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.PingHealthIndicator;
import org.springframework.boot.health.contributor.Status;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CompositeHealthIndicatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Tag("Metrics")
class CompositeHealthIndicatorTests {
    @Test
    void verifyOperation() {
        val composite = new CompositeHealthIndicator(
            List.of(new DownHealthIndicator(Status.OUT_OF_SERVICE), new PingHealthIndicator())
        );
        val results = composite.health();
        assertEquals(Status.DOWN, results.getStatus());
    }

    @RequiredArgsConstructor
    static class DownHealthIndicator extends AbstractHealthIndicator {
        private final Status status;

        @Override
        protected void doHealthCheck(final Health.Builder builder) {
            builder.status(status);
        }
    }

}
