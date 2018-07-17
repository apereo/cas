package org.apereo.cas.authentication.mfa;

import org.apereo.cas.authentication.DefaultVariegatedMultifactorAuthenticationProvider;
import org.apereo.cas.services.MultifactorAuthenticationProvider;
import org.apereo.cas.services.RegisteredService;

import lombok.val;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DefaultVariegatedMultifactorAuthenticationProviderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
public class DefaultVariegatedMultifactorAuthenticationProviderTests {
    private static final String MFA_PROVIDER1_ID = "MFA1";
    private static final String MFA_PROVIDER2_ID = "MFA2";
    private static final String MFA_PROVIDER_BOGUS = "BOGUS";

    private static final int MFA_PROVIDER1_ORDER = 0;
    private static final int MFA_PROVIDER2_ORDER = 1;

    private final MultifactorAuthenticationProvider mfaProvider1 = mock(MultifactorAuthenticationProvider.class);
    private final MultifactorAuthenticationProvider mfaProvider2 = mock(MultifactorAuthenticationProvider.class);
    private final RegisteredService registeredService = mock(RegisteredService.class);

    private static MultifactorAuthenticationProvider createVariegatedProviderWith(final MultifactorAuthenticationProvider... providers) {
        val provider = new DefaultVariegatedMultifactorAuthenticationProvider();
        provider.addProviders(providers);
        return provider;
    }

    @Before
    public void initialize() {
        when(mfaProvider1.getId()).thenReturn(MFA_PROVIDER1_ID);
        when(mfaProvider1.getOrder()).thenReturn(MFA_PROVIDER1_ORDER);
        when(mfaProvider1.getFriendlyName()).thenReturn(MFA_PROVIDER1_ID);
        when(mfaProvider1.matches(MFA_PROVIDER1_ID)).thenReturn(true);
        when(mfaProvider1.matches(MFA_PROVIDER_BOGUS)).thenReturn(false);

        when(mfaProvider2.getId()).thenReturn(MFA_PROVIDER2_ID);
        when(mfaProvider2.getOrder()).thenReturn(MFA_PROVIDER2_ORDER);
        when(mfaProvider2.getFriendlyName()).thenReturn(MFA_PROVIDER2_ID);
        when(mfaProvider2.matches(MFA_PROVIDER2_ID)).thenReturn(true);
        when(mfaProvider2.matches(MFA_PROVIDER_BOGUS)).thenReturn(false);
    }

    @Test
    public void verifySingleProviderProperties() {
        val variegatedProvider = createVariegatedProviderWith(mfaProvider1);
        assertEquals(MFA_PROVIDER1_ID, variegatedProvider.getId());
        assertEquals(MFA_PROVIDER1_ORDER, variegatedProvider.getOrder());
    }

    @Test
    public void verifyMultipleProvidersProperties() {
        val variegatedProvider = createVariegatedProviderWith(mfaProvider2, mfaProvider1);
        assertEquals(MFA_PROVIDER2_ID, variegatedProvider.getId());
        assertEquals(MFA_PROVIDER2_ORDER, variegatedProvider.getOrder());
    }

    @Test
    public void verifyProviderUnavailability() {
        when(mfaProvider1.isAvailable(registeredService)).thenReturn(true);
        when(mfaProvider2.isAvailable(registeredService)).thenReturn(false);
        val variegatedProvider = createVariegatedProviderWith(mfaProvider1, mfaProvider2);
        assertTrue(!variegatedProvider.isAvailable(registeredService));
    }

    @Test
    public void verifyProviderAvailability() {
        when(mfaProvider1.isAvailable(registeredService)).thenReturn(true);
        when(mfaProvider2.isAvailable(registeredService)).thenReturn(true);
        val variegatedProvider = createVariegatedProviderWith(mfaProvider1, mfaProvider2);
        assertTrue(variegatedProvider.isAvailable(registeredService));
    }

    @Test
    public void verifyMatches() {
        val variegatedProvider = createVariegatedProviderWith(mfaProvider1, mfaProvider2);
        assertTrue(variegatedProvider.matches(MFA_PROVIDER1_ID));
        assertTrue(variegatedProvider.matches(MFA_PROVIDER2_ID));
        assertTrue(!variegatedProvider.matches(MFA_PROVIDER_BOGUS));
    }
}
