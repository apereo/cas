package org.apereo.cas.support.saml.util;

import org.apereo.cas.config.CoreSamlConfigurationTests;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.opensaml.saml.saml2.core.NameID;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link NonInflatingSaml20ObjectBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAML")
@SpringBootTest(classes = CoreSamlConfigurationTests.SharedTestConfiguration.class)
public class NonInflatingSaml20ObjectBuilderTests extends CoreSamlConfigurationTests {

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
        assertNull(attr3);
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
}
