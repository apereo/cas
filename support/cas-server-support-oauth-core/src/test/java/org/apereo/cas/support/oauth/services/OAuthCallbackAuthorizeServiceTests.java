package org.apereo.cas.support.oauth.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.support.oauth.OAuthConstants;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

/**
 * @author Misagh Moayyed
 * @since 4.1
 */
public class OAuthCallbackAuthorizeServiceTests {

    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "oAuthCallbackAuthorizeService.json");
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    public void verifySerializeAnOAuthCallbackAuthorizeServiceToJson() throws IOException {
        final OAuthCallbackAuthorizeService serviceWritten = new OAuthCallbackAuthorizeService();
        serviceWritten.setServiceId("id" + OAuthConstants.CALLBACK_AUTHORIZE_URL_DEFINITION);

        MAPPER.writeValue(JSON_FILE, serviceWritten);

        final RegisteredService serviceRead = MAPPER.readValue(JSON_FILE, OAuthCallbackAuthorizeService.class);

        assertEquals(serviceWritten, serviceRead);
    }
}
