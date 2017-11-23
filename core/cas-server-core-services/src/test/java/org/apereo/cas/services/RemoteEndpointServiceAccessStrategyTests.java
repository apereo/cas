package org.apereo.cas.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

/**
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class RemoteEndpointServiceAccessStrategyTests {

    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "remoteEndpointServiceAccessStrategy.json");
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    public void verifySerializeAX509CertificateCredentialToJson() throws IOException {
        final RemoteEndpointServiceAccessStrategy strategyWritten = new RemoteEndpointServiceAccessStrategy();

        MAPPER.writeValue(JSON_FILE, strategyWritten);

        final RegisteredServiceAccessStrategy credentialRead = MAPPER.readValue(JSON_FILE, RemoteEndpointServiceAccessStrategy.class);

        assertEquals(strategyWritten, credentialRead);
    }

}
