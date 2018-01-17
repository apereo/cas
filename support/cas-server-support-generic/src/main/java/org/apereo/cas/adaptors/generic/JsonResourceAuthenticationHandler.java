package org.apereo.cas.adaptors.generic;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.DefaultMessageDescriptor;
import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.MessageDescriptor;
import org.apereo.cas.authentication.PreventedException;
import org.apereo.cas.authentication.UsernamePasswordCredential;
import org.apereo.cas.authentication.exceptions.AccountDisabledException;
import org.apereo.cas.authentication.exceptions.AccountPasswordMustChangeException;
import org.apereo.cas.authentication.handler.support.AbstractUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.services.ServicesManager;
import org.springframework.core.io.Resource;

import javax.security.auth.login.AccountExpiredException;
import javax.security.auth.login.AccountLockedException;
import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;
import java.io.Serializable;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This is {@link JsonResourceAuthenticationHandler}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
public class JsonResourceAuthenticationHandler extends AbstractUsernamePasswordAuthenticationHandler {


    private final ObjectMapper mapper;
    private final Resource resource;

    public JsonResourceAuthenticationHandler(final String name, final ServicesManager servicesManager,
                                             final PrincipalFactory principalFactory,
                                             final Integer order, final Resource resource) {
        super(name, servicesManager, principalFactory, order);
        this.resource = resource;
        this.mapper = new ObjectMapper()
            .findAndRegisterModules()
            .enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
    }

    @Override
    protected AuthenticationHandlerExecutionResult authenticateUsernamePasswordInternal(final UsernamePasswordCredential credential,
                                                                                        final String originalPassword)
        throws GeneralSecurityException, PreventedException {
        final Map<String, CasUserAccount> map;
        try {
            map = mapper.readValue(resource.getInputStream(),
                new TypeReference<Map<String, CasUserAccount>>() {
                });
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new PreventedException(e);
        }
        final String username = credential.getUsername();
        final String password = credential.getPassword();
        if (!map.containsKey(username)) {
            throw new AccountNotFoundException();
        }

        final CasUserAccount account = map.get(username);
        if (matches(password, account.getPassword())) {
            switch (account.getStatus()) {
                case DISABLED:
                    throw new AccountDisabledException();
                case EXPIRED:
                    throw new AccountExpiredException();
                case LOCKED:
                    throw new AccountLockedException();
                case MUST_CHANGE_PASSWORD:
                    throw new AccountPasswordMustChangeException();
                case OK:
                default:
                    LOGGER.debug("Account status is OK");
            }

            final List<MessageDescriptor> warnings = new ArrayList<>();
            if (account.getExpirationDate() != null) {
                final LocalDate now = LocalDate.now(ZoneOffset.UTC);
                if (now.isEqual(account.getExpirationDate()) || now.isAfter(account.getExpirationDate())) {
                    throw new AccountExpiredException();
                }
                if (getPasswordPolicyConfiguration() != null) {
                    final LocalDate warningPeriod = account.getExpirationDate()
                        .minusDays(getPasswordPolicyConfiguration().getPasswordWarningNumberOfDays());
                    if (now.isAfter(warningPeriod) || now.isEqual(warningPeriod)) {
                        final long daysRemaining = ChronoUnit.DAYS.between(now, account.getExpirationDate());
                        warnings.add(new DefaultMessageDescriptor(
                            "password.expiration.loginsRemaining",
                            "You have {0} logins remaining before you MUST change your password.",
                            new Serializable[] {daysRemaining}));
                    }
                }
            }
            final Principal principal = this.principalFactory.createPrincipal(username, account.getAttributes());
            return createHandlerResult(credential, principal, warnings);
        }

        throw new FailedLoginException();
    }
}
