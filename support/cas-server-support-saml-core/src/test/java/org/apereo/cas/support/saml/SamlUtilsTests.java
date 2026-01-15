package org.apereo.cas.support.saml;

import module java.base;
import org.apereo.cas.config.BaseSamlConfigurationTests;
import org.apereo.cas.support.saml.util.NonInflatingSaml20ObjectBuilder;
import org.apereo.cas.test.CasTestExtension;
import lombok.val;
import net.shibboleth.shared.xml.ParserPool;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.velocity.app.VelocityEngine;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.XMLObjectBuilderFactory;
import org.opensaml.core.xml.io.MarshallerFactory;
import org.opensaml.core.xml.io.UnmarshallerFactory;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.NameIDType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
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
@ExtendWith(CasTestExtension.class)
@SpringBootTest(classes = BaseSamlConfigurationTests.SharedTestConfiguration.class)
class SamlUtilsTests {
    @Autowired
    @Qualifier("shibboleth.VelocityEngine")
    protected VelocityEngine velocityEngineFactoryBean;

    @Autowired
    @Qualifier(OpenSamlConfigBean.DEFAULT_BEAN_NAME)
    protected OpenSamlConfigBean openSamlConfigBean;

    @Autowired
    @Qualifier("shibboleth.ParserPool")
    protected ParserPool parserPool;

    @Autowired
    @Qualifier("shibboleth.BuilderFactory")
    protected XMLObjectBuilderFactory builderFactory;

    @Autowired
    @Qualifier("shibboleth.MarshallerFactory")
    protected MarshallerFactory marshallerFactory;

    @Autowired
    @Qualifier("shibboleth.UnmarshallerFactory")
    protected UnmarshallerFactory unmarshallerFactory;

    @Test
    void verify() {
        assertNotNull(velocityEngineFactoryBean);
        assertNotNull(openSamlConfigBean);
        assertNotNull(parserPool);
        assertNotNull(builderFactory);
        assertNotNull(marshallerFactory);
        assertNotNull(unmarshallerFactory);
    }
    
    @Test
    void verifyCert() {
        val x509 = SamlUtils.readCertificate(new ClassPathResource("idp-signing.crt"));
        assertNotNull(x509);
    }

    @Test
    void verifyCertFails() {
        assertThrows(IllegalArgumentException.class, () -> SamlUtils.readCertificate(mock(Resource.class)));
    }

    @Test
    void verifyTransformFails() {
        val builder = new NonInflatingSaml20ObjectBuilder(openSamlConfigBean);
        val attr = builder.newNameID(NameIDType.TRANSIENT, "value");
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
