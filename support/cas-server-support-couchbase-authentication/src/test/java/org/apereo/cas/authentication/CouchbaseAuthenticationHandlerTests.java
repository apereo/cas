package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.support.password.PasswordEncoderUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.core.authentication.PasswordEncoderProperties;
import org.apereo.cas.couchbase.core.DefaultCouchbaseClientFactory;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.support.StaticApplicationContext;

import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Basic unit tests on the {@link CouchbaseAuthenticationHandler} to ensure the password check behavior.
 *
 * @author Jerome LELEU
 * @since 6.0.4
 * @deprecated Since 7.0.0
 */
@Tag("Couchbase")
@EnabledIfListeningOnPort(port = 8091)
@SpringBootTest(classes = RefreshAutoConfiguration.class,
    properties = {
        "cas.authn.couchbase.cluster-username=admin",
        "cas.authn.couchbase.cluster-password=password",
        "cas.authn.couchbase.bucket=pplbucket"
    })
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Deprecated(since = "7.0.0")
public class CouchbaseAuthenticationHandlerTests {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Test
    public void verify() throws Exception {
        val props = casProperties.getAuthn().getCouchbase();
        val factory = new DefaultCouchbaseClientFactory(props);
        val handler = new CouchbaseAuthenticationHandler(mock(ServicesManager.class),
            PrincipalFactoryUtils.newPrincipalFactory(), factory, props);
        val c = CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("casuser", "Mellon");
        val result = handler.authenticate(c, mock(Service.class));
        assertNotNull(result);
        val attributes = result.getPrincipal().getAttributes();
        assertEquals(2, attributes.size());
        assertTrue(attributes.containsKey("firstname"));
        assertTrue(attributes.containsKey("lastname"));
    }

    @Test
    public void verifyBadEncoding() {
        val ctx = new StaticApplicationContext();
        ctx.refresh();

        val props = casProperties.getAuthn().getCouchbase();
        val factory = new DefaultCouchbaseClientFactory(props);
        val handler = new CouchbaseAuthenticationHandler(mock(ServicesManager.class),
            PrincipalFactoryUtils.newPrincipalFactory(), factory, props);
        handler.setPasswordEncoder(PasswordEncoderUtils.newPasswordEncoder(new PasswordEncoderProperties().setType("SCRYPT"), ctx));
        val c = CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("casuser", "Mellon");
        assertThrows(FailedLoginException.class, () -> handler.authenticate(c, mock(Service.class)));
    }

    @Test
    public void verifyBadRecord() {
        val ctx = new StaticApplicationContext();
        ctx.refresh();
        val props = casProperties.getAuthn().getCouchbase();
        val factory = new DefaultCouchbaseClientFactory(props);
        val handler = new CouchbaseAuthenticationHandler(mock(ServicesManager.class),
            PrincipalFactoryUtils.newPrincipalFactory(), factory, props);
        handler.setPasswordEncoder(PasswordEncoderUtils.newPasswordEncoder(new PasswordEncoderProperties().setType("SCRYPT"), ctx));
        val c = CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("nopsw", "Mellon");
        assertThrows(FailedLoginException.class, () -> handler.authenticate(c, mock(Service.class)));
    }

    @Test
    public void verifyMissingUser() {
        val props = casProperties.getAuthn().getCouchbase();
        val factory = new DefaultCouchbaseClientFactory(props);
        val handler = new CouchbaseAuthenticationHandler(mock(ServicesManager.class),
            PrincipalFactoryUtils.newPrincipalFactory(), factory, props);
        val c = CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("casuser-missing", "Mellon");
        assertThrows(AccountNotFoundException.class, () -> handler.authenticate(c, mock(Service.class)));
    }
}
