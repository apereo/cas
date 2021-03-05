package org.apereo.cas.web.security.authentication;

import org.apereo.cas.adaptors.ldap.LdapIntegrationTestsOperations;
import org.apereo.cas.configuration.CasConfigurationProperties;

import com.unboundid.ldap.sdk.LDAPConnection;
import lombok.SneakyThrows;
import lombok.val;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * This is {@link BaseEndpointLdapAuthenticationProviderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@SpringBootTest(classes = RefreshAutoConfiguration.class,
    properties = {
        "cas.monitor.endpoints.ldap.ldap-url=ldap://localhost:10389",
        "cas.monitor.endpoints.ldap.base-dn=ou=people,dc=example,dc=org",
        "cas.monitor.endpoints.ldap.search-filter=cn={user}",
        "cas.monitor.endpoints.ldap.bind-dn=cn=Directory Manager",
        "cas.monitor.endpoints.ldap.bind-credential=password"
    })
@EnableConfigurationProperties(CasConfigurationProperties.class)
public abstract class BaseEndpointLdapAuthenticationProviderTests {
    private static final int LDAP_PORT = 10389;

    @Autowired
    protected CasConfigurationProperties casProperties;

    @BeforeAll
    @SneakyThrows
    public static void bootstrap() {
        ClientInfoHolder.setClientInfo(new ClientInfo(new MockHttpServletRequest()));
        val localhost = new LDAPConnection("localhost", LDAP_PORT, "cn=Directory Manager", "password");
        localhost.connect("localhost", LDAP_PORT);
        localhost.bind("cn=Directory Manager", "password");
        LdapIntegrationTestsOperations.populateEntries(localhost,
            new ClassPathResource("ldif/ldap-authz.ldif").getInputStream(), "ou=people,dc=example,dc=org");
    }

    @BeforeEach
    public void init() {
        val request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request, new MockHttpServletResponse()));
    }
}
