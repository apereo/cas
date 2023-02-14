package org.apereo.cas.web.saml2;

import org.apereo.cas.config.DelegatedAuthenticationSAMLConfiguration;
import org.apereo.cas.support.saml.util.Saml20ObjectBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.AuthenticatedAssertionContext;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileBuilderContext;
import org.apereo.cas.support.saml.web.idp.profile.builders.response.SamlIdPResponseCustomizer;
import org.apereo.cas.web.BaseDelegatedAuthenticationTests;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AuthenticatingAuthority;
import org.opensaml.saml.saml2.core.AuthnContext;
import org.opensaml.saml.saml2.core.AuthnStatement;
import org.pac4j.core.util.Pac4jConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

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
@SpringBootTest(classes = {
    BaseDelegatedAuthenticationTests.SharedTestConfiguration.class,
    DelegatedAuthenticationSAMLConfiguration.class
},
    properties = {
        "cas.authn.pac4j.saml[0].metadata.service-provider.file-system.location=/tmp/sp-metadata.xml",
        "cas.authn.pac4j.saml[0].metadata.identity-provider-metadata-path=src/test/resources/idp-metadata.xml"
    })
@Tag("SAML2Web")
public class DelegatedAuthenticationSamlIdPResponseCustomizerTests {
    @Autowired
    @Qualifier("delegatedSaml2IdPResponseCustomizer")
    private SamlIdPResponseCustomizer delegatedSaml2IdPResponseCustomizer;

    @Test
    public void verifyOperation() throws Exception {
        val assertion = mock(Assertion.class);
        val authnStatement = mock(AuthnStatement.class);
        val authnContext = mock(AuthnContext.class);
        val listofAuthorities = new ArrayList<AuthenticatingAuthority>();
        when(authnContext.getAuthenticatingAuthorities()).thenReturn(listofAuthorities);
        when(authnStatement.getAuthnContext()).thenReturn(authnContext);
        when(assertion.getAuthnStatements()).thenReturn(List.of(authnStatement));
        val context = SamlProfileBuilderContext.builder()
            .authenticatedAssertion(Optional.of(AuthenticatedAssertionContext.builder()
                .name("casuser")
                .attributes(Map.of(Pac4jConstants.CLIENT_NAME, "SAML2Client"))
                .build()))
            .build();

        val builder = mock(Saml20ObjectBuilder.class);
        val authority = mock(AuthenticatingAuthority.class);
        when(builder.newSamlObject(any())).thenReturn(authority);
        assertDoesNotThrow(() -> delegatedSaml2IdPResponseCustomizer.customizeAssertion(context, builder, assertion));
        verify(authority, times(1)).setURI(anyString());
        assertEquals(1, listofAuthorities.size());
    }
}
