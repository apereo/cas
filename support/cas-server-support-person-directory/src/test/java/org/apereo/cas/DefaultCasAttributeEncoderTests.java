package org.apereo.cas;

import org.apereo.cas.authentication.support.DefaultCasProtocolAttributeEncoder;
import org.apereo.cas.config.CasPersonDirectoryAutoConfiguration;
import org.apereo.cas.services.RegisteredServicePublicKeyCipherExecutor;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.util.crypto.CipherExecutor;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.IntStream;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is test cases for {@link DefaultCasProtocolAttributeEncoder}.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
@Tag("Attributes")
@ImportAutoConfiguration(CasPersonDirectoryAutoConfiguration.class)
class DefaultCasAttributeEncoderTests extends BaseCasCoreTests {
    private Map<String, Object> attributes;

    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    private ServicesManager servicesManager;

    private static Collection<String> newSingleAttribute(final String attr) {
        return Set.of(attr);
    }

    @BeforeEach
    void before() {
        this.attributes = new HashMap<>();
        IntStream.range(0, 3).forEach(i -> this.attributes.put("attr%d".formatted(i), newSingleAttribute("value%d".formatted(i))));
        this.attributes.put(CasViewConstants.MODEL_ATTRIBUTE_NAME_PROXY_GRANTING_TICKET, newSingleAttribute("PGT-1234567"));
        this.attributes.put(CasViewConstants.MODEL_ATTRIBUTE_NAME_PRINCIPAL_CREDENTIAL, newSingleAttribute("PrincipalPassword"));
    }

    @Test
    void checkNoPublicKeyDefined() {
        val service = RegisteredServiceTestUtils.getService("testDefault");
        val encoder = new DefaultCasProtocolAttributeEncoder(this.servicesManager,
            RegisteredServicePublicKeyCipherExecutor.INSTANCE,
            CipherExecutor.noOpOfStringToString());
        val encoded = encoder.encodeAttributes(Map.of(), this.attributes, servicesManager.findServiceBy(service), service);
        assertEquals(this.attributes.size() - 2, encoded.size());
    }

    @Test
    void checkAttributesEncodedCorrectly() {
        val service = RegisteredServiceTestUtils.getService("testencryption");
        val encoder = new DefaultCasProtocolAttributeEncoder(this.servicesManager,
            RegisteredServicePublicKeyCipherExecutor.INSTANCE,
            CipherExecutor.noOpOfStringToString());
        val pgt = mock(Ticket.class);
        val pgtId = UUID.randomUUID().toString();
        when(pgt.getId()).thenReturn(pgtId);

        val encoded = encoder.encodeAttributes(Map.of(CasViewConstants.MODEL_ATTRIBUTE_NAME_PROXY_GRANTING_TICKET, pgt),
            this.attributes, servicesManager.findServiceBy(service), service);
        assertEquals(encoded.size(), this.attributes.size());
        checkEncryptedValues(CasViewConstants.MODEL_ATTRIBUTE_NAME_PRINCIPAL_CREDENTIAL, encoded);
        checkEncryptedValues(CasViewConstants.MODEL_ATTRIBUTE_NAME_PROXY_GRANTING_TICKET, encoded);
    }

    private void checkEncryptedValues(final String name, final Map<String, Object> encoded) {
        val v1 = ((Collection<?>) this.attributes.get(name)).iterator().next().toString();
        val v2 = (String) encoded.get(name);
        assertNotEquals(v1, v2);
    }
}
