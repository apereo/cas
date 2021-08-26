package org.apereo.cas.authentication.principal;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.principal.resolvers.ChainingPrincipalResolver;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.core.authentication.PrincipalAttributesCoreProperties;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import java.util.Arrays;
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
@SpringBootTest(classes = RefreshAutoConfiguration.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class ChainingPrincipalResolverTests {

    private final PrincipalFactory principalFactory = PrincipalFactoryUtils.newPrincipalFactory();

    @Autowired
    private CasConfigurationProperties casProperties;

    @Test
    public void examineSupports() {
        val credential = mock(Credential.class);
        when(credential.getId()).thenReturn("a");

        val resolver1 = mock(PrincipalResolver.class);
        when(resolver1.supports(eq(credential))).thenReturn(true);

        val resolver2 = mock(PrincipalResolver.class);
        when(resolver2.supports(eq(credential))).thenReturn(false);

        val resolver = new ChainingPrincipalResolver(new DefaultPrincipalElectionStrategy(), casProperties);
        resolver.setChain(Arrays.asList(resolver1, resolver2));
        assertTrue(resolver.supports(credential));
    }

    @Test
    public void examineResolve() {
        val principalOut = principalFactory.createPrincipal("output");
        val credential = mock(Credential.class);
        when(credential.getId()).thenReturn("input");

        val resolver1 = mock(PrincipalResolver.class);
        when(resolver1.supports(eq(credential))).thenReturn(true);
        when(resolver1.resolve(eq(credential), any(Optional.class), any(Optional.class))).thenReturn(principalOut);

        val resolver2 = mock(PrincipalResolver.class);
        when(resolver2.supports(any(Credential.class))).thenReturn(true);
        when(resolver2.resolve(any(Credential.class), any(Optional.class), any(Optional.class)))
            .thenReturn(principalFactory.createPrincipal("output", Collections.singletonMap("mail", List.of("final@example.com"))));

        val resolver = new ChainingPrincipalResolver(new DefaultPrincipalElectionStrategy(), casProperties);
        resolver.setChain(Arrays.asList(resolver1, resolver2));
        val principal = resolver.resolve(credential,
            Optional.of(principalOut),
            Optional.of(new SimpleTestUsernamePasswordAuthenticationHandler()));
        assertEquals("output", principal.getId());
        assertNotNull(resolver.getAttributeRepository());
        val mail = CollectionUtils.firstElement(principal.getAttributes().get("mail"));
        assertTrue(mail.isPresent());
        assertEquals("final@example.com", mail.get());
    }

    @Test
    public void examineResolverMergingAttributes() {
        val p1 = principalFactory.createPrincipal("casuser", Map.of("familyName", List.of("Smith")));
        val p2 = principalFactory.createPrincipal("casuser", Map.of("familyName", List.of("smith")));

        val credential = mock(Credential.class);
        when(credential.getId()).thenReturn("input");

        val resolver1 = mock(PrincipalResolver.class);
        when(resolver1.supports(eq(credential))).thenReturn(true);
        when(resolver1.resolve(eq(credential), any(Optional.class), any(Optional.class))).thenReturn(p1);

        val resolver2 = mock(PrincipalResolver.class);
        when(resolver2.supports(any(Credential.class))).thenReturn(true);
        when(resolver2.resolve(any(Credential.class), any(Optional.class), any(Optional.class))).thenReturn(p2);

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

    private static Principal mergeAndResolve(final Principal p1, final Credential credential,
                                      final PrincipalResolver resolver1, final PrincipalResolver resolver2,
                                      final PrincipalAttributesCoreProperties.MergingStrategyTypes mergerType) {
        val props = new CasConfigurationProperties();
        props
            .getAuthn()
            .getAttributeRepository()
            .getCore()
            .setMerger(mergerType);
        val resolver = new ChainingPrincipalResolver(new DefaultPrincipalElectionStrategy(), props);
        resolver.setChain(Arrays.asList(resolver1, resolver2));

        return resolver.resolve(credential,
            Optional.of(p1), Optional.of(new SimpleTestUsernamePasswordAuthenticationHandler()));
    }

}
