package org.apereo.cas.authentication;

import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.query.N1qlQuery;
import com.couchbase.client.java.query.N1qlQueryResult;
import com.couchbase.client.java.query.N1qlQueryRow;
import com.couchbase.client.java.query.Select;
import com.couchbase.client.java.query.SimpleN1qlQuery;
import com.couchbase.client.java.query.Statement;
import com.couchbase.client.java.query.dsl.Expression;
import org.apereo.cas.authentication.handler.support.AbstractUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.configuration.model.support.couchbase.authentication.CouchbaseAuthenticationProperties;
import org.apereo.cas.couchbase.core.CouchbaseClientFactory;
import org.apereo.cas.services.ServicesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * This is {@link CouchbaseAuthenticationHandler}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class CouchbaseAuthenticationHandler extends AbstractUsernamePasswordAuthenticationHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(CouchbaseAuthenticationHandler.class);

    private final CouchbaseAuthenticationProperties couchbaseProperties;
    private final CouchbaseClientFactory couchbase;

    public CouchbaseAuthenticationHandler(final ServicesManager servicesManager,
                                          final PrincipalFactory principalFactory,
                                          final CouchbaseClientFactory couchbase,
                                          final CouchbaseAuthenticationProperties couchbaseProperties) {
        super(couchbaseProperties.getName(), servicesManager, principalFactory, couchbaseProperties.getOrder());
        this.couchbase = couchbase;
        this.couchbaseProperties = couchbaseProperties;

        System.setProperty("com.couchbase.queryEnabled", Boolean.toString(couchbaseProperties.isQueryEnabled()));
    }

    @Override
    protected HandlerResult authenticateUsernamePasswordInternal(final UsernamePasswordCredential transformedCredential,
                                                                 final String originalPassword) throws GeneralSecurityException {
        final Statement statement = Select.select("*")
                .from(Expression.i(couchbaseProperties.getBucket()))
                .where(Expression.x(couchbaseProperties.getUsernameAttribute())
                        .eq('\'' + transformedCredential.getUsername() + '\''));

        final SimpleN1qlQuery query = N1qlQuery.simple(statement);
        final N1qlQueryResult result = couchbase.getBucket().query(query, couchbaseProperties.getTimeout(), TimeUnit.MILLISECONDS);
        if (result.finalSuccess()) {
            if (result.allRows().size() > 1) {
                throw new FailedLoginException("More then one row found for user " + transformedCredential.getId());
            }
            final N1qlQueryRow row = result.allRows().get(0);
            if (row.value().containsKey(couchbase.getBucket().name())) {
                final JsonObject value = (JsonObject) row.value().get(couchbase.getBucket().name());
                if (!value.containsKey(couchbaseProperties.getUsernameAttribute())) {
                    throw new FailedLoginException("No user attribute found for " + transformedCredential.getId());
                }
                if (!value.containsKey(couchbaseProperties.getPasswordAttribute())) {
                    throw new FailedLoginException("No password attribute found for " + transformedCredential.getId());
                }

                if (!value.get(couchbaseProperties.getPasswordAttribute()).equals(transformedCredential.getPassword())) {
                    LOGGER.warn("Account password on record for [{}] does not match the given/encoded password",
                            transformedCredential.getId());
                    throw new FailedLoginException();
                }
                return createHandlerResult(transformedCredential,
                        this.principalFactory.createPrincipal(transformedCredential.getId(),
                                new HashMap<>()), new ArrayList<>());

            }
        }
        LOGGER.debug("Couchbase authentication failed with [{}]", result);
        throw new AccountNotFoundException("Could not locate account for user " + transformedCredential.getUsername());
    }
}
