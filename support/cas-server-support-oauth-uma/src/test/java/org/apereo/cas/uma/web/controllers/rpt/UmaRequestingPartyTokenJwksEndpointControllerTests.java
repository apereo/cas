package org.apereo.cas.uma.web.controllers.rpt;

import module java.base;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.uma.web.controllers.BaseUmaEndpointControllerTests;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link UmaRequestingPartyTokenJwksEndpointControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("UMA")
@Execution(ExecutionMode.SAME_THREAD)
class UmaRequestingPartyTokenJwksEndpointControllerTests extends BaseUmaEndpointControllerTests {
    @Test
    void verifyUnknownFile() throws Throwable {
        casProperties.getAuthn().getOauth().getUma().getRequestingPartyToken()
            .getJwksFile().setLocation(new FileSystemResource(new File("/tmp/uma-unknown.jkws")));
        val result = performUmaRequest(HttpMethod.GET, OAuth20Constants.UMA_JWKS_URL);
        assertEquals(HttpStatus.NOT_IMPLEMENTED.value(), result.getResponse().getStatus());
    }

    @Test
    void verifyBadFile() throws Throwable {
        val file = Files.createTempFile("uma", ".jwks").toFile();
        FileUtils.write(file, "@@", StandardCharsets.UTF_8);
        casProperties.getAuthn().getOauth().getUma().getRequestingPartyToken()
            .getJwksFile().setLocation(new FileSystemResource(file));
        val result = performUmaRequest(HttpMethod.GET, OAuth20Constants.UMA_JWKS_URL);
        assertEquals(HttpStatus.BAD_REQUEST.value(), result.getResponse().getStatus());
    }

    @Test
    void verifySuccess() throws Throwable {
        casProperties.getAuthn().getOauth().getUma().getRequestingPartyToken()
            .getJwksFile().setLocation(new ClassPathResource("uma-keystore.jwks"));
        val result = performUmaRequest(HttpMethod.GET, OAuth20Constants.UMA_JWKS_URL);
        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
    }
}
