package org.apereo.cas.mcp.tools;

import module java.base;
import org.apereo.cas.logout.LogoutRequestStatus;
import org.apereo.cas.logout.slo.SingleLogoutRequestContext;
import org.apereo.cas.logout.slo.SingleLogoutRequestExecutor;
import org.apereo.cas.mcp.CasMcpEvaluationContextFactory;
import org.apereo.cas.mcp.CasMcpSessionReferences;
import org.apereo.cas.services.ChainingRegisteredServiceAccessStrategy;
import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceAccessStrategy;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.web.support.ThrottledSubmission;
import org.apereo.cas.web.support.ThrottledSubmissionsStore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * This is {@link CasAdministrationTools}, the tools that change CAS: they end single sign-on sessions, release throttled
 * logins, turn applications on or off and change log levels. They are registered only when the {@code mcp-write} feature
 * is turned on and require the {@code MCP_OPERATOR} role. Single sign-on session ids are never accepted or returned.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequiredArgsConstructor
public class CasAdministrationTools {
    /**
     * The bean name of these tools.
     */
    public static final String BEAN_NAME = "casAdministrationTools";

    private final TicketRegistry ticketRegistry;

    private final ServicesManager servicesManager;

    private final CasMcpEvaluationContextFactory casMcpEvaluationContextFactory;

    private final ObjectProvider<SingleLogoutRequestExecutor> singleLogoutRequestExecutor;

    private final ObjectProvider<ThrottledSubmissionsStore> throttledSubmissionsStore;

    private final ObjectProvider<LoggingSystem> loggingSystem;

    /**
     * End the single sign-on sessions of a person, with single logout for the applications of each session.
     *
     * @param username         the username
     * @param sessionReference the session reference of a single session, optional
     * @return the ended sessions
     */
    @McpTool(description = "End the single sign-on sessions of a person, or a single session given its session reference from "
        + "findSessions, so the person must log in again. Single logout is sent to the applications of each session, as when "
        + "the person logs out; use previewLogout first to see which applications are notified. Returns the session "
        + "references that were ended and how many logout requests succeeded or failed.",
        title = "Revoke Single Sign-On Sessions",
        generateOutputSchema = true,
        annotations = @McpTool.McpAnnotations(readOnlyHint = false, destructiveHint = true,
            idempotentHint = true, openWorldHint = true))
    @PreAuthorize("hasRole('MCP_OPERATOR')")
    public RevokedSessions revokeSessions(
        @McpToolParam(description = "The username (principal id) of the person")
        final String username,
        @McpToolParam(description = "Only end the session with this session reference, as returned by findSessions", required = false)
        @Nullable final String sessionReference) {
        val executor = singleLogoutRequestExecutor.getIfAvailable();
        if (executor == null) {
            return new RevokedSessions(username, 0, List.of(), "Single logout is not available");
        }
        if (StringUtils.isNotBlank(sessionReference) && !CasMcpSessionReferences.isReference(sessionReference)) {
            return new RevokedSessions(username, 0, List.of(), "Only session references returned by findSessions are accepted");
        }
        val ticketIds = new ArrayList<String>();
        if (StringUtils.isNotBlank(sessionReference)) {
            CasMcpSessionReferences.resolve(ticketRegistry, sessionReference, username).map(Ticket::getId).ifPresent(ticketIds::add);
            if (ticketIds.isEmpty()) {
                return new RevokedSessions(username, 0, List.of(), "No single sign-on session of the person has this session reference");
            }
        } else {
            try (val sessions = ticketRegistry.getSessionsFor(username)) {
                sessions.map(Ticket::getId).forEach(ticketIds::add);
            }
        }
        val exchange = casMcpEvaluationContextFactory.currentHttpExchange();
        val revoked = ticketIds.stream()
            .map(ticketId -> {
                val requests = executor.execute(ticketId, exchange.request(), exchange.response());
                return new RevokedSession(CasMcpSessionReferences.of(ticketId),
                    count(requests, LogoutRequestStatus.SUCCESS), count(requests, LogoutRequestStatus.FAILURE));
            })
            .toList();
        return new RevokedSessions(username, revoked.size(), revoked, null);
    }

