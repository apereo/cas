package org.apereo.cas.adaptors.swivel.web.flow.rest;

import org.apereo.cas.adaptors.swivel.BaseSwivelAuthenticationTests;
import org.apereo.cas.util.MockWebServer;

import lombok.val;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.HttpStatus.OK;

/**
 * This is {@link SwivelTuringImageGeneratorControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = BaseSwivelAuthenticationTests.SharedTestConfiguration.class,
    properties = {
        "cas.authn.mfa.swivel.swivelUrl=http://localhost:9991",
        "cas.authn.mfa.swivel.swivelTuringImageUrl=http://localhost:9991",
        "cas.authn.mfa.swivel.sharedSecret=$ecret",
        "cas.authn.mfa.swivel.ignoreSslErrors=true"
    })
@Tag("MFA")
public class SwivelTuringImageGeneratorControllerTests {
    @Autowired
    @Qualifier("swivelTuringImageGeneratorController")
    private SwivelTuringImageGeneratorController controller;

    @Test
    public void verifyOperation() throws Exception {
        val bytes = IOUtils.toByteArray(new ClassPathResource("logo.png").getInputStream());
        try (val webServer = new MockWebServer(9991,
            new ByteArrayResource(bytes, "Output"), OK)) {
            webServer.start();

            val request = new MockHttpServletRequest();
            request.addParameter("principal", "casuser");
            val response = new MockHttpServletResponse();

            assertDoesNotThrow(new Executable() {
                @Override
                public void execute() throws Throwable {
                    controller.generate(response, request);
                }
            });
        }
    }
}
