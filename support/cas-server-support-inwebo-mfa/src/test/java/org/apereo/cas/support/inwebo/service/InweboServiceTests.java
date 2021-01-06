package org.apereo.cas.support.inwebo.service;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.SpringResourceProperties;
import org.apereo.cas.configuration.model.core.util.ClientCertificateProperties;
import org.apereo.cas.support.inwebo.service.response.InweboDeviceNameResponse;
import org.apereo.cas.support.inwebo.service.response.InweboResult;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

/**
 * Tests {@link InweboService}.
 *
 * @author Jerome LELEU
 * @since 6.4.0
 */
@Tag("MFA")
public class InweboServiceTests {

    private static final String OPERATION = "operation";

    private InweboService service;

    @BeforeEach
    public void setUp() {
        val casProperties = new CasConfigurationProperties();
        val certificate = new SpringResourceProperties();
        certificate.setLocation(new ClassPathResource("clientcert.p12"));
        val clientCertificate = new ClientCertificateProperties();
        clientCertificate.setCertificate(certificate);
        clientCertificate.setPassphrase("password");
        casProperties.getAuthn().getMfa().getInwebo().setClientCertificate(clientCertificate);
        service = new InweboService(casProperties, mock(InweboConsoleAdmin.class));
    }

    @Test
    public void verifyErrOk() {
        val response = service.buildResponse(new InweboDeviceNameResponse(), OPERATION, "OK");
        assertEquals(InweboResult.OK, response.getResult());
    }

    @Test
    public void verifyErrNopush() {
        val response = service.buildResponse(new InweboDeviceNameResponse(), OPERATION, "NOK:NOPUSH");
        assertEquals(InweboResult.NOPUSH, response.getResult());
    }

    @Test
    public void verifyErrNoma() {
        val response = service.buildResponse(new InweboDeviceNameResponse(), OPERATION, "NOK:NOMA");
        assertEquals(InweboResult.NOMA, response.getResult());
    }

    @Test
    public void verifyErrNologin() {
        val response = service.buildResponse(new InweboDeviceNameResponse(), OPERATION, "NOK:NOLOGIN");
        assertEquals(InweboResult.NOLOGIN, response.getResult());
    }

    @Test
    public void verifyErrSn() {
        val response = service.buildResponse(new InweboDeviceNameResponse(), OPERATION, "NOK:SN");
        assertEquals(InweboResult.SN, response.getResult());
    }

    @Test
    public void verifyErrSrv() {
        val response = service.buildResponse(new InweboDeviceNameResponse(), OPERATION, "NOK:srv unknown");
        assertEquals(InweboResult.UNKNOWN_SERVICE, response.getResult());
    }

    @Test
    public void verifyErrWaiting() {
        val response = service.buildResponse(new InweboDeviceNameResponse(), OPERATION, "NOK:WAITING");
        assertEquals(InweboResult.WAITING, response.getResult());
    }

    @Test
    public void verifyErrRefused() {
        val response = service.buildResponse(new InweboDeviceNameResponse(), OPERATION, "NOK:REFUSED");
        assertEquals(InweboResult.REFUSED, response.getResult());
    }

    @Test
    public void verifyErrTimeout() {
        val response = service.buildResponse(new InweboDeviceNameResponse(), OPERATION, "NOK:TIMEOUT");
        assertEquals(InweboResult.TIMEOUT, response.getResult());
    }

    @Test
    public void verifyErrOther() {
        val response = service.buildResponse(new InweboDeviceNameResponse(), OPERATION, "NOK:other");
        assertEquals(InweboResult.NOK, response.getResult());
    }
}
