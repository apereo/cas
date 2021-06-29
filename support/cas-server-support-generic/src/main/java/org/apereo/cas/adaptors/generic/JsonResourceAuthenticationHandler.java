package org.apereo.cas.adaptors.generic;

import org.apereo.cas.DefaultMessageDescriptor;
import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.MessageDescriptor;
import org.apereo.cas.authentication.PreventedException;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.exceptions.AccountDisabledException;
import org.apereo.cas.authentication.exceptions.AccountPasswordMustChangeException;
import org.apereo.cas.authentication.exceptions.InvalidLoginLocationException;
import org.apereo.cas.authentication.exceptions.InvalidLoginTimeException;
import org.apereo.cas.authentication.handler.support.AbstractUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.DateTimeUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.RegexUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Splitter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.springframework.core.io.Resource;

import javax.security.auth.login.AccountExpiredException;
import javax.security.auth.login.AccountLockedException;
import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;
import java.io.Serializable;
import java.security.GeneralSecurityException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Map;

/**
 * This is {@link JsonResourceAuthenticationHandler}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
public class JsonResourceAuthenticationHandler extends AbstractUsernamePasswordAuthenticationHandler {

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true)
        .failOnUnknownProperties(true)
        .singleValueAsArray(true)
        .build()
        .toObjectMapper();

    private final Resource resource;

    public JsonResourceAuthenticationHandler(final String name, final ServicesManager servicesManager,
                                             final PrincipalFactory principalFactory,
                                             final Integer order, final Resource resource) {
        super(name, servicesManager, principalFactory, order);
        this.resource = resource;
    }

    @Override
    protected AuthenticationHandlerExecutionResult authenticateUsernamePasswordInternal(final UsernamePasswordCredential credential,
                                                                                        final String originalPassword) throws GeneralSecurityException, PreventedException {

        val map = readAccountsFromResource();
        val username = credential.getUsername();
        val password = credential.getPassword();
        if (!map.containsKey(username)) {
            throw new AccountNotFoundException();
        }

        val account = map.get(username);
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

            val clientInfo = ClientInfoHolder.getClientInfo();
            if (clientInfo != null && StringUtils.isNotBlank(account.getLocation())
                && !RegexUtils.find(account.getLocation(), clientInfo.getClientIpAddress())) {
                throw new InvalidLoginLocationException("Unable to login from this location");
            }

            if (StringUtils.isNotBlank(account.getAvailability())) {
                val range = Splitter.on("~").splitToList(account.getAvailability());
                val startDate = DateTimeUtils.convertToZonedDateTime(range.get(0));
                val endDate = DateTimeUtils.convertToZonedDateTime(range.get(1));
                val now = ZonedDateTime.now(Clock.systemUTC());
                if (now.isBefore(startDate) || now.isAfter(endDate)) {
                    throw new InvalidLoginTimeException("Unable to login at this time");
                }
            }

            val warnings = new ArrayList<MessageDescriptor>();
            if (account.getExpirationDate() != null) {
                val now = LocalDate.now(ZoneOffset.UTC);
                if (now.isEqual(account.getExpirationDate()) || now.isAfter(account.getExpirationDate())) {
                    throw new AccountExpiredException();
                }
                if (getPasswordPolicyConfiguration() != null) {
                    val warningPeriod = account.getExpirationDate()
                        .minusDays(getPasswordPolicyConfiguration().getPasswordWarningNumberOfDays());
                    if (now.isAfter(warningPeriod) || now.isEqual(warningPeriod)) {
                        val daysRemaining = ChronoUnit.DAYS.between(now, account.getExpirationDate());
                        warnings.add(new DefaultMessageDescriptor(
                            "password.expiration.loginsRemaining",
                            "You have {0} logins remaining before you MUST change your password.",
                            new Serializable[]{daysRemaining}));
                    }
                }
            }

            account.getWarnings().forEach(warning -> warnings.add(new DefaultMessageDescriptor(warning, warning, new Serializable[]{username})));
            val principal = this.principalFactory.createPrincipal(username, account.getAttributes());
            return createHandlerResult(credential, principal, warnings);
        }

        throw new FailedLoginException();
    }

    private Map<String, CasUserAccount> readAccountsFromResource() throws PreventedException {
        try {
            return MAPPER.readValue(resource.getInputStream(),
                new TypeReference<>() {
                });
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
            throw new PreventedException(e);
        }
    }
}
