package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.SimplePrincipal;
import org.apereo.cas.configuration.model.support.couchbase.authentication.CouchbaseAuthenticationProperties;
import org.apereo.cas.couchbase.core.CouchbaseClientFactory;
import org.apereo.cas.services.ServicesManager;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.query.DefaultN1qlQueryResult;
import com.couchbase.client.java.query.N1qlQueryRow;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.StandardPasswordEncoder;

import javax.security.auth.login.FailedLoginException;
import java.util.ArrayList;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Basic unit tests on the {@link CouchbaseAuthenticationHandler} to ensure the password check behavior.
 *
 * @author Jerome LELEU
 * @since 6.0.4
 */
public class CouchbaseAuthenticationHandlerTests {

    private static final String BUCKET_NAME = "default";
    private static final String LOGIN = "login";
    private static final String GOOD_PASSWORD = "good";
    private static final String BAD_PASSWORD = "bad";

    @Test
    public void noEncryptionGoodPassword() throws Exception {
        val principal = new SimplePrincipal();
        val result = internalAutenticate(NoOpPasswordEncoder.getInstance(), principal, GOOD_PASSWORD);

        assertEquals(principal, result.getPrincipal());
    }

    @Test
    public void noEncryptionBadPassword() throws Exception {
        assertThrows(FailedLoginException.class, () ->
            internalAutenticate(NoOpPasswordEncoder.getInstance(), new SimplePrincipal(), BAD_PASSWORD));
    }

    @Test
    public void sha256EncryptionGoodPassword() throws Exception {
        val principal = new SimplePrincipal();
        val result = internalAutenticate(new StandardPasswordEncoder(), principal, GOOD_PASSWORD);

        assertEquals(principal, result.getPrincipal());
    }

    @Test
    public void sha256EncryptionBadPassword() throws Exception {
        assertThrows(FailedLoginException.class, () ->
            internalAutenticate(new StandardPasswordEncoder(), new SimplePrincipal(), BAD_PASSWORD));
    }

    @Test
    public void bcryptEncryptionGoodPassword() throws Exception {
        val principal = new SimplePrincipal();
        val result = internalAutenticate(new BCryptPasswordEncoder(), principal, GOOD_PASSWORD);

        assertEquals(principal, result.getPrincipal());
    }

    @Test
    public void bcryptEncryptionBadPassword() throws Exception {
        assertThrows(FailedLoginException.class, () ->
        internalAutenticate(new BCryptPasswordEncoder(), new SimplePrincipal(), BAD_PASSWORD));
    }

    private static AuthenticationHandlerExecutionResult internalAutenticate(final PasswordEncoder encoder,
                                                                            final Principal principal,
                                                                            final String userPassword) throws Exception {
        val factory = mock(CouchbaseClientFactory.class);
        val defBucket = mock(Bucket.class);
        when(defBucket.name()).thenReturn(BUCKET_NAME);
        when(factory.getBucket()).thenReturn(defBucket);
        val properties = new CouchbaseAuthenticationProperties();
        val principalFactory = mock(PrincipalFactory.class);
        when(principalFactory.createPrincipal(any(String.class), any(Map.class))).thenReturn(principal);
        val handler = new CouchbaseAuthenticationHandler(mock(ServicesManager.class), principalFactory, factory, properties);
        handler.setPasswordEncoder(encoder);
        val c = CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword(LOGIN, userPassword);

        val queryResult = mock(DefaultN1qlQueryResult.class);
        val listRows = new ArrayList<N1qlQueryRow>();
        val row = mock(N1qlQueryRow.class);
        val json = JsonObject.empty()
            .put(properties.getUsernameAttribute(), LOGIN)
            .put(properties.getPasswordAttribute(), encoder.encode(GOOD_PASSWORD));
        val bucket = JsonObject.empty()
            .put(BUCKET_NAME, json);
        when(row.value()).thenReturn(bucket);
        listRows.add(row);
        when(queryResult.allRows()).thenReturn(listRows);

        when(factory.query(properties.getUsernameAttribute(), LOGIN)).thenReturn(queryResult);

        return handler.authenticate(c);
    }
}
