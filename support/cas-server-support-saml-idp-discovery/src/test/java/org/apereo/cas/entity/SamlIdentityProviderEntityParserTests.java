package org.apereo.cas.entity;

import org.apereo.cas.authentication.principal.ClientCustomPropertyConstants;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.saml2.Saml2TestClientsBuilder;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.saml.client.SAML2Client;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SamlIdentityProviderEntityParserTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("SAML2")
class SamlIdentityProviderEntityParserTests {
    @Test
    void verifyResource() throws Throwable {
        val context = MockRequestContext.create();
        val parser = new SamlIdentityProviderEntityParser(new ClassPathResource("disco-feed.json"));
        assertFalse(parser.resolveEntities(context.getHttpServletRequest(), context.getHttpServletResponse()).isEmpty());
    }

    @Test
    void verifyFile() throws Throwable {
        val context = MockRequestContext.create();
        val file = Files.createTempFile("feed", ".json").toFile();
        val content = IOUtils.toString(new ClassPathResource("disco-feed.json").getInputStream(), StandardCharsets.UTF_8);
        FileUtils.writeStringToFile(file, content, StandardCharsets.UTF_8);
        val parser = new SamlIdentityProviderEntityParser(new FileSystemResource(file));
        assertFalse(parser.resolveEntities(context.getHttpServletRequest(), context.getHttpServletResponse()).isEmpty());
        Files.setLastModifiedTime(file.toPath(), FileTime.from(Instant.now()));
        Thread.sleep(8_000);
        parser.destroy();
    }

    @Test
    void verifyEntity() throws Exception {
        val context = MockRequestContext.create();
        val entity = new SamlIdentityProviderEntity();
        entity.setEntityID("org.example");
        val parser = new SamlIdentityProviderEntityParser(entity);
        assertEquals(1, parser.resolveEntities(context.getHttpServletRequest(), context.getHttpServletResponse()).size());
    }

    @Test
    void verifySaml2IdentityProvider() throws Exception {
        val context = MockRequestContext.create();
        val saml2Config = Saml2TestClientsBuilder.newSAML2Configuration(Saml2TestClientsBuilder.IDP_METADATA_PATH);
        val saml2Client = new SAML2Client(saml2Config);
        saml2Client.getCustomProperties().put(ClientCustomPropertyConstants.CLIENT_CUSTOM_PROPERTY_DISPLAY_NAME, "SAML2ClientDiscovery");
        saml2Client.setCallbackUrl("https://callback.example.org");
        saml2Client.setName("SAML2ClientDiscovery");
        val parser = SamlIdentityProviderEntityParser.fromClient(saml2Client);
        assertEquals(1, parser.resolveEntities(context.getHttpServletRequest(), context.getHttpServletResponse()).size());
    }

    @Test
    void verifySaml2IdentityProvidersViaAggregate() throws Exception {
        val context = MockRequestContext.create();
        val saml2Config = Saml2TestClientsBuilder.newSAML2Configuration("src/test/resources/idp-metadata-aggregate.xml");
        val saml2Client = new SAML2Client(saml2Config);
        saml2Client.setCallbackUrl("https://callback.example.org");
        saml2Client.setName("SAML2ClientDiscoveryAggregate");
        val parser = SamlIdentityProviderEntityParser.fromClient(saml2Client);
        assertEquals(2, parser.resolveEntities(context.getHttpServletRequest(), context.getHttpServletResponse()).size());
    }
}
