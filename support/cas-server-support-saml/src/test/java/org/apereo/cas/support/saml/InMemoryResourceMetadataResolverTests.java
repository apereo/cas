package org.apereo.cas.support.saml;

import com.google.common.collect.Iterables;
import lombok.val;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.saml.criterion.EntityRoleCriterion;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link InMemoryResourceMetadataResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@SpringBootTest(classes = AbstractOpenSamlTests.SharedTestConfiguration.class,
    properties = "spring.main.allow-bean-definition-overriding=true")
@Tag("SAMLMetadata")
public class InMemoryResourceMetadataResolverTests extends AbstractOpenSamlTests {

    @Test
    public void verifyValidMetadataResource() throws Exception {
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
    public void verifyExpiredValidUntilMetadataResource() throws Exception {
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
    public void verifyInvalidExpiredMetadataResourceIsOkay() throws Exception {
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