    /**
     * Release throttled logins, so that the person or address may try to log in again right away.
     *
     * @param username        the username, optional
     * @param clientIpAddress the client IP address, optional
     * @return the number of released records
     */
    @McpTool(description = "Release failed login attempts that CAS authentication throttling tracks, for a username, a client "
        + "IP address or both, so the person may try to log in again right away instead of waiting for the lock to end. "
        + "Use findThrottledLogins first. Covers in-memory and Hazelcast throttling; throttling backed by JDBC, MongoDB or "
        + "Redis counts failures in the audit trail, which this tool does not change.",
        title = "Release Throttled Logins",
        generateOutputSchema = true,
        annotations = @McpTool.McpAnnotations(readOnlyHint = false, destructiveHint = true,
            idempotentHint = true, openWorldHint = false))
    @PreAuthorize("hasRole('MCP_OPERATOR')")
    public ReleasedThrottledLogins releaseThrottledLogins(
        @McpToolParam(description = "Release records for this username, ignoring case", required = false)
        @Nullable final String username,
        @McpToolParam(description = "Release records for this client IP address", required = false)
        @Nullable final String clientIpAddress) {
        if (StringUtils.isAllBlank(username, clientIpAddress)) {
            return new ReleasedThrottledLogins(username, clientIpAddress, 0, "A username or a client IP address is required");
        }
        val store = throttledSubmissionsStore.getIfAvailable();
        if (store == null) {
            return new ReleasedThrottledLogins(username, clientIpAddress, 0, "No throttle store is available");
        }
        val matches = (Predicate<ThrottledSubmission>) submission ->
            (StringUtils.isBlank(username) || Strings.CI.equals(submission.getUsername(), username))
                && (StringUtils.isBlank(clientIpAddress) || Strings.CS.equals(submission.getClientIpAddress(), clientIpAddress));
        val released = ((Stream<?>) store.entries()).map(ThrottledSubmission.class::cast).filter(matches).count();
        store.removeIf(matches);
        return new ReleasedThrottledLogins(username, clientIpAddress, released, null);
    }

    /**
     * Turn an application on or off in its access strategy.
     *
     * @param serviceId the numeric id of the registered service
     * @param enabled   whether the application is allowed
     * @return the status of the application
     */
    @McpTool(description = "Turn a registered application (service) on or off by its numeric id, through the enabled flag of "
        + "its access strategy, and save it in the service registry. While off, CAS refuses to log in to the application "
        + "and to validate its tickets. Applications whose access strategy has no enabled flag are left unchanged. Returns "
        + "whether the application was allowed before and after.",
        title = "Enable Or Disable Application",
        generateOutputSchema = true,
        annotations = @McpTool.McpAnnotations(readOnlyHint = false, destructiveHint = true,
            idempotentHint = true, openWorldHint = false))
    @PreAuthorize("hasRole('MCP_OPERATOR')")
    public ServiceStatus setServiceEnabled(
        @McpToolParam(description = "The numeric id of the registered service")
        final long serviceId,
        @McpToolParam(description = "true to allow the application, false to refuse it")
        final boolean enabled) {
        val registeredService = servicesManager.findServiceBy(serviceId);
        if (registeredService == null) {
            return new ServiceStatus(serviceId, null, null, null, null, "No registered service has this id");
        }
        val accessStrategy = registeredService.getAccessStrategy();
        val strategies = accessStrategy instanceof final ChainingRegisteredServiceAccessStrategy chain
            ? chain.getStrategies().stream().filter(DefaultRegisteredServiceAccessStrategy.class::isInstance).toList()
            : Stream.ofNullable(accessStrategy).filter(DefaultRegisteredServiceAccessStrategy.class::isInstance).toList();
        val type = accessStrategy != null ? CasDeploymentTools.typeOf(accessStrategy) : null;
        val previous = isEnabled(registeredService, accessStrategy);
        if (strategies.isEmpty()) {
            return new ServiceStatus(serviceId, registeredService.getName(), previous, previous, type,
                "The access strategy of the application has no enabled flag");
        }
        strategies.forEach(strategy -> ((DefaultRegisteredServiceAccessStrategy) strategy).setEnabled(enabled));
        val saved = servicesManager.save(registeredService);
        return new ServiceStatus(serviceId, saved.getName(), previous, isEnabled(saved, saved.getAccessStrategy()), type, null);
    }

