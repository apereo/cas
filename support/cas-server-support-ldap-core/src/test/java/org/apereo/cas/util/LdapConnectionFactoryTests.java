package org.apereo.cas.util;

import module java.base;
import lombok.Getter;
import lombok.val;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.ldaptive.ConnectionConfig;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.DeleteOperation;
import org.ldaptive.DeleteRequest;
import org.ldaptive.DeleteResponse;
import org.ldaptive.FilterTemplate;
import org.ldaptive.LdapEntry;
import org.ldaptive.LdapException;
import org.ldaptive.ResultCode;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link LdapConnectionFactoryTests}.
 *
 * @author Hal Deadman
 * @since 8.0.0
 */
@Tag("Ldap")
class LdapConnectionFactoryTests {

    @Test
    void verifySearchFailureIsLoggedAtWarnOrHigher() throws Exception {
        val context = LoggerContext.getContext(false);
        val config = context.getConfiguration();
        val loggerConfig = config.getLoggerConfig(LdapConnectionFactory.class.getName());
        val appender = new InMemoryLogAppender("LdapConnectionFactoryTestsAppender");
        appender.start();
        loggerConfig.addAppender(appender, Level.WARN, null);
        context.updateLoggers();

        try {
            val connectionFactory = mock(ConnectionFactory.class);
            val connectionConfig = mock(ConnectionConfig.class);
            when(connectionFactory.getConnectionConfig()).thenReturn(connectionConfig);
            when(connectionConfig.getLdapUrl()).thenReturn("ldaps://localhost:10636");
            when(connectionFactory.getConnection()).thenThrow(new LdapException("INVALID_CREDENTIALS data 52e"));

            val filter = new FilterTemplate("(cn={user})");
            filter.setParameter("user", "casuser");

            try (val wrapper = new LdapConnectionFactory(connectionFactory)) {
                assertThrows(LdapException.class, () -> wrapper.executeSearchOperation("dc=example,dc=org", filter, 0, "cn"));
            }
            assertTrue(appender.getEvents().stream().anyMatch(event ->
                event.getLevel().isMoreSpecificThan(Level.WARN)
                    && event.getMessage().getFormattedMessage().contains("LDAP search operation failed")
                    && event.getMessage().getFormattedMessage().contains("dc=example,dc=org")
                    && event.getMessage().getFormattedMessage().contains("INVALID_CREDENTIALS")));
        } finally {
            loggerConfig.removeAppender(appender.getName());
            appender.stop();
            context.updateLoggers();
        }
    }

    @Test
    void verifyFailedOperationIsLoggedAtWarnOrHigher() {
        val context = LoggerContext.getContext(false);
        val config = context.getConfiguration();
        val loggerConfig = config.getLoggerConfig(LdapConnectionFactory.class.getName());
        val appender = new InMemoryLogAppender("LdapConnectionFactoryFailedOperationAppender");
        appender.start();
        loggerConfig.addAppender(appender, Level.WARN, null);
        context.updateLoggers();

        try {
            val connectionFactory = mock(ConnectionFactory.class);
            val result = mock(DeleteResponse.class);
            when(result.isSuccess()).thenReturn(false);
            when(result.getResultCode()).thenReturn(ResultCode.OTHER);
            when(result.getDiagnosticMessage()).thenReturn("operation failed");

            try (val ignored = mockConstruction(DeleteOperation.class,
                (operation, __) -> when(operation.execute(any(DeleteRequest.class))).thenReturn(result));
                 val wrapper = new LdapConnectionFactory(connectionFactory)) {
                val entry = new LdapEntry();
                entry.setDn("cn=casuser,dc=example,dc=org");
                assertFalse(wrapper.executeDeleteOperation(entry));
            }

            assertTrue(appender.getEvents().stream()
                .anyMatch(event -> event.getLevel().isMoreSpecificThan(Level.WARN)));
        } finally {
            loggerConfig.removeAppender(appender.getName());
            appender.stop();
            context.updateLoggers();
        }
    }

    @Getter
    private static final class InMemoryLogAppender extends AbstractAppender {
        private final List<LogEvent> events = new CopyOnWriteArrayList<>();

        private InMemoryLogAppender(final String name) {
            super(name, null, PatternLayout.createDefaultLayout(), true, Property.EMPTY_ARRAY);
        }

        @Override
        public void append(final LogEvent event) {
            events.add(event.toImmutable());
        }
    }
}


