package org.apereo.cas.web.flow;

import org.apereo.cas.aup.AcceptableUsagePolicyRepository;
import org.apereo.cas.config.CasAcceptableUsagePolicyWebflowConfiguration;
import org.apereo.cas.config.CasCookieAutoConfiguration;
import org.apereo.cas.config.CasCoreAuditAutoConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreAutoConfiguration;
import org.apereo.cas.config.CasCoreLogoutAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreServicesAutoConfiguration;
import org.apereo.cas.config.CasCoreTicketsAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.config.CasMultifactorAuthenticationWebflowAutoConfiguration;
import org.apereo.cas.config.CasPersonDirectoryTestConfiguration;
import org.apereo.cas.config.CasWebflowAutoConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * This is {@link BaseAcceptableUsagePolicyActionTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    WebMvcAutoConfiguration.class,
    CasAcceptableUsagePolicyWebflowConfiguration.class,
    CasCoreUtilAutoConfiguration.class,
    CasWebflowAutoConfiguration.class,
    CasCoreWebAutoConfiguration.class,
    CasCoreNotificationsAutoConfiguration.class,
    CasCoreServicesAutoConfiguration.class,
    CasCookieAutoConfiguration.class,
    CasCoreAuditAutoConfiguration.class,
    CasCoreMultifactorAuthenticationAutoConfiguration.class,
    CasMultifactorAuthenticationWebflowAutoConfiguration.class,
    CasCoreAutoConfiguration.class,
    CasCoreLogoutAutoConfiguration.class,
    CasCoreTicketsAutoConfiguration.class,
    CasCoreAuthenticationAutoConfiguration.class,
    CasPersonDirectoryTestConfiguration.class
})
public abstract class BaseAcceptableUsagePolicyActionTests {
    @Autowired
    @Qualifier(AcceptableUsagePolicyRepository.BEAN_NAME)
    protected AcceptableUsagePolicyRepository acceptableUsagePolicyRepository;

    @Autowired
    protected ConfigurableApplicationContext applicationContext;

}
