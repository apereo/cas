package org.apereo.cas;

import org.apereo.cas.authentication.attribute.TenantPersonAttributeDaoBuilder;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.MockWebServer;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import java.net.URI;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link TenantRestfulPersonAttributeDaoBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@Tag("RestfulApi")
@ExtendWith(CasTestExtension.class)
@SpringBootTest(
    classes = BasePrincipalAttributeRepositoryTests.SharedTestConfiguration.class,
    properties = {
        "cas.multitenancy.core.enabled=true",
        "cas.multitenancy.json.location=classpath:/tenants.json"
    })
class TenantRestfulPersonAttributeDaoBuilderTests {
    @Autowired
    @Qualifier("restfulTenantPersonAttributeDaoBuilder")
    private TenantPersonAttributeDaoBuilder restfulTenantPersonAttributeDaoBuilder;

    @Autowired
    @Qualifier(TenantExtractor.BEAN_NAME)
    private TenantExtractor tenantExtractor;

    @Test
    void verifyOperation() throws Exception {
        val tenantDefinition = tenantExtractor.getTenantsManager().findTenant("resty").orElseThrow();
        val port = new URI(tenantDefinition.getProperties().get("cas.authn.attribute-repository.rest[0].url").toString()).getPort();
        val body = """
            {
               "name": "casuser",
               "age": 29,
               "messages": ["msg 1", "msg 2", "msg 3"]
            }
            """.stripIndent();
        try (val webServer = new MockWebServer(port, body)) {
            webServer.start();

            val results = restfulTenantPersonAttributeDaoBuilder.build(tenantDefinition);
            assertFalse(results.isEmpty());
            val attributeDao = results.getFirst();
            assertFalse(attributeDao.isDisposable());
            val person = attributeDao.getPerson("casuser");
            assertNotNull(person);
            assertEquals("casuser", person.getAttributeValue("name"));
            assertEquals(29, person.getAttributeValue("age"));
            assertEquals(3, person.getAttributeValues("messages").size());
        }
    }
}
