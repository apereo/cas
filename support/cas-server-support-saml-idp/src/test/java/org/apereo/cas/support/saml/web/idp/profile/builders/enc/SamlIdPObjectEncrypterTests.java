package org.apereo.cas.support.saml.web.idp.profile.builders.enc;

import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.SamlException;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.NameID;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link SamlIdPObjectEncrypterTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("SAML")
@TestPropertySource(properties = "cas.authn.saml-idp.metadata.file-system.location=classpath:metadata/")
public class SamlIdPObjectEncrypterTests extends BaseSamlIdPConfigurationTests {
    @Test
    public void verifyEncOptional() {
        val registeredService = getSamlRegisteredServiceForTestShib(true, false, true);
        registeredService.setEncryptionOptional(true);
        registeredService.setEncryptionBlackListedAlgorithms(CollectionUtils.wrapArrayList("excludeAlg1"));
        registeredService.setEncryptionWhiteListedAlgorithms(CollectionUtils.wrapArrayList("includeAlg1"));
        registeredService.setWhiteListBlackListPrecedence("exclude");

        val adaptor = SamlRegisteredServiceServiceProviderMetadataFacade
            .get(samlRegisteredServiceCachingMetadataResolver, registeredService,
                registeredService.getServiceId()).get();
        assertNull(samlIdPObjectEncrypter.encode(mock(Assertion.class), registeredService, adaptor));
        assertNull(samlIdPObjectEncrypter.encode(mock(NameID.class), registeredService, adaptor));
    }

    @Test
    public void verifyEncBadService() {
        val registeredService = getSamlRegisteredServiceForTestShib(true, false, true);
        registeredService.setServiceId("https://noenc.example.org");
        registeredService.setEncryptionOptional(true);
        registeredService.setEncryptionBlackListedAlgorithms(null);
        registeredService.setEncryptionWhiteListedAlgorithms(null);

        val adaptor = SamlRegisteredServiceServiceProviderMetadataFacade
            .get(samlRegisteredServiceCachingMetadataResolver, registeredService,
                registeredService.getServiceId()).get();
        assertNull(samlIdPObjectEncrypter.encode(mock(Assertion.class), registeredService, adaptor));
    }

    @Test
    public void verifyEncNotOptional() {
        val registeredService = getSamlRegisteredServiceForTestShib(true, false, true);
        registeredService.setServiceId("https://noenc.example.org");
        registeredService.setEncryptionOptional(false);

        val adaptor = SamlRegisteredServiceServiceProviderMetadataFacade
            .get(samlRegisteredServiceCachingMetadataResolver, registeredService,
                registeredService.getServiceId()).get();
        assertThrows(SamlException.class,
            () -> samlIdPObjectEncrypter.encode(mock(Assertion.class), registeredService, adaptor));
    }
}
