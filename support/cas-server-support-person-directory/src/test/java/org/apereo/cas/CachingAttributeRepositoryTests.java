package org.apereo.cas;

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
@Tag("AttributeRepository")
@ExtendWith(CasTestExtension.class)
class CachingAttributeRepositoryTests {
    @Autowired
    @Qualifier("cachingAttributeRepository")
    private PersonAttributeDao cachingAttributeRepository;

    @Test
    void verifyRepositoryCaching() {
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
