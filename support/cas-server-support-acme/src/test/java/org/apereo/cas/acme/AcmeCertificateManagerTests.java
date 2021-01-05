package org.apereo.cas.acme;

import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.shredzone.acme4j.Certificate;
import org.shredzone.acme4j.Login;
import org.shredzone.acme4j.Order;
import org.shredzone.acme4j.Status;
import org.shredzone.acme4j.challenge.Http01Challenge;
import org.shredzone.acme4j.toolbox.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link AcmeCertificateManagerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("Web")
@Import(AcmeCertificateManagerTests.AcmeTestConfiguration.class)
@TestPropertySource(properties = {
    "cas.acme.retry-internal=PT1S",
    "cas.acme.terms-of-use-accepted=true"
})
public class AcmeCertificateManagerTests extends BaseAcmeTests {

    @Autowired
    @Qualifier("acmeCertificateManager")
    private AcmeCertificateManager acmeCertificateManager;

    @BeforeEach
    @AfterEach
    public void beforeEach() throws Exception {
        FileUtils.deleteQuietly(casProperties.getAcme().getDomainChain().getLocation().getFile());
        FileUtils.deleteQuietly(casProperties.getAcme().getDomainCsr().getLocation().getFile());
        FileUtils.deleteQuietly(casProperties.getAcme().getDomainKey().getLocation().getFile());
        FileUtils.deleteQuietly(casProperties.getAcme().getUserKey().getLocation().getFile());
    }
    
    @Test
    public void verifyOperation() throws Exception {
        assertNotNull(acmeCertificateManager);
        acmeCertificateManager.fetchCertificate(casProperties.getAcme().getDomains());
    }

    @TestConfiguration("AcmeTestConfiguration")
    public static class AcmeTestConfiguration {
        @Bean
        public AcmeAuthorizationExecutor acmeAuthorizationExecutor() throws Exception {
            val locator = mock(AcmeAuthorizationExecutor.class);
            val challenge = new MockHttp01Challenge();
            when(locator.find(any())).thenReturn(challenge);

            val order = mock(Order.class);
            val certificate = mock(Certificate.class);
            when(order.getCertificate()).thenReturn(certificate);
            when(locator.execute(any(), any())).thenReturn(order);
            return locator;
        }
    }

    private static class MockHttp01Challenge extends Http01Challenge {
        private static final long serialVersionUID = -5555468598931902011L;

        private Status status = Status.INVALID;

        MockHttp01Challenge() {
            super(mock(Login.class), JSON.parse("{\"url\":\"https://url\"}"));
        }

        @Override
        public Status getStatus() {
            return status;
        }

        @Override
        protected void setJSON(final JSON json) {
        }

        @Override
        public void update() {
            status = Status.VALID;
        }

        @Override
        public String getToken() {
            return "token";
        }

        @Override
        public String getAuthorization() {
            return "authz";
        }

        @Override
        public void trigger() {
            status = Status.PROCESSING;
        }
    }
}
