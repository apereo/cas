package org.apereo.cas.jdbc;

import org.apereo.cas.authentication.attribute.TenantPersonAttributeDaoBuilder;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link TenantJdbcPersonAttributeDaoBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@EnabledIfListeningOnPort(port = 5432)
@Tag("Postgres")
@TestPropertySource(properties = {
    "cas.multitenancy.core.enabled=true",
    "cas.multitenancy.json.location=classpath:/tenants.json"
})
class TenantJdbcPersonAttributeDaoBuilderTests extends BaseJdbcAttributeRepositoryTests {
    @Autowired
    @Qualifier("jdbcTenantPersonAttributeDaoBuilder")
    private TenantPersonAttributeDaoBuilder jdbcTenantPersonAttributeDaoBuilder;

    @Autowired
    @Qualifier(TenantExtractor.BEAN_NAME)
    private TenantExtractor tenantExtractor;
    

    @Test
    void verifySingleRowAttributeRepository() {
        val tenantDefinition = tenantExtractor.getTenantsManager().findTenant("attributes").orElseThrow();
        val repository = jdbcTenantPersonAttributeDaoBuilder.build(tenantDefinition).getFirst();
        val person = repository.getPerson("caskeycloak@example.org");
        assertNotNull(person);
        assertFalse(person.getAttributes().isEmpty());
    }
}
