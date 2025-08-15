package org.apereo.cas.mfa.simple.validation;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.mfa.simple.CasSimpleMultifactorAuthenticationConstants;
import org.apereo.cas.mfa.simple.CasSimpleMultifactorTokenCredential;
import org.apereo.cas.mfa.simple.ticket.CasSimpleMultifactorAuthenticationTicket;
import org.apereo.cas.mfa.simple.ticket.CasSimpleMultifactorAuthenticationTicketFactory;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;

/**
 * This is {@link DefaultCasSimpleMultifactorAuthenticationService}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Slf4j
public class DefaultCasSimpleMultifactorAuthenticationService extends BaseCasSimpleMultifactorAuthenticationService {

    private static final int MAX_ATTEMPTS = 5;

    protected final TicketFactory ticketFactory;
    protected final ObjectProvider<CasSimpleMultifactorAuthenticationAccountService> accountServiceProvider;

    public DefaultCasSimpleMultifactorAuthenticationService(final TicketRegistry ticketRegistry,
                                                            final TicketFactory ticketFactory,
                                                            final ObjectProvider<CasSimpleMultifactorAuthenticationAccountService> accountServiceProvider) {
        super(ticketRegistry);
        this.ticketFactory = ticketFactory;
        this.accountServiceProvider = accountServiceProvider;
    }

    @Override
    public CasSimpleMultifactorAuthenticationTicket generate(final Principal principal, final Service service) throws Throwable {
        val mfaFactory = (CasSimpleMultifactorAuthenticationTicketFactory) ticketFactory.get(CasSimpleMultifactorAuthenticationTicket.class);
        val properties = CollectionUtils.<String, Serializable>wrap(CasSimpleMultifactorAuthenticationConstants.PROPERTY_PRINCIPAL, principal);
        return FunctionUtils.doAndRetry(retryContext -> {
            val token = FunctionUtils.doAndThrow(() -> mfaFactory.create(service, properties), RuntimeException::new);
            val trackingToken = ticketRegistry.getTicket(token.getId());
            if (trackingToken != null) {
                throw new IllegalArgumentException("Token: " + trackingToken.getId() + " already exists in ticket registry");
            }
            LOGGER.debug("Created multifactor authentication token [{}] for service [{}]", token.getId(), service);
            return token;
        }, MAX_ATTEMPTS);
    }

    @Override
    public void store(final CasSimpleMultifactorAuthenticationTicket token) {
        token.update();
        val trackingToken = ticketRegistry.getTicket(token.getId());
        FunctionUtils.doUnchecked(us -> {
            if (trackingToken != null) {
                LOGGER.debug("Updating existing token [{}] to registry", token.getId());
                ticketRegistry.updateTicket(trackingToken);
            } else {
                LOGGER.debug("Adding token [{}] to registry", token.getId());
                ticketRegistry.addTicket(token);
            }
        });
    }

    @Override
    public Principal fetch(final CasSimpleMultifactorTokenCredential tokenCredential) {
        return Optional.ofNullable(getMultifactorAuthenticationTicket(tokenCredential))
            .map(this::getPrincipalFromTicket)
            .orElse(null);
    }

    @Override
    public void update(final Principal principal, final Map<String, Object> attributes) {
        accountServiceProvider.ifAvailable(service -> service.update(principal, attributes));
    }

    @Override
    public Principal validate(final Principal resolvedPrincipal,
                              final CasSimpleMultifactorTokenCredential credential) throws Exception {
        val acct = getMultifactorAuthenticationTicket(credential);
        LOGGER.debug("Received token [{}] and principal id [{}]", acct, resolvedPrincipal.getId());
        val principal = validateTokenForPrincipal(resolvedPrincipal, acct);
        deleteToken(acct);
        LOGGER.debug("Validated token [{}] successfully for [{}].", credential.getId(), resolvedPrincipal.getId());
        return principal;
    }
}
