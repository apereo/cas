package org.apereo.cas;

import org.apereo.cas.authentication.attribute.TenantPersonAttributeDaoBuilder;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.test.CasTestExtension;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link TenantStubPersonAttributeDaoBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@Tag("Attributes")
@ExtendWith(CasTestExtension.class)
@SpringBootTest(
    classes = BasePrincipalAttributeRepositoryTests.SharedTestConfiguration.class,
    properties = {
        "cas.multitenancy.core.enabled=true",
        "cas.multitenancy.json.location=classpath:/tenants.json"
    })
class TenantStubPersonAttributeDaoBuilderTests {
    @Autowired
    @Qualifier("stubTenantPersonAttributeDaoBuilder")
    private TenantPersonAttributeDaoBuilder stubTenantPersonAttributeDaoBuilder;

    @Autowired
    @Qualifier(TenantExtractor.BEAN_NAME)
    private TenantExtractor tenantExtractor;

    @Test
    void verifyOperation() {
        val tenantDefinition = tenantExtractor.getTenantsManager().findTenant("shire").orElseThrow();
        val results = stubTenantPersonAttributeDaoBuilder.build(tenantDefinition);
        assertFalse(results.isEmpty());
        val attributeDao = results.getFirst();
        assertFalse(attributeDao.isDisposable());
        val person = attributeDao.getPerson("casuser");
        assertNotNull(person);
        assertTrue(person.getAttributes().containsKey("name"));
        assertTrue(person.getAttributes().containsKey("department"));
    }
}
