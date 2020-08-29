package org.apereo.cas.support.saml;

import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.opensaml.core.xml.XMLObject;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link SamlUtilsTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAML")
public class SamlUtilsTests {

    @Test
    public void verifyCert() {
        val x509 = SamlUtils.readCertificate(new ClassPathResource("idp-signing.crt"));
        assertNotNull(x509);
    }

    @Test
    public void verifyCertFails() {
        assertThrows(IllegalArgumentException.class, () -> SamlUtils.readCertificate(mock(Resource.class)));
    }

    @Test
    public void verifyTransformFails() {
        assertThrows(SamlException.class, () -> SamlUtils.transformSamlObject(mock(OpenSamlConfigBean.class),
            ArrayUtils.EMPTY_BYTE_ARRAY, XMLObject.class));
        assertThrows(SamlException.class, () -> SamlUtils.transformSamlObject(mock(OpenSamlConfigBean.class), mock(XMLObject.class)));
        assertThrows(SamlException.class, () -> SamlUtils.logSamlObject(mock(OpenSamlConfigBean.class), mock(XMLObject.class)));
    }
}
