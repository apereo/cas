package org.apereo.cas.web.security.authentication;

import org.apereo.cas.adaptors.ldap.LdapIntegrationTestsOperations;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.LdapTest;

import com.unboundid.ldap.sdk.LDAPConnection;
import lombok.SneakyThrows;
import lombok.val;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.apereo.cas.constants.test.Ldap.*;

/**
 * This is {@link BaseMonitorEndpointLdapAuthenticationProviderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Tag("Ldap")
@TestPropertySource(properties = {
    "cas.monitor.endpoints.ldap.ldapUrl=${ldap.url}",
    "cas.monitor.endpoints.ldap.useSsl=false",
    "cas.monitor.endpoints.ldap.baseDn=${ldap.peopleDn}",
    "cas.monitor.endpoints.ldap.searchFilter=cn={user}",
    "cas.monitor.endpoints.ldap.bindDn=cn=Directory Manager",
    "cas.monitor.endpoints.ldap.bindCredential=password"
})
@SpringBootTest(classes = RefreshAutoConfiguration.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public abstract class BaseMonitorEndpointLdapAuthenticationProviderTests implements LdapTest {
    @Autowired
    protected CasConfigurationProperties casProperties;

    @BeforeAll
    @SneakyThrows
    public static void bootstrap() {
        ClientInfoHolder.setClientInfo(new ClientInfo(new MockHttpServletRequest()));
        val localhost = new LDAPConnection(getHost(), getPort(), getBindDn(), getBindPass());
        localhost.connect(getHost(), getPort());
        localhost.bind(getBindDn(), getBindPass());
        LdapIntegrationTestsOperations.populateEntries(localhost,
            new ClassPathResource("ldif/ldap-authz.ldif").getInputStream(), "ou=people," + getBaseDn());
    }

    @BeforeEach
    public void init() {
        val request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request, new MockHttpServletResponse()));
    }
}
