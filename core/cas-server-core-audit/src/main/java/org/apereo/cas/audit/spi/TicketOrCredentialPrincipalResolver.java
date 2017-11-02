package org.apereo.cas.audit.spi;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationTransaction;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.util.AopUtils;
import org.apereo.cas.util.Pac4jUtils;
import org.apereo.inspektr.common.spi.PrincipalResolver;

import org.aspectj.lang.JoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collection;

/**
 * PrincipalResolver that can retrieve the username from either the Ticket or from the Credential.
 *
 * @author Scott Battaglia
 * @since 3.1.2
 * @deprecated As of CAS 5. 
 */
@Deprecated
public class TicketOrCredentialPrincipalResolver implements PrincipalResolver {

    /** Logger instance. */
    private static final Logger LOGGER = LoggerFactory.getLogger(TicketOrCredentialPrincipalResolver.class);

    private final CentralAuthenticationService centralAuthenticationService;
    
    private AuditPrincipalIdProvider auditPrincipalIdProvider = new AuditPrincipalIdProvider() {};

    /**
     * Instantiates a new Ticket or credential principal resolver.
     *
     * @param centralAuthenticationService the central authentication service
     * @since 4.1.0
     */
    public TicketOrCredentialPrincipalResolver(final CentralAuthenticationService centralAuthenticationService) {
        this.centralAuthenticationService = centralAuthenticationService;
    }
    
    @Override
    public String resolveFrom(final JoinPoint joinPoint, final Object retVal) {
        return resolveFromInternal(AopUtils.unWrapJoinPoint(joinPoint));
    }

    @Override
    public String resolveFrom(final JoinPoint joinPoint, final Exception retVal) {
        return resolveFromInternal(AopUtils.unWrapJoinPoint(joinPoint));
    }

    @Override
    public String resolve() {
        return UNKNOWN_USER;
    }

    /**
     * Resolve the principal from the join point given.
     *
     * @param joinPoint the join point
     * @return the principal id, or {@link PrincipalResolver#UNKNOWN_USER}
     */
    protected String resolveFromInternal(final JoinPoint joinPoint) {
        final StringBuilder builder = new StringBuilder();

        final Object arg1 = joinPoint.getArgs()[0];
        if (arg1.getClass().isArray()) {
            final Object[] args1AsArray = (Object[]) arg1;
            resolveArguments(builder, args1AsArray);
        } else {
            builder.append(resolveArgument(arg1));
        }

        return builder.toString();

    }

    private String resolveArguments(final StringBuilder builder, final Collection args1AsArray) {
        args1AsArray.stream().forEach(arg -> builder.append(resolveArgument(arg)));
        return builder.toString();
    }

    private String resolveArguments(final StringBuilder builder, final Object[] args1AsArray) {
        Arrays.stream(args1AsArray).forEach(arg -> builder.append(resolveArgument(arg)));
        return builder.toString();
    }

    /**
     * Resolve the join point argument.
     *
     * @param arg1 the arg
     * @return the resolved string
     */
    private String resolveArgument(final Object arg1) {
        LOGGER.debug("Resolving argument [{}] for audit", arg1.getClass().getSimpleName());

        if (arg1 instanceof AuthenticationTransaction) {
            final AuthenticationTransaction transaction = AuthenticationTransaction.class.cast(arg1);
            return resolveArguments(new StringBuilder(), transaction.getCredentials());
        }
        if (arg1 instanceof Credential) {
            return arg1.toString();
        }
        if (arg1 instanceof String) {
            try {
                final Ticket ticket = this.centralAuthenticationService.getTicket((String) arg1, Ticket.class);
                Authentication authentication = null;
                if (ticket instanceof ServiceTicket) {
                    authentication = ServiceTicket.class.cast(ticket).getGrantingTicket().getAuthentication();
                } else if (ticket instanceof TicketGrantingTicket) {
                    authentication = TicketGrantingTicket.class.cast(ticket).getAuthentication();
                }
                return this.auditPrincipalIdProvider.getPrincipalIdFrom(authentication);
            } catch (final InvalidTicketException e) {
                LOGGER.trace(e.getMessage(), e);
            }
            LOGGER.debug("Could not locate ticket [{}] in the registry", arg1);
        }
        return Pac4jUtils.getPac4jAuthenticatedUsername();
    }

    /**
     * Get principal id provider.
     *
     * @return principal id provider
     */
    public AuditPrincipalIdProvider getAuditPrincipalIdProvider() {
        return auditPrincipalIdProvider;
    }

    public void setAuditPrincipalIdProvider(final AuditPrincipalIdProvider auditPrincipalIdProvider) {
        this.auditPrincipalIdProvider = auditPrincipalIdProvider;
    }
}
