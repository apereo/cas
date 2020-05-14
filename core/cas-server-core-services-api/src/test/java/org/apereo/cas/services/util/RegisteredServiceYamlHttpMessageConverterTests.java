package org.apereo.cas.services.util;

import org.apereo.cas.services.AbstractRegisteredService;
import org.apereo.cas.services.RegexRegisteredService;
import org.apereo.cas.services.RegisteredService;

import lombok.val;
import org.apache.commons.lang3.NotImplementedException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.MediaType;
import org.springframework.mock.http.MockHttpOutputMessage;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link RegisteredServiceYamlHttpMessageConverterTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Simple")
public class RegisteredServiceYamlHttpMessageConverterTests {
    private static AbstractRegisteredService getService() {
        val svc = new RegexRegisteredService();
        svc.setServiceId("Testing");
        svc.setName("Test");
        return svc;
    }

    @Test
    public void verifyOperation() throws Exception {
        val input = new RegisteredServiceYamlHttpMessageConverter<>();
        assertTrue(input.supports(Collection.class));
        assertTrue(input.supports(RegisteredService.class));
        assertThrows(NotImplementedException.class,
            () -> input.readInternal(RegisteredService.class, mock(HttpInputMessage.class)));

        val outputMessage = new MockHttpOutputMessage();
        input.write(getService(), MediaType.APPLICATION_JSON, outputMessage);
        assertNotNull(outputMessage.getBodyAsString());

        input.write(List.of(getService()), MediaType.APPLICATION_JSON, outputMessage);
        assertNotNull(outputMessage.getBodyAsString());
    }
}
