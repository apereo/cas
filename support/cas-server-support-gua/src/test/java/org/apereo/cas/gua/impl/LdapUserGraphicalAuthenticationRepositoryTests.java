package org.apereo.cas.gua.impl;

import org.apereo.cas.AbstractGraphicalAuthenticationTests;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.gua.api.UserGraphicalAuthenticationRepository;
import org.apereo.cas.util.LdapUtils;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import lombok.val;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.ldaptive.AddOperation;
import org.ldaptive.AddRequest;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.LdapAttribute;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link LdapUserGraphicalAuthenticationRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Ldap")
@SpringBootTest(classes = AbstractGraphicalAuthenticationTests.SharedTestConfiguration.class,
    properties = {
        "cas.authn.gua.ldap.base-dn=dc=example,dc=org",
        "cas.authn.gua.ldap.ldap-url=ldap://localhost:10389",
        "cas.authn.gua.ldap.search-filter=cn={user}",
        "cas.authn.gua.ldap.image-attribute=jpegPhoto",
        "cas.authn.gua.ldap.bind-dn=cn=Directory Manager",
        "cas.authn.gua.ldap.bind-credential=password"
    })
@EnabledIfPortOpen(port = 10389)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class LdapUserGraphicalAuthenticationRepositoryTests {

    @Autowired
    @Qualifier("userGraphicalAuthenticationRepository")
    private UserGraphicalAuthenticationRepository userGraphicalAuthenticationRepository;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Test
    public void verifyOperation() throws Exception {
        val ldap = casProperties.getAuthn().getGua().getLdap();
        val factory = LdapUtils.newLdaptiveConnectionFactory(ldap);
        val cn = createLdapEntry(factory);
        assertFalse(userGraphicalAuthenticationRepository.getGraphics(cn).isEmpty());
        assertTrue(userGraphicalAuthenticationRepository.getGraphics("bad-user").isEmpty());
    }

    private static String createLdapEntry(final ConnectionFactory factory) throws Exception {
        val photo = IOUtils.toByteArray(new ClassPathResource("image.jpg").getInputStream());
        val cn = RandomUtils.randomAlphabetic(6).toLowerCase();
        val request = AddRequest.builder()
            .attributes(List.of(
                new LdapAttribute("objectclass", "top", "person", "inetOrgPerson"),
                new LdapAttribute("cn", cn),
                new LdapAttribute("jpegPhoto", photo),
                new LdapAttribute("sn", cn)))
            .dn("cn=" + cn + ",ou=People,dc=example,dc=org")
            .build();
        val operation = new AddOperation(factory);
        operation.execute(request);
        return cn;
    }
}
