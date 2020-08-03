package org.apereo.cas.authentication;

import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.handler.support.AbstractUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.configuration.model.support.couchbase.authentication.CouchbaseAuthenticationProperties;
import org.apereo.cas.couchbase.core.CouchbaseClientFactory;
import org.apereo.cas.services.ServicesManager;

import lombok.extern.slf4j.Slf4j;
import lombok.val;

import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;

/**
 * This is {@link CouchbaseAuthenticationHandler}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class CouchbaseAuthenticationHandler extends AbstractUsernamePasswordAuthenticationHandler {
    private final CouchbaseAuthenticationProperties couchbaseProperties;

    private final CouchbaseClientFactory couchbase;

    public CouchbaseAuthenticationHandler(final ServicesManager servicesManager,
                                          final PrincipalFactory principalFactory,
                                          final CouchbaseClientFactory couchbase,
                                          final CouchbaseAuthenticationProperties couchbaseProperties) {
        super(couchbaseProperties.getName(), servicesManager, principalFactory, couchbaseProperties.getOrder());
        this.couchbase = couchbase;
        this.couchbaseProperties = couchbaseProperties;
    }

    @Override
    protected AuthenticationHandlerExecutionResult authenticateUsernamePasswordInternal(final UsernamePasswordCredential transformedCredential,
                                                                                        final String originalPassword) throws GeneralSecurityException {
        val query = String.format("%s = '%s'", couchbaseProperties.getUsernameAttribute(), transformedCredential.getUsername());
        val result = couchbase.select(query);
        val results = result.rowsAsObject();
        if (results.isEmpty()) {
            LOGGER.error("Couchbase query did not return any results/rows.");
            throw new AccountNotFoundException("Could not locate account for user " + transformedCredential.getUsername());
        }

        if (results.size() > 1) {
            throw new FailedLoginException("More then one row found for user " + transformedCredential.getId());
        }

        val row = results.get(0).getObject(couchbase.getBucket());
        if (!row.containsKey(couchbaseProperties.getUsernameAttribute())) {
            throw new FailedLoginException("No user attribute found for " + transformedCredential.getId());
        }
        if (!row.containsKey(couchbaseProperties.getPasswordAttribute())) {
            throw new FailedLoginException("No password attribute found for " + transformedCredential.getId());
        }

        val entryPassword = row.getString(couchbaseProperties.getPasswordAttribute());
        if (!getPasswordEncoder().matches(originalPassword, entryPassword)) {
            LOGGER.warn("Account password on record for [{}] does not match the given/encoded password", transformedCredential.getId());
            throw new FailedLoginException();
        }

        val attributes = CouchbaseClientFactory.collectAttributesFromEntity(row, s ->
            !s.equals(couchbaseProperties.getPasswordAttribute()) && !s.equals(couchbaseProperties.getUsernameAttribute()));
        val principal = this.principalFactory.createPrincipal(transformedCredential.getId(), attributes);
        return createHandlerResult(transformedCredential, principal, new ArrayList<>(0));
    }
}
