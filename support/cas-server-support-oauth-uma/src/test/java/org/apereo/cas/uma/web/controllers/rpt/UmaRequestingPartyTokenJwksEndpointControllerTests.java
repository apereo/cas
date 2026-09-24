package org.apereo.cas.uma.web.controllers.rpt;

import module java.base;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.uma.web.controllers.BaseUmaEndpointControllerTests;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link UmaRequestingPartyTokenJwksEndpointControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("UMA")
@TestPropertySource(properties = "cas.authn.oauth.uma.requesting-party-token.jwks-file.location=classpath:uma-keystore.jwks")
class UmaRequestingPartyTokenJwksEndpointControllerTests extends BaseUmaEndpointControllerTests {
    @Test
    void verifyOperation() throws Throwable {
        val jwksFile = casProperties.getAuthn().getOauth().getUma().getRequestingPartyToken().getJwksFile();

        val missing = Files.createTempDirectory("uma").resolve("missing.jwks").toFile();
        jwksFile.setLocation(new FileSystemResource(missing));
        assertEquals(HttpStatus.NOT_IMPLEMENTED.value(),
            performUmaRequest(HttpMethod.GET, OAuth20Constants.UMA_JWKS_URL).getResponse().getStatus());

        val malformed = Files.createTempFile("uma", ".jwks").toFile();
        FileUtils.write(malformed, "@@", StandardCharsets.UTF_8);
        jwksFile.setLocation(new FileSystemResource(malformed));
        assertEquals(HttpStatus.BAD_REQUEST.value(),
            performUmaRequest(HttpMethod.GET, OAuth20Constants.UMA_JWKS_URL).getResponse().getStatus());

        jwksFile.setLocation(new ClassPathResource("uma-keystore.jwks"));
        assertEquals(HttpStatus.OK.value(),
            performUmaRequest(HttpMethod.GET, OAuth20Constants.UMA_JWKS_URL).getResponse().getStatus());
    }
}
