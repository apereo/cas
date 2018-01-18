package org.apereo.cas;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.config.CasPersonDirectoryConfiguration;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.apereo.services.persondir.IPersonAttributes;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

/**
 * This is {@link CachingAttributeRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
    CasPersonDirectoryConfiguration.class,
    RefreshAutoConfiguration.class})
@TestPropertySource(locations = {"classpath:/persondirectory.properties"})
@Slf4j
public class CachingAttributeRepositoryTests {

    @Autowired
    @Qualifier("cachingAttributeRepository")
    private IPersonAttributeDao cachingAttributeRepository;

    @Test
    public void verifyRepositoryCaching() {
        final IPersonAttributes person1 = cachingAttributeRepository.getPerson("casuser");
        assertEquals("casuser", person1.getName());
        assertEquals(4, person1.getAttributes().size());

        // The second call should not go out to the repositories again
        final IPersonAttributes person2 = cachingAttributeRepository.getPerson("casuser");
        assertEquals(4, person2.getAttributes().size());
    }
}
