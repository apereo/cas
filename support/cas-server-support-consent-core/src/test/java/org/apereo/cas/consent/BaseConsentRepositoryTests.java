package org.apereo.cas.consent;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.config.CasConsentCoreAutoConfiguration;
import org.apereo.cas.config.CasCoreAuditAutoConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreAutoConfiguration;
import org.apereo.cas.config.CasCoreCookieAutoConfiguration;
import org.apereo.cas.config.CasCoreLogoutAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationWebflowAutoConfiguration;
import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreServicesAutoConfiguration;
import org.apereo.cas.config.CasCoreTicketsAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.config.CasCoreWebflowAutoConfiguration;
import org.apereo.cas.config.CasPersonDirectoryTestConfiguration;
import org.apereo.cas.services.BaseRegisteredService;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.Import;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import static org.awaitility.Awaitility.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link BaseConsentRepositoryTests}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@SpringBootTest(classes = BaseConsentRepositoryTests.SharedTestConfiguration.class)
@Getter
public abstract class BaseConsentRepositoryTests {
    protected static final DefaultConsentDecisionBuilder BUILDER = new DefaultConsentDecisionBuilder(CipherExecutor.noOpOfSerializableToString());

    protected static final Service SVC = RegisteredServiceTestUtils.getService();

    protected static final BaseRegisteredService REG_SVC = RegisteredServiceTestUtils.getRegisteredService(SVC.getId());

    protected static final Map<String, List<Object>> ATTR = CollectionUtils.wrap("attribute", List.of("value"));

    public abstract ConsentRepository getRepository();
    
    @Test
    void verifyConsentDecisionIsNotFound() throws Throwable {
        val user = getUser();
        val repo = getRepository();
        val decision = BUILDER.build(SVC, REG_SVC, user, ATTR);
        decision.setId(1);
        assertNotNull(repo.storeConsentDecision(decision));
        assertFalse(repo.findConsentDecisions().isEmpty());
        assertFalse(repo.findConsentDecisions(user).isEmpty());
        assertNull(repo.findConsentDecision(SVC, REG_SVC, CoreAuthenticationTestUtils.getAuthentication()));
        await().untilAsserted(() -> assertFalse(repo.deleteConsentDecision(decision.getId(), UUID.randomUUID().toString())));
    }

    @Test
    void verifyConsentDecisionIsFound() throws Throwable {
        val user = getUser();
        val repo = getRepository();
        var decision = BUILDER.build(SVC, REG_SVC, user, ATTR);
        decision.setId(100);
        decision = repo.storeConsentDecision(decision);
        assertNotNull(decision);
        decision = repo.storeConsentDecision(decision);
        assertNotNull(decision);

        val d = repo.findConsentDecision(SVC, REG_SVC, CoreAuthenticationTestUtils.getAuthentication(user));
        assertNotNull(d);
        assertEquals(user, d.getPrincipal());

        assertTrue(repo.deleteConsentDecision(d.getId(), d.getPrincipal()));
        await().untilAsserted(() -> assertNull(repo.findConsentDecision(SVC, REG_SVC,
            CoreAuthenticationTestUtils.getAuthentication(user))));

    }

    @Test
    void verifyDeleteRecordsForPrincipal() throws Throwable {
        val user = getUser();
        val repo = getRepository();
        repo.deleteAll();
        val decision = BUILDER.build(SVC, REG_SVC, user, ATTR);

        decision.setId(200);
        val result = repo.storeConsentDecision(decision);
        assertNotNull(result);
        await().untilAsserted(() -> assertTrue(repo.deleteConsentDecisions(result.getPrincipal())));
        await().untilAsserted(() ->
            assertNull(repo.findConsentDecision(SVC, REG_SVC, CoreAuthenticationTestUtils.getAuthentication(user))));
    }

    protected String getUser() {
        return RandomUtils.randomAlphanumeric(8);
    }

    @ImportAutoConfiguration({
        AopAutoConfiguration.class,
        RefreshAutoConfiguration.class,
        WebMvcAutoConfiguration.class,
        ServletWebServerFactoryAutoConfiguration.class,
        DispatcherServletAutoConfiguration.class
    })
    @SpringBootConfiguration
    @Import({
        CasConsentCoreAutoConfiguration.class,
        CasCoreUtilAutoConfiguration.class,
        CasCoreWebAutoConfiguration.class,
        CasCoreLogoutAutoConfiguration.class,
        CasCoreWebflowAutoConfiguration.class,
        CasCoreCookieAutoConfiguration.class,
        CasCoreNotificationsAutoConfiguration.class,
        CasCoreServicesAutoConfiguration.class,
        CasCoreMultifactorAuthenticationAutoConfiguration.class,
        CasCoreMultifactorAuthenticationWebflowAutoConfiguration.class,
        CasPersonDirectoryTestConfiguration.class,
        CasCoreAuthenticationAutoConfiguration.class,
        CasCoreTicketsAutoConfiguration.class,
        CasCoreAuditAutoConfiguration.class,
        CasCoreAutoConfiguration.class
    })
    public static class SharedTestConfiguration {
    }
}
