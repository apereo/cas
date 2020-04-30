package org.apereo.cas.support.saml.util;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.config.CoreSamlConfigurationTests;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.SamlUtils;
import org.apereo.cas.ticket.expiration.TicketGrantingTicketExpirationPolicy;
import org.apereo.cas.util.EncodingUtils;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.xerces.xs.XSObject;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.opensaml.core.xml.schema.XSBase64Binary;
import org.opensaml.core.xml.schema.XSBoolean;
import org.opensaml.core.xml.schema.XSDateTime;
import org.opensaml.core.xml.schema.XSInteger;
import org.opensaml.core.xml.schema.XSString;
import org.opensaml.core.xml.schema.XSURI;
import org.opensaml.saml.saml2.core.NameID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link NonInflatingSaml20ObjectBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAML")
@SpringBootTest(classes = CoreSamlConfigurationTests.SharedTestConfiguration.class)
public class NonInflatingSaml20ObjectBuilderTests {
    @Autowired
    @Qualifier("shibboleth.OpenSAMLConfig")
    private OpenSamlConfigBean openSamlConfigBean;
    
    @Test
    public void verifyAttrValueTypeString() {
        val builder = new NonInflatingSaml20ObjectBuilder(openSamlConfigBean);
        assertNotNull(builder.newAttribute("email", "mail",
            List.of("cas@example.org"),
            Map.of("mail", "basic"), "basic",
            Map.of("mail", XSString.class.getSimpleName())));
    }

    @Test
    public void verifyAttrValueTypeUri() {
        val builder = new NonInflatingSaml20ObjectBuilder(openSamlConfigBean);
        assertNotNull(builder.newAttribute("email", "mail",
            List.of("cas@example.org"),
            Map.of("mail", "basic"), "basic",
            Map.of("mail", XSURI.class.getSimpleName())));
    }

    @Test
    public void verifyAttrValueTypeXSBoolean() {
        val builder = new NonInflatingSaml20ObjectBuilder(openSamlConfigBean);
        assertNotNull(builder.newAttribute("email", "mail",
            List.of("false"),
            Map.of("mail", "basic"), "basic",
            Map.of("mail", XSBoolean.class.getSimpleName())));
    }

    @Test
    public void verifyAttrValueTypeXSInteger() {
        val builder = new NonInflatingSaml20ObjectBuilder(openSamlConfigBean);
        assertNotNull(builder.newAttribute("email", "mail",
            List.of("12345678"),
            Map.of("mail", "basic"), "basic",
            Map.of("mail", XSInteger.class.getSimpleName())));
    }

    @Test
    public void verifyAttrValueTypeXSDateTime() {
        val builder = new NonInflatingSaml20ObjectBuilder(openSamlConfigBean);
        assertNotNull(builder.newAttribute("email", "mail",
            List.of(ZonedDateTime.now(ZoneOffset.UTC).toString()),
            Map.of("mail", "basic"), "basic",
            Map.of("mail", XSDateTime.class.getSimpleName())));
    }

    @Test
    public void verifyAttrValueTypeXSBinary() {
        val builder = new NonInflatingSaml20ObjectBuilder(openSamlConfigBean);
        assertNotNull(builder.newAttribute("email", "mail",
            List.of(EncodingUtils.encodeBase64("values")),
            Map.of("mail", "basic"), "basic",
            Map.of("mail", XSBase64Binary.class.getSimpleName())));
    }

    @Test
    public void verifyAttrValueTypeXSObject() {
        val builder = new NonInflatingSaml20ObjectBuilder(openSamlConfigBean);
        assertNotNull(builder.newAttribute("email", "mail",
            List.of(new TicketGrantingTicketExpirationPolicy(100, 100)),
            Map.of("mail", "basic"), "basic",
            Map.of("mail", XSObject.class.getSimpleName())));
    }

    @Test
    public void verifyAttrValueTypeNone() {
        val builder = new NonInflatingSaml20ObjectBuilder(openSamlConfigBean);
        assertNotNull(builder.newAttribute("email", "mail",
            List.of(),
            Map.of("mail", "basic"), "basic",
            Map.of("mail", XSObject.class.getSimpleName())));
    }

    @Test
    public void verifyAttributes() {
        val builder = new NonInflatingSaml20ObjectBuilder(openSamlConfigBean);
        val formats = Map.of("mail", "basic", "name", "unspecified", "cn", StringUtils.EMPTY);
        assertNotNull(builder.newAttribute("email", "mail",
            List.of("cas@example.org"),
            formats, "basic", Map.of()));
        assertNotNull(builder.newAttribute("common-name", "name",
            List.of("casuser"),
            formats, "basic", Map.of()));
        val attr3 = builder.newAttribute("cn-name", "cn",
            List.of("casuser"),
            formats, "basic", Map.of());
        assertNotNull(attr3);
        assertNull(attr3.getNameFormat());
    }

    @Test
    public void verifySubject() {
        val builder = new NonInflatingSaml20ObjectBuilder(openSamlConfigBean);
        val id = builder.getNameID(NameID.UNSPECIFIED, "casuser");
        val subjectId = builder.getNameID(NameID.UNSPECIFIED, "casuser");
        val sub = builder.newSubject(id, subjectId, "cas", ZonedDateTime.now(ZoneOffset.UTC),
            "https://github.com", ZonedDateTime.now(ZoneOffset.UTC));
        assertNotNull(sub);
    }

    @Test
    public void verifyQName() {
        val builder = new NonInflatingSaml20ObjectBuilder(openSamlConfigBean);
        assertThrows(IllegalStateException.class,
            () -> builder.getSamlObjectQName(Object.class));
    }

    @Test
    public void failSign() {
        val builder = new NonInflatingSaml20ObjectBuilder(openSamlConfigBean);
        assertThrows(IllegalArgumentException.class,
            () -> builder.signSamlResponse("bad-response",
                mock(PrivateKey.class), mock(PublicKey.class)));
        val response = builder.newResponse(UUID.randomUUID().toString(), ZonedDateTime.now(ZoneOffset.UTC), "cas",
            CoreAuthenticationTestUtils.getWebApplicationService());
        val result = SamlUtils.transformSamlObject(openSamlConfigBean, response, true).toString();
        assertThrows(IllegalArgumentException.class,
            () -> builder.signSamlResponse(result,
                mock(PrivateKey.class), mock(PublicKey.class)));
        assertThrows(IllegalArgumentException.class,
            () -> {
                val pubKey = mock(PublicKey.class);
                when(pubKey.getAlgorithm()).thenReturn("RSA");
                builder.signSamlResponse(result,
                    mock(PrivateKey.class), pubKey);
            });
    }
}
