package org.apereo.cas.web.saml2;

import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.util.Saml20ObjectBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.AuthenticatedAssertionContext;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileBuilderContext;
import org.apereo.cas.support.saml.web.idp.profile.builders.response.SamlIdPResponseCustomizer;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.util.RandomUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AuthenticatingAuthority;
import org.opensaml.saml.saml2.core.AuthnContext;
import org.opensaml.saml.saml2.core.AuthnStatement;
import org.pac4j.core.util.Pac4jConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DelegatedAuthenticationSamlIdPResponseCustomizerTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@SpringBootTest(classes = BaseSaml2DelegatedAuthenticationTests.SharedTestConfiguration.class)
@Tag("SAML2Web")
@ExtendWith(CasTestExtension.class)
class DelegatedAuthenticationSamlIdPResponseCustomizerTests {
    @Autowired
    @Qualifier("delegatedSaml2IdPResponseCustomizer")
    private SamlIdPResponseCustomizer delegatedSaml2IdPResponseCustomizer;

    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    private ServicesManager servicesManager;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    void verifyOperation() throws Throwable {
        val requestContext = MockRequestContext.create(applicationContext);
        
        val assertion = mock(Assertion.class);
        val authnStatement = mock(AuthnStatement.class);
        val authnContext = mock(AuthnContext.class);
        val listOfAuthorities = new ArrayList<AuthenticatingAuthority>();
        when(authnContext.getAuthenticatingAuthorities()).thenReturn(listOfAuthorities);
        when(authnStatement.getAuthnContext()).thenReturn(authnContext);
        when(assertion.getAuthnStatements()).thenReturn(List.of(authnStatement));

        val registeredService = new SamlRegisteredService();
        registeredService.setId(RandomUtils.nextInt());
        registeredService.setName("SAML");
        registeredService.setServiceId("https://samltest.id/saml/sp");
        registeredService.setMetadataLocation("https://samltest.id/saml/sp");
        servicesManager.save(registeredService);

        val context = SamlProfileBuilderContext.builder()
            .authenticatedAssertion(Optional.of(AuthenticatedAssertionContext.builder()
                .name("casuser")
                .attributes(Map.of(Pac4jConstants.CLIENT_NAME, "SAML2Client"))
                .build()))
            .registeredService(registeredService)
            .httpRequest(requestContext.getHttpServletRequest())
            .httpResponse(requestContext.getHttpServletResponse())
            .build();

        val builder = mock(Saml20ObjectBuilder.class);
        val authority = mock(AuthenticatingAuthority.class);
        when(builder.newSamlObject(any())).thenReturn(authority);
        assertDoesNotThrow(() -> delegatedSaml2IdPResponseCustomizer.customizeAssertion(context, builder, assertion));
        verify(authority, times(1)).setURI(anyString());
        assertEquals(1, listOfAuthorities.size());
    }
}
