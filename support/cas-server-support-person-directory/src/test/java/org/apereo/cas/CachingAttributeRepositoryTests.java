package org.apereo.cas;

import org.apereo.cas.config.CasPersonDirectoryConfiguration;

import lombok.val;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.apereo.services.persondir.IPersonAttributeDaoFilter;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CachingAttributeRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = {
    CasPersonDirectoryConfiguration.class,
    RefreshAutoConfiguration.class
},
    properties = {
        "cas.authn.attribute-repository.stub.attributes.uid=uid",
        "cas.authn.attribute-repository.stub.attributes.givenName=givenName",
        "cas.authn.attribute-repository.stub.attributes.eppn=eppn"
    })
@Tag("Attributes")
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
}
