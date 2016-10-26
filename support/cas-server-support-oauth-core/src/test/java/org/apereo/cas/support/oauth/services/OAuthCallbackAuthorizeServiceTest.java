package org.apereo.cas.support.oauth.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.support.oauth.OAuthConstants;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class OAuthCallbackAuthorizeServiceTest {

    private static final File JSON_FILE = new File("oAuthCallbackAuthorizeService.json");
    private static final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void verifySerializeAnOAuthCallbackAuthorizeServiceToJson() throws IOException {
        final OAuthCallbackAuthorizeService serviceWritten = new OAuthCallbackAuthorizeService();
        serviceWritten.setServiceId("id" + OAuthConstants.CALLBACK_AUTHORIZE_URL_DEFINITION);

        mapper.writeValue(JSON_FILE, serviceWritten);

        final RegisteredService serviceRead = mapper.readValue(JSON_FILE, OAuthCallbackAuthorizeService.class);

        assertEquals(serviceWritten, serviceRead);
    }
}
