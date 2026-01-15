package org.apereo.cas.support.saml;

import module java.base;
import org.apereo.cas.test.CasTestExtension;
import com.google.common.collect.Iterables;
import lombok.val;
import net.shibboleth.shared.resolver.CriteriaSet;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.saml.criterion.EntityRoleCriterion;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link InMemoryResourceMetadataResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@SpringBootTest(classes = AbstractOpenSamlTests.SharedTestConfiguration.class)
@Tag("SAMLMetadata")
@ExtendWith(CasTestExtension.class)
class InMemoryResourceMetadataResolverTests extends AbstractOpenSamlTests {

    @Test
    void verifyValidMetadataResource() throws Throwable {
        val resolver = new InMemoryResourceMetadataResolver(new ClassPathResource("metadata/metadata-valid.xml"), configBean);
        resolver.setId(UUID.randomUUID().toString());
        resolver.initialize();
        
        val criteriaSet = new CriteriaSet();
        criteriaSet.add(new EntityIdCriterion("urn:app.e2ma.net"));
        criteriaSet.add(new EntityRoleCriterion(SPSSODescriptor.DEFAULT_ELEMENT_NAME));
        val resolved = resolver.resolve(criteriaSet);
        assertFalse(Iterables.isEmpty(resolved));
    }

    @Test
    void verifyExpiredValidUntilMetadataResource() throws Throwable {
        val resolver = new InMemoryResourceMetadataResolver(new ClassPathResource("metadata/metadata-expired.xml"), configBean);
        resolver.setId(UUID.randomUUID().toString());
        resolver.initialize();

        val criteriaSet = new CriteriaSet();
        criteriaSet.add(new EntityIdCriterion("urn:app.e2ma.net"));
        criteriaSet.add(new EntityRoleCriterion(SPSSODescriptor.DEFAULT_ELEMENT_NAME));
        val resolved = resolver.resolve(criteriaSet);
        assertTrue(Iterables.isEmpty(resolved));
    }

    @Test
    void verifyInvalidExpiredMetadataResourceIsOkay() throws Throwable {
        val resolver = new InMemoryResourceMetadataResolver(new ClassPathResource("metadata/metadata-expired.xml"), configBean);
        resolver.setRequireValidMetadata(false);
        resolver.setId(UUID.randomUUID().toString());
        resolver.initialize();

        val criteriaSet = new CriteriaSet();
        criteriaSet.add(new EntityIdCriterion("urn:app.e2ma.net"));
        criteriaSet.add(new EntityRoleCriterion(SPSSODescriptor.DEFAULT_ELEMENT_NAME));
        val resolved = resolver.resolve(criteriaSet);
        assertFalse(Iterables.isEmpty(resolved));
    }
}


