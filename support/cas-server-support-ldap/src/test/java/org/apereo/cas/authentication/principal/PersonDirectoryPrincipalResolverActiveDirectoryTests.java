package org.apereo.cas.authentication.principal;

import org.apereo.cas.authentication.BaseActiveDirectoryLdapAuthenticationHandlerTests;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.CoreAuthenticationUtils;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;
import org.apereo.cas.config.CasPersonDirectoryConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import lombok.val;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link PersonDirectoryPrincipalResolverActiveDirectoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@SpringBootTest(classes = {
    CasPersonDirectoryConfiguration.class,
    RefreshAutoConfiguration.class
}, properties = {
    "cas.authn.attribute-repository.ldap[0].bind-dn=Administrator@cas.example.org",
    "cas.authn.attribute-repository.ldap[0].bind-credential=" + BaseActiveDirectoryLdapAuthenticationHandlerTests.AD_ADMIN_PASSWORD,
    "cas.authn.attribute-repository.ldap[0].ldap-url=" + BaseActiveDirectoryLdapAuthenticationHandlerTests.AD_LDAP_URL,
    "cas.authn.attribute-repository.ldap[0].useStartTls=true",
    "cas.authn.attribute-repository.ldap[0].base-dn=dc=cas,dc=example,dc=org",
    "cas.authn.attribute-repository.ldap[0].search-filter=(sAMAccountName={user})",
    "cas.authn.attribute-repository.ldap[0].trust-store=" + BaseActiveDirectoryLdapAuthenticationHandlerTests.AD_TRUST_STORE,
    "cas.authn.attribute-repository.ldap[0].trust-store-type=JKS",
    "cas.authn.attribute-repository.ldap[0].trust-store-password=changeit",
    "cas.authn.attribute-repository.ldap[0].attributes.displayName=description",
    "cas.authn.attribute-repository.ldap[0].attributes.objectGUID=objectGUID",
    "cas.authn.attribute-repository.ldap[0].attributes.objectSid=objectSid"
})
@EnabledIfPortOpen(port = 10390)
@Tag("Ldap")
public class PersonDirectoryPrincipalResolverActiveDirectoryTests {
    @Autowired
    @Qualifier("attributeRepository")
    private IPersonAttributeDao attributeRepository;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Test
    public void verifyResolver() {
        val resolver = CoreAuthenticationUtils.newPersonDirectoryPrincipalResolver(PrincipalFactoryUtils.newPrincipalFactory(),
            this.attributeRepository, casProperties.getPersonDirectory());
        val p = resolver.resolve(new UsernamePasswordCredential("admin", "P@ssw0rd"),
            Optional.of(CoreAuthenticationTestUtils.getPrincipal()),
            Optional.of(new SimpleTestUsernamePasswordAuthenticationHandler()));
        assertNotNull(p);
        assertTrue(p.getAttributes().containsKey("description"));
        assertTrue(p.getAttributes().containsKey("objectGUID"));
        assertTrue(p.getAttributes().containsKey("objectSid"));
        CollectionUtils.firstElement(p.getAttributes().get("objectGUID"))
            .ifPresent(value -> assertNotNull(EncodingUtils.decodeBase64(value.toString())));
        CollectionUtils.firstElement(p.getAttributes().get("objectSid"))
            .ifPresent(value -> assertNotNull(EncodingUtils.decodeBase64(value.toString())));
    }
}
