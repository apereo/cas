package org.apereo.cas.authentication.support;

import org.apereo.cas.CasViewConstants;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasPersonDirectoryAttributeRepositoryConfiguration;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.TestUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

import static org.junit.Assert.*;

/**
 * This is test cases for {@link DefaultCasAttributeEncoder}.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {CasCoreServicesConfiguration.class,
        CasPersonDirectoryAttributeRepositoryConfiguration.class,
        CasCoreAuthenticationConfiguration.class, CasCoreUtilConfiguration.class})
@ContextConfiguration(locations= {"/services-context.xml"})
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
        final Service service = TestUtils.getService("testDefault");
        final CasAttributeEncoder encoder = new DefaultCasAttributeEncoder(this.servicesManager);
        final Map<String, Object> encoded = encoder.encodeAttributes(this.attributes, service);
        assertEquals(encoded.size(), this.attributes.size() - 2);
    }

    @Test
    public void checkAttributesEncodedCorrectly() {
        final Service service = TestUtils.getService("testencryption");
        final CasAttributeEncoder encoder = new DefaultCasAttributeEncoder(this.servicesManager);
        final Map<String, Object> encoded = encoder.encodeAttributes(this.attributes, service);
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
