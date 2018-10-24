package org.apereo.cas;

import org.apereo.cas.config.CasPersonDirectoryConfiguration;

import lombok.val;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import static org.junit.Assert.*;

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
public class CachingAttributeRepositoryTests {
    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Autowired
    @Qualifier("cachingAttributeRepository")
    private IPersonAttributeDao cachingAttributeRepository;

    @Test
    public void verifyRepositoryCaching() {
        val person1 = cachingAttributeRepository.getPerson("casuser");
        assertEquals("casuser", person1.getName());
        assertEquals(4, person1.getAttributes().size());

        // The second call should not go out to the repositories again
        val person2 = cachingAttributeRepository.getPerson("casuser");
        assertEquals(4, person2.getAttributes().size());
    }
}
