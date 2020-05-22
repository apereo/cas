package org.apereo.cas.monitor;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for {@link AbstractPoolHealthIndicator} class.
 *
 * @author Marvin S. Addison
 * @since 3.5.0
 */
@Tag("Simple")
public class PoolHealthIndicatorTests {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Test
    public void verifyObserveOK() throws Exception {
        val monitor = new AbstractPoolHealthIndicator(1000, executor) {
            @Override
            protected Health.Builder checkPool(final Health.Builder builder) {
                return builder.up();
            }

            @Override
            protected int getIdleCount() {
                return 3;
            }

            @Override
            protected int getActiveCount() {
                return 2;
            }

        };
        val health = monitor.health();
        assertEquals(health.getStatus(), Status.UP);

        assertAll(new Executable() {
            @Override
            public void execute() throws Exception {
                ((DisposableBean) monitor).destroy();
            }
        });
    }

    @Test
    public void verifyObserveDown() throws Exception {
        val monitor = new AbstractPoolHealthIndicator(200, executor) {
            @Override
            protected Health.Builder checkPool(final Health.Builder builder) throws Exception {
                Thread.sleep(300);
                return builder.up();
            }

            @Override
            protected int getIdleCount() {
                return 1;
            }

            @Override
            protected int getActiveCount() {
                return 1;
            }
        };
        val health = monitor.health();
        assertEquals(Status.DOWN, health.getStatus());

        assertAll(new Executable() {
                @Override
                public void execute() throws Exception {
                    ((DisposableBean) monitor).destroy();
                }
        });
    }

    @Test
    public void verifyObserveError() throws Exception {
        val monitor = new AbstractPoolHealthIndicator(500, executor) {
            @Override
            protected Health.Builder checkPool(final Health.Builder builder) {
                throw new IllegalArgumentException("Pool check failed.");
            }

            @Override
            protected int getIdleCount() {
                return 1;
            }

            @Override
            protected int getActiveCount() {
                return 1;
            }
        };
        val health = monitor.health();
        assertEquals(health.getStatus(), Status.OUT_OF_SERVICE);
        assertAll(new Executable() {
                @Override
                public void execute() throws Exception {
                    ((DisposableBean) monitor).destroy();
                }
        });
    }
}
