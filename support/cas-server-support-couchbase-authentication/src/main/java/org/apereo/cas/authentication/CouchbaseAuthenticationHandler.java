package org.apereo.cas.authentication;

import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.query.N1qlQuery;
import com.couchbase.client.java.query.N1qlQueryResult;
import com.couchbase.client.java.query.N1qlQueryRow;
import com.couchbase.client.java.query.Select;
import com.couchbase.client.java.query.SimpleN1qlQuery;
import com.couchbase.client.java.query.Statement;
import com.couchbase.client.java.query.dsl.Expression;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apereo.cas.authentication.handler.support.AbstractUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.configuration.model.support.couchbase.authentication.CouchbaseAuthenticationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.couchbase.core.CouchbaseClientFactory;
import org.apereo.cas.services.ServicesManager;

import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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

        System.setProperty("com.couchbase.queryEnabled", Boolean.toString(couchbaseProperties.isQueryEnabled()));
    }

    @Override
    protected AuthenticationHandlerExecutionResult authenticateUsernamePasswordInternal(final UsernamePasswordCredential transformedCredential,
                                                                                        final String originalPassword) throws GeneralSecurityException {
        final Statement statement = Select.select("*")
            .from(Expression.i(couchbaseProperties.getBucket()))
            .where(Expression.x(couchbaseProperties.getUsernameAttribute())
                .eq('\'' + transformedCredential.getUsername() + '\''));

        LOGGER.debug("Running query [{}] on bucket [{}]", statement.toString(), couchbase.getBucket().name());

        final SimpleN1qlQuery query = N1qlQuery.simple(statement);
        final long timeout = Beans.newDuration(couchbaseProperties.getTimeout()).toMillis();
        final N1qlQueryResult result = couchbase.getBucket().query(query, timeout, TimeUnit.MILLISECONDS);
        if (!result.finalSuccess()) {
            LOGGER.error("Couchbase authentication failed with [{}]", result.errors().stream().map(JsonObject::toString).collect(Collectors.joining(",")));
            throw new AccountNotFoundException("Could not locate account for user " + transformedCredential.getUsername());
        }

        if (result.allRows().isEmpty()) {
            LOGGER.error("Couchbase query did not return any results/rows.");
            throw new AccountNotFoundException("Could not locate account for user " + transformedCredential.getUsername());
        }

        if (result.allRows().size() > 1) {
            throw new FailedLoginException("More then one row found for user " + transformedCredential.getId());
        }

        final N1qlQueryRow row = result.allRows().get(0);
        if (!row.value().containsKey(couchbase.getBucket().name())) {
            throw new AccountNotFoundException("Couchbase query row does not contain this bucket [{}]" + couchbase.getBucket().name());
        }

        final JsonObject value = (JsonObject) row.value().get(couchbase.getBucket().name());
        if (!value.containsKey(couchbaseProperties.getUsernameAttribute())) {
            throw new FailedLoginException("No user attribute found for " + transformedCredential.getId());
        }
        if (!value.containsKey(couchbaseProperties.getPasswordAttribute())) {
            throw new FailedLoginException("No password attribute found for " + transformedCredential.getId());
        }

        if (!value.get(couchbaseProperties.getPasswordAttribute()).equals(transformedCredential.getPassword())) {
            LOGGER.warn("Account password on record for [{}] does not match the given/encoded password", transformedCredential.getId());
            throw new FailedLoginException();
        }

        final Map<String, Object> attributes =
            value.getNames().stream()
                .filter(name -> !name.equals(couchbaseProperties.getPasswordAttribute())
                    && !name.equals(couchbaseProperties.getUsernameAttribute()))
                .map(name -> Pair.of(name, value.get(name)))
                .collect(Collectors.toMap(Pair::getKey, Pair::getValue));

        final Principal principal = this.principalFactory.createPrincipal(transformedCredential.getId(), attributes);
        return createHandlerResult(transformedCredential, principal, new ArrayList<>());

    }
}
