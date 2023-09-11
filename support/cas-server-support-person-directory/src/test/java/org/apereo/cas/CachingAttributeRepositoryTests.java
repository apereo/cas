package org.apereo.cas;

import lombok.val;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CachingAttributeRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = BasePrincipalAttributeRepositoryTests.SharedTestConfiguration.class,
    properties = {
        "cas.authn.attribute-repository.stub.attributes.uid=uid",
        "cas.authn.attribute-repository.stub.attributes.givenName=givenName",
        "cas.authn.attribute-repository.stub.attributes.eppn=eppn"
    })
@Tag("Attributes")
class CachingAttributeRepositoryTests {
    @Autowired
    @Qualifier("cachingAttributeRepository")
    private IPersonAttributeDao cachingAttributeRepository;

    @Test
    void verifyRepositoryCaching() throws Throwable {
        val person1 = cachingAttributeRepository.getPerson("casuser");
        assertEquals("casuser", person1.getName());
        assertEquals(4, person1.getAttributes().size());

        /*
         * The second call should not
         * go out to the repositories again
         */
        val person2 = cachingAttributeRepository.getPerson("casuser");
        assertEquals(4, person2.getAttributes().size());
    }
}
