package org.apereo.cas.support.saml.web.idp.metadata;

import module java.base;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletResponse;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SamlIdPMetadataControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAMLMetadata")
class SamlIdPMetadataControllerTests extends BaseSamlIdPConfigurationTests {

    @Autowired
    @Qualifier("samlIdPMetadataController")
    private SamlIdPMetadataController samlIdPMetadataController;

    @Test
    void verifyOperationByServiceId() {
        val response = new MockHttpServletResponse();
        val service = RegisteredServiceTestUtils.getService().getId();
        assertDoesNotThrow(() -> samlIdPMetadataController.generateMetadataForIdp(service, response));
        assertDoesNotThrow(() -> samlIdPMetadataController.idpSigningCertificate(service));
        assertDoesNotThrow(() -> samlIdPMetadataController.idpEncryptionCertificate(service));
    }

    @Test
    void verifyOperation() {
        val response = new MockHttpServletResponse();
        assertDoesNotThrow(() -> samlIdPMetadataController.generateMetadataForIdp("1000", response));
    }

    @Test
    void verifyNoServiceOperation() {
        val response = new MockHttpServletResponse();
        assertDoesNotThrow(() -> samlIdPMetadataController.generateMetadataForIdp(null, response));
    }
}
