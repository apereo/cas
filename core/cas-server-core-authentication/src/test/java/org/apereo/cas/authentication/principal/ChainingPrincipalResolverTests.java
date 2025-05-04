package org.apereo.cas.authentication.principal;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.principal.resolvers.ChainingPrincipalResolver;
import org.apereo.cas.config.CasCoreEnvironmentBootstrapAutoConfiguration;
import org.apereo.cas.config.CasCoreMultitenancyAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.core.authentication.PrincipalAttributesCoreProperties;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit test for {@link ChainingPrincipalResolver}.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@Tag("Attributes")
@SpringBootTest(classes = {
    CasCoreEnvironmentBootstrapAutoConfiguration.class,
    CasCoreMultitenancyAutoConfiguration.class
})
@ExtendWith(CasTestExtension.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@SpringBootTestAutoConfigurations
class ChainingPrincipalResolverTests {

    private final PrincipalFactory principalFactory = PrincipalFactoryUtils.newPrincipalFactory();

    @Autowired
    @Qualifier(TenantExtractor.BEAN_NAME)
    private TenantExtractor tenantExtractor;

    @Autowired
    private CasConfigurationProperties casProperties;

    private Principal mergeAndResolve(final Principal principal, final Credential credential,
                                      final PrincipalResolver resolver1, final PrincipalResolver resolver2,
                                      final PrincipalAttributesCoreProperties.MergingStrategyTypes mergerType) throws Throwable {
        val props = new CasConfigurationProperties();
        props
            .getAuthn()
            .getAttributeRepository()
            .getCore()
            .setMerger(mergerType);
        val resolver = buildResolver(List.of(resolver1, resolver2), props);

        return resolver.resolve(credential,
            Optional.of(principal),
            Optional.of(new SimpleTestUsernamePasswordAuthenticationHandler()),
            Optional.of(CoreAuthenticationTestUtils.getService()));
    }

    @Test
    void examineSupports() {
        val credential = mock(Credential.class);
        when(credential.getId()).thenReturn("a");

        val resolver1 = mock(PrincipalResolver.class);
        when(resolver1.supports(eq(credential))).thenReturn(true);

        val resolver2 = mock(PrincipalResolver.class);
        when(resolver2.supports(eq(credential))).thenReturn(false);

        val resolver = buildResolver(List.of(resolver1, resolver2), casProperties);
        assertTrue(resolver.supports(credential));
    }

    @Test
    void examineResolve() throws Throwable {
        val principalOut = principalFactory.createPrincipal("output");
        val credential = mock(Credential.class);
        when(credential.getId()).thenReturn("input");

        val resolver1 = mock(PrincipalResolver.class);
        when(resolver1.supports(eq(credential))).thenReturn(true);
        when(resolver1.resolve(eq(credential), any(Optional.class), any(Optional.class), any(Optional.class))).thenReturn(principalOut);

        val resolver2 = mock(PrincipalResolver.class);
        when(resolver2.supports(any(Credential.class))).thenReturn(true);
        when(resolver2.resolve(any(Credential.class), any(Optional.class), any(Optional.class), any(Optional.class)))
            .thenReturn(principalFactory.createPrincipal("output", Collections.singletonMap("mail", List.of("final@example.com"))));

        val resolver = buildResolver(List.of(resolver1, resolver2), casProperties);
        val principal = resolver.resolve(credential,
            Optional.of(principalOut),
            Optional.of(new SimpleTestUsernamePasswordAuthenticationHandler()),
            Optional.of(CoreAuthenticationTestUtils.getService()));
        assertEquals("output", principal.getId());
        val mail = CollectionUtils.firstElement(principal.getAttributes().get("mail"));
        assertTrue(mail.isPresent());
        assertEquals("final@example.com", mail.get());
    }

    @Test
    void examineResolverMergingAttributes() throws Throwable {
        val p1 = principalFactory.createPrincipal("casuser", Map.of("familyName", List.of("Smith")));
        val p2 = principalFactory.createPrincipal("casuser", Map.of("familyName", List.of("smith")));

        val credential = mock(Credential.class);
        when(credential.getId()).thenReturn("input");

        val resolver1 = mock(PrincipalResolver.class);
        when(resolver1.supports(eq(credential))).thenReturn(true);
        when(resolver1.resolve(eq(credential), any(Optional.class), any(Optional.class), any(Optional.class))).thenReturn(p1);

        val resolver2 = mock(PrincipalResolver.class);
        when(resolver2.supports(any(Credential.class))).thenReturn(true);
        when(resolver2.resolve(any(Credential.class), any(Optional.class), any(Optional.class), any(Optional.class))).thenReturn(p2);

        var finalResult = mergeAndResolve(p1, credential, resolver1, resolver2, PrincipalAttributesCoreProperties.MergingStrategyTypes.REPLACE);
        assertTrue(finalResult.getAttributes().containsValue(List.of("smith")));

        finalResult = mergeAndResolve(p1, credential, resolver1, resolver2, PrincipalAttributesCoreProperties.MergingStrategyTypes.ADD);
        assertTrue(finalResult.getAttributes().containsValue(List.of("Smith")));

        /*
            Distinct values are set to true for multivalued merger strategies.
            This means the final attribute collection will only collect one value for Smith.
         */
        finalResult = mergeAndResolve(p1, credential, resolver1, resolver2, PrincipalAttributesCoreProperties.MergingStrategyTypes.MULTIVALUED);
        assertTrue(finalResult.getAttributes().containsValue(List.of("Smith")));
    }

    private PrincipalResolver buildResolver(final List<PrincipalResolver> resolvers, final CasConfigurationProperties casProperties) {
        return new ChainingPrincipalResolver(new DefaultPrincipalElectionStrategy(),
            tenantExtractor, resolvers, casProperties);
    }
}
