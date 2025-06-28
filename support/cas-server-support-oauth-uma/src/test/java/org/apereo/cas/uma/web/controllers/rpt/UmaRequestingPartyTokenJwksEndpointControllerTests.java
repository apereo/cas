package org.apereo.cas.uma.web.controllers.rpt;

import java.nio.file.Files;
import org.apereo.cas.uma.web.controllers.BaseUmaEndpointControllerTests;

import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.File;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link UmaRequestingPartyTokenJwksEndpointControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("UMA")
class UmaRequestingPartyTokenJwksEndpointControllerTests extends BaseUmaEndpointControllerTests {
    @Test
    void verifyUnknownFile() {
        casProperties.getAuthn().getOauth().getUma().getRequestingPartyToken()
            .getJwksFile().setLocation(new FileSystemResource(new File("/tmp/uma-unknown.jkws")));
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        val responseEntity = umaRequestingPartyTokenJwksEndpointController.getKeys(request, response);
        assertEquals(HttpStatus.NOT_IMPLEMENTED, responseEntity.getStatusCode());
    }

    @Test
    void verifyBadFile() throws Throwable {
        val file = Files.createTempFile("uma", ".jwks").toFile();
        FileUtils.write(file, "@@", StandardCharsets.UTF_8);
        casProperties.getAuthn().getOauth().getUma().getRequestingPartyToken()
            .getJwksFile().setLocation(new FileSystemResource(file));
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        val responseEntity = umaRequestingPartyTokenJwksEndpointController.getKeys(request, response);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }

    @Test
    void verifySuccess() {
        casProperties.getAuthn().getOauth().getUma().getRequestingPartyToken()
            .getJwksFile().setLocation(new ClassPathResource("uma-keystore.jwks"));
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        val responseEntity = umaRequestingPartyTokenJwksEndpointController.getKeys(request, response);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }
}
