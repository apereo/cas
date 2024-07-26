package org.apereo.cas.services;

import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.web.CasYamlHttpMessageConverter;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.MediaType;
import org.springframework.mock.http.MockHttpOutputMessage;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasYamlHttpMessageConverterTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("RegisteredService")
@ExtendWith(CasTestExtension.class)
class CasYamlHttpMessageConverterTests {
    private static BaseRegisteredService getService() {
        val svc = new CasRegisteredService();
        svc.setServiceId("Testing");
        svc.setName("Test");
        return svc;
    }

    @Test
    void verifyOperation() throws Throwable {
        val converter = new CasYamlHttpMessageConverter();
        val outputMessage = new MockHttpOutputMessage();
        converter.write(getService(), MediaType.APPLICATION_JSON, outputMessage);
        assertNotNull(outputMessage.getBodyAsString());
        converter.write(List.of(getService()), MediaType.APPLICATION_JSON, outputMessage);
        assertNotNull(outputMessage.getBodyAsString());
        assertFalse(converter.canWrite(String.class, null));
        assertFalse(converter.canWrite(String.class, MediaType.APPLICATION_JSON));
        assertTrue(converter.canWrite(String.class, CasYamlHttpMessageConverter.MEDIA_TYPE_YAML));
    }
}
