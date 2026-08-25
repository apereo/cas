package org.apereo.cas.aup;

import module java.base;
import org.apereo.cas.config.CasAcceptableUsagePolicyMongoDbAutoConfiguration;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
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
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link TenantMongoDbAcceptableUsagePolicyRepositoryBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@SpringBootTest(classes = {
    CasAcceptableUsagePolicyMongoDbAutoConfiguration.class,
    BaseAcceptableUsagePolicyRepositoryTests.SharedTestConfiguration.class
},
    properties = {
        "cas.acceptable-usage-policy.core.enabled=false",
        "cas.multitenancy.core.enabled=true",
        "cas.multitenancy.json.location=classpath:/tenants.json"
    })
@Tag("MongoDb")
@ExtendWith(CasTestExtension.class)
@Getter
@EnabledIfListeningOnPort(port = 27017)
public class TenantMongoDbAcceptableUsagePolicyRepositoryBuilderTests {
    @Autowired
    @Qualifier("mongoDbAcceptableUsagePolicyMultitenancyRepositoryBuilder")
    private TenantAcceptableUsagePolicyRepositoryBuilder repositoryBuilder;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    @Qualifier(TenantExtractor.BEAN_NAME)
    private TenantExtractor tenantExtractor;

    @Test
    void verifyOperation() throws Throwable {
        val authentication = RegisteredServiceTestUtils.getAuthentication(UUID.randomUUID().toString());
        val requestContext = MockRequestContext.create(applicationContext);
        requestContext.addHeader(TenantExtractor.HEADER_TENANT_ID, "shire");
        WebUtils.putAuthentication(authentication, requestContext);
        val tenantDefinition = tenantExtractor.extract(requestContext).orElseThrow();
        val repository = repositoryBuilder.build(tenantDefinition);
        assertNotNull(repository);
        val verification = repository.verify(requestContext);
        assertTrue(verification.getStatus().isFalse());
    }

    @Test
    void verifyTenantWithoutPolicy() throws Throwable {
        val authentication = RegisteredServiceTestUtils.getAuthentication(UUID.randomUUID().toString());
        val requestContext = MockRequestContext.create(applicationContext);
        requestContext.addHeader(TenantExtractor.HEADER_TENANT_ID, "london");
        WebUtils.putAuthentication(authentication, requestContext);
        val tenantDefinition = tenantExtractor.extract(requestContext).orElseThrow();
        val repository = repositoryBuilder.build(tenantDefinition);
        assertNull(repository);
    }
}
