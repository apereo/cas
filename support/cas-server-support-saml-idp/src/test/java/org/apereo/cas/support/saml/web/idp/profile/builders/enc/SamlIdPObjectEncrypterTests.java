package org.apereo.cas.support.saml.web.idp.profile.builders.enc;

import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.SamlException;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceMetadataAdaptor;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.crypto.DecryptionException;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.NameIDType;
import org.opensaml.saml.saml2.core.impl.NameIDBuilder;
import org.springframework.test.context.TestPropertySource;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link SamlIdPObjectEncrypterTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("SAML2")
@TestPropertySource(properties = "cas.authn.saml-idp.metadata.file-system.location=classpath:metadata/")
class SamlIdPObjectEncrypterTests extends BaseSamlIdPConfigurationTests {
    @Test
    void verifyEncOptional() {
        val registeredService = getSamlRegisteredServiceForTestShib(true, false, true);
        registeredService.setEncryptionOptional(true);
        registeredService.setEncryptionBlackListedAlgorithms(CollectionUtils.wrapArrayList("excludeAlg1"));
        registeredService.setEncryptionWhiteListedAlgorithms(CollectionUtils.wrapArrayList("includeAlg1"));
        registeredService.setWhiteListBlackListPrecedence("exclude");

        val adaptor = SamlRegisteredServiceMetadataAdaptor
            .get(samlRegisteredServiceCachingMetadataResolver, registeredService,
                registeredService.getServiceId()).get();
        assertNull(samlIdPObjectEncrypter.encode(mock(Assertion.class), registeredService, adaptor));
        assertNull(samlIdPObjectEncrypter.encode(mock(NameID.class), registeredService, adaptor));
    }

    @Test
    void verifyEncBadService() {
        val registeredService = getSamlRegisteredServiceForTestShib(true, false, true);
        registeredService.setServiceId("https://noenc.example.org");
        registeredService.setEncryptionOptional(true);
        registeredService.setEncryptionBlackListedAlgorithms(null);
        registeredService.setEncryptionWhiteListedAlgorithms(null);

        val adaptor = SamlRegisteredServiceMetadataAdaptor
            .get(samlRegisteredServiceCachingMetadataResolver, registeredService,
                registeredService.getServiceId()).get();
        assertNull(samlIdPObjectEncrypter.encode(mock(Assertion.class), registeredService, adaptor));
    }

    @Test
    void verifyEncNotOptional() {
        val registeredService = getSamlRegisteredServiceForTestShib(true, false, true);
        registeredService.setServiceId("https://noenc.example.org");
        registeredService.setEncryptionOptional(false);

        val adaptor = SamlRegisteredServiceMetadataAdaptor
            .get(samlRegisteredServiceCachingMetadataResolver, registeredService,
                registeredService.getServiceId()).get();
        assertThrows(SamlException.class,
            () -> samlIdPObjectEncrypter.encode(mock(Assertion.class), registeredService, adaptor));
    }

    @Test
    void verifyEncNameId() {
        val registeredService = getSamlRegisteredServiceForTestShib(true, false, true);
        registeredService.setEncryptionBlackListedAlgorithms(CollectionUtils.wrapArrayList("excludeAlg1"));
        registeredService.setEncryptionWhiteListedAlgorithms(CollectionUtils.wrapArrayList("includeAlg1"));
        registeredService.setWhiteListBlackListPrecedence("exclude");

        val adaptor = SamlRegisteredServiceMetadataAdaptor
            .get(samlRegisteredServiceCachingMetadataResolver, registeredService,
                registeredService.getServiceId()).get();

        val builder = new NameIDBuilder();
        val nameId = builder.buildObject();
        nameId.setValue(UUID.randomUUID().toString());
        nameId.setFormat(NameIDType.ENCRYPTED);
        val encNameId = samlIdPObjectEncrypter.encode(nameId, registeredService, adaptor);
        assertNotNull(encNameId);

        assertThrows(DecryptionException.class,
            () -> samlIdPObjectEncrypter.decode(encNameId, registeredService, adaptor));
    }

    @Test
    void verifyDecodeEncNameIdFails() {
        val registeredService = getSamlRegisteredServiceForTestShib(true, false, true);
        val adaptor = SamlRegisteredServiceMetadataAdaptor
            .get(samlRegisteredServiceCachingMetadataResolver, registeredService,
                registeredService.getServiceId()).get();

        val builder = new NameIDBuilder();
        val nameId = builder.buildObject();
        nameId.setValue(UUID.randomUUID().toString());
        nameId.setFormat(NameIDType.ENCRYPTED);
        val encNameId = samlIdPObjectEncrypter.encode(nameId, registeredService, adaptor);
        assertNotNull(encNameId);
        assertThrows(DecryptionException.class, () -> samlIdPObjectEncrypter.decode(encNameId, registeredService, adaptor));
    }
}