    /**
     * Change the level of a logger, or reset it to the level of the logging configuration.
     *
     * @param loggerName the logger name
     * @param level      the level, or null to reset it
     * @return the logger levels
     */
    @McpTool(description = "Change the log level of a logger of this CAS server, such as org.apereo.cas or ROOT, until the "
        + "server restarts or the logging configuration is reloaded; leave the level out to reset the logger to its "
        + "configured level. Levels are TRACE, DEBUG, INFO, WARN, ERROR, FATAL and OFF. DEBUG and TRACE may write "
        + "sensitive data such as attributes, tickets and tokens to the logs, so reset the logger when done. Changes only "
        + "this node of a cluster.",
        title = "Change Log Level",
        generateOutputSchema = true,
        annotations = @McpTool.McpAnnotations(readOnlyHint = false, destructiveHint = false,
            idempotentHint = true, openWorldHint = false))
    @PreAuthorize("hasRole('MCP_OPERATOR')")
    public LogLevelChange setLogLevel(
        @McpToolParam(description = "The name of the logger, such as org.apereo.cas, or ROOT for the root logger")
        final String loggerName,
        @McpToolParam(description = "The new level; leave it out to reset the logger to its configured level", required = false)
        @Nullable final String level) {
        val system = loggingSystem.getIfAvailable();
        if (system == null || StringUtils.isBlank(loggerName)) {
            return new LogLevelChange(loggerName, null, null, system == null ? "The logging system is not available" : "A logger name is required");
        }
        val requested = StringUtils.trimToEmpty(level);
        val newLevel = Arrays.stream(LogLevel.values())
            .filter(candidate -> candidate.name().equalsIgnoreCase(requested))
            .findFirst()
            .orElse(null);
        if (!requested.isEmpty() && (newLevel == null || !system.getSupportedLogLevels().contains(newLevel))) {
            return new LogLevelChange(loggerName, levelOf(system, loggerName), null, "The level is not supported: " + level);
        }
        val previous = levelOf(system, loggerName);
        system.setLogLevel(loggerName, newLevel);
        return new LogLevelChange(loggerName, previous, levelOf(system, loggerName), null);
    }

    private static @Nullable String levelOf(final LoggingSystem system, final String logger) {
        val configuration = system.getLoggerConfiguration(logger);
        if (configuration == null) {
            return null;
        }
        val effective = configuration.getEffectiveLevel();
        return effective != null ? effective.name() : null;
    }

    private static @Nullable Boolean isEnabled(final RegisteredService registeredService,
                                               @Nullable final RegisteredServiceAccessStrategy accessStrategy) {
        return accessStrategy != null ? accessStrategy.isServiceAccessAllowed(registeredService, null) : null;
    }

    private static int count(final List<SingleLogoutRequestContext> requests, final LogoutRequestStatus status) {
        return (int) requests.stream().filter(request -> request.getStatus() == status).count();
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record RevokedSessions(String username, int count, List<RevokedSession> sessions, @Nullable String reason) {
    }

    public record RevokedSession(String sessionReference, int successfulLogoutRequests, int failedLogoutRequests) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ReleasedThrottledLogins(@Nullable String username, @Nullable String clientIpAddress, long released,
                                          @Nullable String reason) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ServiceStatus(long serviceId, @Nullable String name, @Nullable Boolean previouslyEnabled, @Nullable Boolean enabled,
                                @Nullable String accessStrategy, @Nullable String reason) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record LogLevelChange(String logger, @Nullable String previousLevel, @Nullable String level, @Nullable String reason) {
    }
}
