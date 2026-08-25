package org.apereo.cas.aup;

import module java.base;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.config.CasAcceptableUsagePolicyMongoDbAutoConfiguration;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;
import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.execution.Action;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link AcceptableUsagePolicyMultitenancyActionTests}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@SpringBootTest(classes = {
    CasAcceptableUsagePolicyMongoDbAutoConfiguration.class,
    BaseAcceptableUsagePolicyRepositoryTests.SharedTestConfiguration.class
},
    properties = {
        "CasFeatureModule.AcceptableUsagePolicy.mongo.enabled=false",
        "cas.acceptable-usage-policy.core.enabled=true",
        "cas.multitenancy.core.enabled=true",
        "cas.multitenancy.json.location=classpath:/tenants.json"
    })
@Tag("MongoDb")
@ExtendWith(CasTestExtension.class)
@Getter
@EnabledIfListeningOnPort(port = 27017)
class AcceptableUsagePolicyMultitenancyActionTests {
    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_AUP_VERIFY)
    private Action acceptableUsagePolicyVerifyAction;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    void verifyAction() throws Throwable {
        val user = UUID.randomUUID().toString();
        val context = MockRequestContext.create(applicationContext);
        context.addHeader(TenantExtractor.HEADER_TENANT_ID, "shire");
        WebUtils.putCredential(context, CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword());
        WebUtils.putTicketGrantingTicketInScopes(context, new MockTicketGrantingTicket(user));
        WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication(UUID.randomUUID().toString()), context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_AUP_MUST_ACCEPT, acceptableUsagePolicyVerifyAction.execute(context).getId());
    }
}
