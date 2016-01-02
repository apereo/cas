package org.jasig.cas.audit.spi;

import org.aspectj.lang.JoinPoint;
import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.AuthenticationTransaction;
import org.jasig.cas.authentication.Credential;
import org.jasig.cas.ticket.InvalidTicketException;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.util.AopUtils;
import org.jasig.cas.web.support.WebUtils;
import org.jasig.inspektr.common.spi.PrincipalResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.util.Collection;

/**
 * PrincipalResolver that can retrieve the username from either the Ticket or from the Credential.
 *
 * @author Scott Battaglia
 * @since 3.1.2
 *
 */
@Component("auditablePrincipalResolver")
public final class TicketOrCredentialPrincipalResolver implements PrincipalResolver {

    /** Logger instance. */
    private static final Logger LOGGER = LoggerFactory.getLogger(TicketOrCredentialPrincipalResolver.class);

    @NotNull
    @Resource(name="centralAuthenticationService")
    private CentralAuthenticationService centralAuthenticationService;

    @Autowired(required = false)
    @Qualifier("principalIdProvider")
    private PrincipalIdProvider principalIdProvider = new DefaultPrincipalIdProvider();

    private TicketOrCredentialPrincipalResolver() {}

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
        for (final Object arg: args1AsArray) {
            builder.append(resolveArgument(arg));
        }
        return builder.toString();
    }

    private String resolveArguments(final StringBuilder builder, final Object[] args1AsArray) {
        for (final Object arg: args1AsArray) {
            builder.append(resolveArgument(arg));
        }
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
                return this.principalIdProvider.getPrincipalIdFrom(authentication);
            } catch (final InvalidTicketException e) {
                LOGGER.trace(e.getMessage(), e);
            }
            LOGGER.debug("Could not locate ticket [{}] in the registry", arg1);
        }
        return WebUtils.getAuthenticatedUsername();
    }

    /**
     * Default implementation that simply returns principal#id.
     */
    static class DefaultPrincipalIdProvider implements PrincipalIdProvider {

        @Override
        public String getPrincipalIdFrom(final Authentication authentication) {
            return authentication.getPrincipal().getId();
        }
    }
}
