package org.apereo.cas.support.saml;

import org.apereo.cas.config.CoreSamlConfigurationTests;
import org.apereo.cas.support.saml.util.NonInflatingSaml20ObjectBuilder;

import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.NameID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link SamlUtilsTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAML")
@SpringBootTest(classes = CoreSamlConfigurationTests.SharedTestConfiguration.class)
public class SamlUtilsTests {
    @Autowired
    @Qualifier(OpenSamlConfigBean.DEFAULT_BEAN_NAME)
    private OpenSamlConfigBean openSamlConfigBean;

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
        val builder = new NonInflatingSaml20ObjectBuilder(openSamlConfigBean);
        val attr = builder.getNameID(NameID.TRANSIENT, "value");
        val xml = SamlUtils.transformSamlObject(openSamlConfigBean, attr).toString();
        assertThrows(SamlException.class, () -> SamlUtils.transformSamlObject(openSamlConfigBean,
            xml.getBytes(StandardCharsets.UTF_8), AuthnRequest.class));

        assertNull(SamlUtils.transformSamlObject(mock(OpenSamlConfigBean.class),
            ArrayUtils.EMPTY_BYTE_ARRAY, XMLObject.class));

        assertThrows(SamlException.class, () -> SamlUtils.transformSamlObject(mock(OpenSamlConfigBean.class),
            "whatever".getBytes(StandardCharsets.UTF_8), XMLObject.class));
        assertThrows(SamlException.class, () -> SamlUtils.transformSamlObject(mock(OpenSamlConfigBean.class), mock(XMLObject.class)));
        assertThrows(SamlException.class, () -> SamlUtils.logSamlObject(mock(OpenSamlConfigBean.class), mock(XMLObject.class)));
    }
}
