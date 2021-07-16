package org.apereo.cas.config;

import org.apereo.cas.BaseCoreWsSecurityIdentityProviderConfigurationTests;
import org.apereo.cas.web.ProtocolEndpointWebSecurityConfigurer;

import org.apache.cxf.sts.token.realm.RealmProperties;
import org.apache.cxf.ws.security.sts.provider.SecurityTokenServiceProvider;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.servlet.ServletRegistrationBean;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CoreWsSecuritySecurityTokenServiceConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("WSFederation")
public class CoreWsSecuritySecurityTokenServiceConfigurationTests extends BaseCoreWsSecurityIdentityProviderConfigurationTests {
    @Autowired
    @Qualifier("cxfServlet")
    private ServletRegistrationBean cxfServlet;

    @Autowired
    @Qualifier("transportSTSProviderBean")
    private SecurityTokenServiceProvider transportSTSProviderBean;

    @Autowired
    @Qualifier("casRealm")
    private RealmProperties casRealm;

    @Autowired
    @Qualifier("wsFederationProtocolEndpointConfigurer")
    private ProtocolEndpointWebSecurityConfigurer<Void> wsFederationProtocolEndpointConfigurer;

    @Test
    public void verifyEndpoints() {
        assertFalse(wsFederationProtocolEndpointConfigurer.getIgnoredEndpoints().isEmpty());
    }

    @Test
    public void verifyOperation() {
        assertNotNull(cxfServlet);
        assertNotNull(transportSTSProviderBean);
        assertNotNull(casRealm);
    }

}
