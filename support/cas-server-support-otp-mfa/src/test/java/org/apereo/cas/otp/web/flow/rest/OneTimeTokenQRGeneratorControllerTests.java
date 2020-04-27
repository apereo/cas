package org.apereo.cas.otp.web.flow.rest;

import org.apereo.cas.otp.repository.token.BaseOneTimeTokenRepositoryTests;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OneTimeTokenQRGeneratorControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = BaseOneTimeTokenRepositoryTests.SharedTestConfiguration.class)
public class OneTimeTokenQRGeneratorControllerTests {

    @Autowired
    @Qualifier("oneTimeTokenQRGeneratorController")
    private OneTimeTokenQRGeneratorController oneTimeTokenQRGeneratorController;

    @Test
    public void verifyOperation() {
        val request = new MockHttpServletRequest();
        request.addParameter("key", "example-key");
        val response = new MockHttpServletResponse();
        assertDoesNotThrow(new Executable() {
            @Override
            public void execute() throws Throwable {
                oneTimeTokenQRGeneratorController.generate(response, request);
            }
        });
    }
}
