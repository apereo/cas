package org.apereo.cas;

import org.apereo.cas.config.CasPersonDirectoryConfiguration;

import lombok.extern.slf4j.Slf4j;

import lombok.val;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.apereo.services.persondir.IPersonAttributeDaoFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CachingAttributeRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = {
    CasPersonDirectoryConfiguration.class,
    RefreshAutoConfiguration.class})
@TestPropertySource(properties = {
    "cas.authn.attributeRepository.stub.attributes.uid=uid",
    "cas.authn.attributeRepository.stub.attributes.givenName=givenName",
    "cas.authn.attributeRepository.stub.attributes.eppn=eppn"
})
@Slf4j
public class CachingAttributeRepositoryTests {
    @Autowired
    @Qualifier("cachingAttributeRepository")
    private IPersonAttributeDao cachingAttributeRepository;

    @Test
    public void verifyRepositoryCaching() {
        val person1 = cachingAttributeRepository.getPerson("casuser", IPersonAttributeDaoFilter.alwaysChoose());
        assertEquals("casuser", person1.getName());
        assertEquals(4, person1.getAttributes().size());

        /*
         * The second call should not
         * go out to the repositories again
         */
        val person2 = cachingAttributeRepository.getPerson("casuser", IPersonAttributeDaoFilter.alwaysChoose());
        assertEquals(4, person2.getAttributes().size());
    }

    @Test
    public void loadTestRepositoryCaching() {
        try {
            var lastTime = System.currentTimeMillis();
            for (int i = 1; i <= 10; i++) {
                for (int j = 1; j <= 25000; j++) {
                    val person1 = cachingAttributeRepository.getPerson("casuser" + j, IPersonAttributeDaoFilter.alwaysChoose());
                    assertEquals(4, person1.getAttributes().size());
                    if (j % 1000 == 0) {
                        val now = System.currentTimeMillis();
                        LOGGER.debug("{}  - {} Time: {}", i, j, now - lastTime);
                        lastTime = now;
                    }
                    if (j % 10000 == 0) {
                        LOGGER.debug(j + " mod 10000");
                    }
                }
            }
        } catch (final StackOverflowError t) {
            LOGGER.error("Error using cache: ", t);
            fail("Stack overflow using cache");
        }
    }
}
