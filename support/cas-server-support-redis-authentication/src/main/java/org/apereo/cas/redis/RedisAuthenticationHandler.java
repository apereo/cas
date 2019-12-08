package org.apereo.cas.redis;

import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.exceptions.AccountDisabledException;
import org.apereo.cas.authentication.exceptions.AccountPasswordMustChangeException;
import org.apereo.cas.authentication.handler.support.AbstractUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.services.ServicesManager;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.data.redis.core.RedisTemplate;

import javax.security.auth.login.AccountExpiredException;
import javax.security.auth.login.AccountLockedException;
import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;

/**
 * This is {@link RedisAuthenticationHandler}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
public class RedisAuthenticationHandler extends AbstractUsernamePasswordAuthenticationHandler {
    private final RedisTemplate redisTemplate;

    public RedisAuthenticationHandler(final String name, final ServicesManager servicesManager,
                                      final PrincipalFactory principalFactory, final Integer order,
                                      final RedisTemplate redisTemplate) {
        super(name, servicesManager, principalFactory, order);
        this.redisTemplate = redisTemplate;
    }

    @Override
    protected AuthenticationHandlerExecutionResult authenticateUsernamePasswordInternal(final UsernamePasswordCredential credential,
                                                                                        final String originalPassword) throws GeneralSecurityException {
        val account = (RedisUserAccount) redisTemplate.opsForValue().get(credential.getUsername());
        if (account == null) {
            throw new AccountNotFoundException();
        }
        if (!getPasswordEncoder().matches(originalPassword, account.getPassword())) {
            LOGGER.warn("Account password on record for [{}] does not match the given/encoded password", credential.getId());
            throw new FailedLoginException();
        }
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
        val principal = principalFactory.createPrincipal(account.getUsername(), account.getAttributes());
        return createHandlerResult(credential, principal, new ArrayList<>(0));
    }
}
