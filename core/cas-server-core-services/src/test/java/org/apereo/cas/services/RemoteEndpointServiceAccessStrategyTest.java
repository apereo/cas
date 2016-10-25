package org.apereo.cas.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class RemoteEndpointServiceAccessStrategyTest {

    private static final File JSON_FILE = new File("remoteEndpointServiceAccessStrategy.json");
    private static final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void verifySerializeAX509CertificateCredentialToJson() throws IOException {
        RemoteEndpointServiceAccessStrategy strategyWritten = new RemoteEndpointServiceAccessStrategy();

        mapper.writeValue(JSON_FILE, strategyWritten);

        final RegisteredServiceAccessStrategy credentialRead = mapper.readValue(JSON_FILE, RemoteEndpointServiceAccessStrategy.class);

        assertEquals(strategyWritten, credentialRead);
    }

}