package org.apereo.cas.web.flow;

import module java.base;
import org.apereo.cas.authentication.principal.PrincipalProvisioner;
import org.apereo.cas.config.CasCoreAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreAutoConfiguration;
import org.apereo.cas.config.CasCoreCookieAutoConfiguration;
import org.apereo.cas.config.CasCoreLogoutAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationWebflowAutoConfiguration;
import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreScriptingAutoConfiguration;
import org.apereo.cas.config.CasCoreServicesAutoConfiguration;
import org.apereo.cas.config.CasCoreTicketsAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.config.CasCoreWebflowAutoConfiguration;
import org.apereo.cas.config.CasPersonDirectoryAutoConfiguration;
import org.apereo.cas.config.CasScimAutoConfiguration;
import org.apereo.cas.config.CasScimPersonDirectoryAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.scim.v2.ScimService;
import org.apereo.cas.scim.v2.provisioning.ScimPrincipalAttributeMapper;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.execution.Action;

/**
 * This is {@link BaseScimTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = {
    CasCoreWebflowAutoConfiguration.class,
    CasCoreServicesAutoConfiguration.class,
    CasCoreWebAutoConfiguration.class,
    CasCoreCookieAutoConfiguration.class,
    CasPersonDirectoryAutoConfiguration.class,
    CasCoreUtilAutoConfiguration.class,
    CasCoreScriptingAutoConfiguration.class,
    CasCoreAutoConfiguration.class,
    CasCoreTicketsAutoConfiguration.class,
    CasCoreLogoutAutoConfiguration.class,
    CasCoreNotificationsAutoConfiguration.class,
    CasCoreAuthenticationAutoConfiguration.class,
    CasCoreMultifactorAuthenticationAutoConfiguration.class,
    CasCoreMultifactorAuthenticationWebflowAutoConfiguration.class,
    CasScimPersonDirectoryAutoConfiguration.class,
    CasScimAutoConfiguration.class
})
@ExtendWith(CasTestExtension.class)
public abstract class BaseScimTests {
    @Autowired
    @Qualifier(PrincipalProvisioner.BEAN_NAME)
    protected PrincipalProvisioner principalProvisioner;

    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_SCIM_PROVISIONING_PRINCIPAL)
    protected Action principalScimProvisionerAction;

    @Autowired
    protected ConfigurableApplicationContext applicationContext;

    @Autowired
    @Qualifier("scim2PrincipalAttributeMapper")
    protected ScimPrincipalAttributeMapper scim2PrincipalAttributeMapper;

    @Autowired
    protected CasConfigurationProperties casProperties;

    protected ScimService getPrincipalProvisioningScimService() {
        return (ScimService) principalProvisioner;
    }
}
