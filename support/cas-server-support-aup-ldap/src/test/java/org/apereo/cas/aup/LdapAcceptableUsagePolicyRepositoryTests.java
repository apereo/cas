package org.apereo.cas.aup;

import org.apereo.cas.config.CasAcceptableUsagePolicyLdapConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.LdapTest;
import org.apereo.cas.util.junit.EnabledIfContinuousIntegration;

import lombok.Getter;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link LdapAcceptableUsagePolicyRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("Ldap")
@Import(CasAcceptableUsagePolicyLdapConfiguration.class)
@EnabledIfContinuousIntegration
@TestPropertySource(properties = {
    "cas.acceptableUsagePolicy.ldap.ldapUrl=${ldap.url}",
    "cas.acceptableUsagePolicy.ldap.useSsl=false",
    "cas.acceptableUsagePolicy.ldap.baseDn=${ldap.peopleDn}",
    "cas.acceptableUsagePolicy.ldap.searchFilter=cn={0}",
    "cas.acceptableUsagePolicy.ldap.bindDn=${ldap.bindDn}",
    "cas.acceptableUsagePolicy.ldap.bindCredential=password",
    "cas.acceptableUsagePolicy.aupAttributeName=carLicense",
    "ldap.test.resource=ldif/ldap-aup.ldif"
})
@Getter
public class LdapAcceptableUsagePolicyRepositoryTests extends BaseAcceptableUsagePolicyRepositoryTests implements LdapTest {
    @Autowired
    @Qualifier("acceptableUsagePolicyRepository")
    protected AcceptableUsagePolicyRepository acceptableUsagePolicyRepository;

    @Autowired
    private CasConfigurationProperties casProperties;

    @BeforeAll
    public static void bootstrap() {
        ClientInfoHolder.setClientInfo(new ClientInfo(new MockHttpServletRequest()));
    }
}
