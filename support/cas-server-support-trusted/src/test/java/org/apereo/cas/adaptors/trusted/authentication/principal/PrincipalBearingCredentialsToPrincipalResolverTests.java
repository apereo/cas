package org.apereo.cas.adaptors.trusted.authentication.principal;

import module java.base;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.CoreAuthenticationUtils;
import org.apereo.cas.authentication.attribute.AttributeDefinitionStore;
import org.apereo.cas.authentication.attribute.AttributeRepositoryResolver;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.attribute.PersonAttributeDao;
import org.apereo.cas.authentication.principal.resolvers.PrincipalResolutionContext;
import org.apereo.cas.configuration.model.core.authentication.PrincipalAttributesCoreProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.CollectionUtils;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
@Tag("Attributes")
@SpringBootTest(classes = RefreshAutoConfiguration.class)
@ExtendWith(CasTestExtension.class)
class PrincipalBearingCredentialsToPrincipalResolverTests {
    @Autowired
    private ConfigurableApplicationContext applicationContext;
    
    private PrincipalBearingPrincipalResolver resolver;

    @Mock
    private ServicesManager servicesManager;

    @Mock
    private AttributeDefinitionStore attributeDefinitionStore;

    @Mock
    private AttributeRepositoryResolver attributeRepositoryResolver;
    
    @BeforeEach
    void initialize() throws Exception {
        MockitoAnnotations.openMocks(this).close();
        val context = PrincipalResolutionContext.builder()
            .attributeDefinitionStore(attributeDefinitionStore)
            .servicesManager(servicesManager)
            .attributeRepositoryResolver(attributeRepositoryResolver)
            .attributeMerger(CoreAuthenticationUtils.getAttributeMerger(PrincipalAttributesCoreProperties.MergingStrategyTypes.REPLACE))
            .attributeRepository(CoreAuthenticationTestUtils.getAttributeRepository())
            .principalFactory(PrincipalFactoryUtils.newPrincipalFactory())
            .returnNullIfNoAttributes(false)
            .principalNameTransformer(formUserId -> formUserId)
            .useCurrentPrincipalId(false)
            .resolveAttributes(true)
            .applicationContext(applicationContext)
            .activeAttributeRepositoryIdentifiers(CollectionUtils.wrapSet(PersonAttributeDao.WILDCARD))
            .build();
        this.resolver = new PrincipalBearingPrincipalResolver(context);
    }

    @Test
    void verifySupports() throws Throwable {
        val credential = new PrincipalBearingCredential(PrincipalFactoryUtils.newPrincipalFactory().createPrincipal("test"));
        assertTrue(this.resolver.supports(credential));
        assertFalse(this.resolver.supports(new UsernamePasswordCredential()));
        assertFalse(this.resolver.supports(null));
    }

    @Test
    void verifyReturnedPrincipal() throws Throwable {
        val credential = new PrincipalBearingCredential(PrincipalFactoryUtils.newPrincipalFactory().createPrincipal("test"));
        val p = this.resolver.resolve(credential,
            Optional.of(CoreAuthenticationTestUtils.getPrincipal()),
            Optional.of(new SimpleTestUsernamePasswordAuthenticationHandler()),
            Optional.of(CoreAuthenticationTestUtils.getService()));
        assertEquals("test", p.getId());
    }

}
