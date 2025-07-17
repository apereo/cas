package org.apereo.cas.config;

import org.apereo.cas.BasePrincipalAttributeRepositoryTests;
import org.apereo.cas.authentication.principal.attribute.PersonAttributeDao;
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
 * This is {@link CasPersonDirectoryConfigurationCachingAttributeRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = BasePrincipalAttributeRepositoryTests.SharedTestConfiguration.class, properties = {
    "cas.authn.attribute-repository.json[0].location=classpath:/json-attribute-repository.json",
    "cas.authn.attribute-repository.core.expiration-time=30",
    "cas.authn.attribute-repository.core.expiration-time-unit=seconds"
})
@Tag("AttributeRepository")
@ExtendWith(CasTestExtension.class)
class CasPersonDirectoryConfigurationCachingAttributeRepositoryTests {
    @Autowired
    @Qualifier("cachingAttributeRepository")
    private PersonAttributeDao cachingAttributeRepository;

    /**
     * These two username produce the same hashcode
     * per the semantics put out by {@link String#hashCode()}.
     * The cache must be able to produce two different results
     * for each user while also maintaining a unique cache key for each.
     */
    @Test
    void verifyOperation() {
        val p1 = cachingAttributeRepository.getPerson("tensada");
        assertEquals("tensada", p1.getName());
        assertEquals("Tens", p1.getAttributeValue("oldName"));

        val p2 = cachingAttributeRepository.getPerson("friabili");
        assertEquals("friabili", p2.getName());
        assertEquals("Fri", p2.getAttributeValue("oldName"));
    }
}
