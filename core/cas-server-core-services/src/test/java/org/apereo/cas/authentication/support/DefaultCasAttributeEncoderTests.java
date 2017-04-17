package org.apereo.cas.authentication.support;

import org.apereo.cas.CasViewConstants;
import org.apereo.cas.authentication.ProtocolAttributeEncoder;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationHandlersConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationMetadataConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPolicyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationSupportConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasPersonDirectoryConfiguration;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.cipher.NoOpCipherExecutor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

import static org.junit.Assert.*;

/**
 * This is test cases for {@link DefaultCasProtocolAttributeEncoder}.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {CasCoreServicesConfiguration.class,
        CasPersonDirectoryConfiguration.class,
        CasCoreAuthenticationConfiguration.class,
        CasCoreAuthenticationPrincipalConfiguration.class,
        CasCoreAuthenticationPolicyConfiguration.class,
        CasCoreAuthenticationMetadataConfiguration.class,
        CasCoreAuthenticationSupportConfiguration.class,
        CasCoreAuthenticationHandlersConfiguration.class,
        RefreshAutoConfiguration.class,
        CasCoreHttpConfiguration.class,
        CasCoreUtilConfiguration.class})
@ContextConfiguration(locations= {"/services-context.xml"})
@EnableScheduling
public class DefaultCasAttributeEncoderTests {

    private Map<String, Object> attributes;

    @Autowired
    private ServicesManager servicesManager;

    @Before
    public void before() {
        this.attributes = new HashMap<>();
        IntStream.range(0, 3).forEach(i -> this.attributes.put("attr" + i, newSingleAttribute("value" + i)));
        this.attributes.put(CasViewConstants.MODEL_ATTRIBUTE_NAME_PROXY_GRANTING_TICKET, newSingleAttribute("PGT-1234567"));
        this.attributes.put(CasViewConstants.MODEL_ATTRIBUTE_NAME_PRINCIPAL_CREDENTIAL, newSingleAttribute("PrincipalPassword"));
    }

    private static Collection<String> newSingleAttribute(final String attr) {
        return Collections.singleton(attr);
    }

    @Test
    public void checkNoPublicKeyDefined() {
        final Service service = RegisteredServiceTestUtils.getService("testDefault");
        final ProtocolAttributeEncoder encoder = new DefaultCasProtocolAttributeEncoder(this.servicesManager, NoOpCipherExecutor.getInstance());
        final Map<String, Object> encoded = encoder.encodeAttributes(this.attributes, this.servicesManager.findServiceBy(service));
        assertEquals(encoded.size(), this.attributes.size() - 2);
    }

    @Test
    public void checkAttributesEncodedCorrectly() {
        final Service service = RegisteredServiceTestUtils.getService("testencryption");
        final ProtocolAttributeEncoder encoder = new DefaultCasProtocolAttributeEncoder(this.servicesManager, NoOpCipherExecutor.getInstance());
        final Map<String, Object> encoded = encoder.encodeAttributes(this.attributes, this.servicesManager.findServiceBy(service));
        assertEquals(encoded.size(), this.attributes.size());
        checkEncryptedValues(CasViewConstants.MODEL_ATTRIBUTE_NAME_PRINCIPAL_CREDENTIAL, encoded);
        checkEncryptedValues(CasViewConstants.MODEL_ATTRIBUTE_NAME_PROXY_GRANTING_TICKET, encoded);
    }

    private void checkEncryptedValues(final String name, final Map<String, Object> encoded) {
        final String v1 = ((Collection<?>) this.attributes.get(
                name)).iterator().next().toString();
        final String v2 = (String) encoded.get(name);
        assertNotEquals(v1, v2);
    }
}
