package org.apereo.cas.services;

import org.apereo.cas.authentication.principal.ShibbolethCompatiblePersistentIdGenerator;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link PairwiseOidcRegisteredServiceUsernameAttributeProviderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("OIDC")
public class PairwiseOidcRegisteredServiceUsernameAttributeProviderTests {
    @Test
    public void verifyNonCompatibleService() {
        val provider = new PairwiseOidcRegisteredServiceUsernameAttributeProvider();
        val uid = provider.resolveUsername(RegisteredServiceTestUtils.getPrincipal("casuser"), RegisteredServiceTestUtils.getService(),
            RegisteredServiceTestUtils.getRegisteredService());
        assertEquals("casuser", uid);
    }

    @Test
    public void verifyUndefinedOrPublicSubjectType() {
        val provider = new PairwiseOidcRegisteredServiceUsernameAttributeProvider();

        val registeredService = new OidcRegisteredService();
        registeredService.setName("verifyUndefinedOrPublicSubjectType");
        registeredService.setServiceId("testId");
        registeredService.setClientId("clientid");
        registeredService.setClientSecret("something");

        registeredService.setSubjectType(StringUtils.EMPTY);
        var uid = provider.resolveUsername(RegisteredServiceTestUtils.getPrincipal("casuser"), RegisteredServiceTestUtils.getService(), registeredService);
        assertEquals("casuser", uid);

        registeredService.setSubjectType(null);
        uid = provider.resolveUsername(RegisteredServiceTestUtils.getPrincipal("casuser"), RegisteredServiceTestUtils.getService(), registeredService);
        assertEquals("casuser", uid);

        registeredService.setSubjectType(OidcSubjectTypes.PUBLIC.getType());
        uid = provider.resolveUsername(RegisteredServiceTestUtils.getPrincipal("casuser"), RegisteredServiceTestUtils.getService(), registeredService);
        assertEquals("casuser", uid);
    }

    @Test
    public void verifySubjectType() {
        val provider = new PairwiseOidcRegisteredServiceUsernameAttributeProvider();
        provider.setPersistentIdGenerator(new ShibbolethCompatiblePersistentIdGenerator("cpaOl1pwGZ439!!"));

        val registeredService = new OidcRegisteredService();
        registeredService.setName("verifySubjectType");
        registeredService.setSectorIdentifierUri("https://sso.example.org/oidc");
        registeredService.setClientId("clientid");
        registeredService.setClientSecret("something");
        registeredService.setSubjectType(OidcSubjectTypes.PAIRWISE.getType());

        val uid = provider.resolveUsername(RegisteredServiceTestUtils.getPrincipal("casuser"), RegisteredServiceTestUtils.getService(), registeredService);
        assertEquals("9IOlxFj2XgfhkNJieynbw+Pm+4E=", uid);

        registeredService.setSectorIdentifierUri(null);
        registeredService.setServiceId("https://sso.example.org/oidc");
        val uid1 = provider.resolveUsername(RegisteredServiceTestUtils.getPrincipal("casuser"), RegisteredServiceTestUtils.getService(), registeredService);
        assertEquals(uid1, uid);
    }
}
