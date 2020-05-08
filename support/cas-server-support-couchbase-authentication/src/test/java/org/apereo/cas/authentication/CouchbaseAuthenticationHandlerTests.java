package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.couchbase.core.CouchbaseClientFactory;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.security.crypto.scrypt.SCryptPasswordEncoder;

import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Basic unit tests on the {@link CouchbaseAuthenticationHandler} to ensure the password check behavior.
 *
 * @author Jerome LELEU
 * @since 6.0.4
 */
@Tag("Couchbase")
@EnabledIfPortOpen(port = 8091)
@SpringBootTest(classes = RefreshAutoConfiguration.class,
    properties = {
        "cas.authn.couchbase.clusterUsername=admin",
        "cas.authn.couchbase.clusterPassword=password",
        "cas.authn.couchbase.bucket=testbucket"
    })
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CouchbaseAuthenticationHandlerTests {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Test
    public void verify() throws Exception {
        val props = casProperties.getAuthn().getCouchbase();
        val factory = new CouchbaseClientFactory(props);
        val handler = new CouchbaseAuthenticationHandler(mock(ServicesManager.class),
            PrincipalFactoryUtils.newPrincipalFactory(), factory, props);
        val c = CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("casuser", "Mellon");
        val result = handler.authenticate(c);
        assertNotNull(result);
        val attributes = result.getPrincipal().getAttributes();
        assertEquals(2, attributes.size());
        assertTrue(attributes.containsKey("firstname"));
        assertTrue(attributes.containsKey("lastname"));
    }

    @Test
    public void verifyBadEncoding() throws Exception {
        val props = casProperties.getAuthn().getCouchbase();
        val factory = new CouchbaseClientFactory(props);
        val handler = new CouchbaseAuthenticationHandler(mock(ServicesManager.class),
            PrincipalFactoryUtils.newPrincipalFactory(), factory, props);
        handler.setPasswordEncoder(new SCryptPasswordEncoder());
        val c = CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("casuser", "Mellon");
        assertThrows(FailedLoginException.class, () -> handler.authenticate(c));
    }

    @Test
    public void verifyMissingUser() {
        val props = casProperties.getAuthn().getCouchbase();
        val factory = new CouchbaseClientFactory(props);
        val handler = new CouchbaseAuthenticationHandler(mock(ServicesManager.class),
            PrincipalFactoryUtils.newPrincipalFactory(), factory, props);
        val c = CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("casuser-missing", "Mellon");
        assertThrows(AccountNotFoundException.class, () -> handler.authenticate(c));
    }
}
