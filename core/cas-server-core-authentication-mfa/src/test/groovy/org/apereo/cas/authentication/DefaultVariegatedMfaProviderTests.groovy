package org.apereo.cas.authentication

import org.apereo.cas.services.MultifactorAuthenticationProvider
import org.apereo.cas.services.RegisteredService
import org.junit.Before
import org.junit.Test

import static org.junit.Assert.*
import static org.mockito.Mockito.*

/**
 * @author Dmitriy Kopylenko
 */
class DefaultVariegatedMfaProviderTests {

    def mfaProvider1 = mock(MultifactorAuthenticationProvider)

    def mfaProvider2 = mock(MultifactorAuthenticationProvider)

    def registeredService = mock(RegisteredService)

    static MFA_PROVIDER1_ID = 'MFA1'

    static MFA_PROVIDER1_ORDER = 0

    static MFA_PROVIDER2_ID = 'MFA2'

    static MFA_PROVIDER2_ORDER = 100

    static BOGUS_ID = 'BOGUS'

    @Before
    def initialize() {
        mfaProvider1.id >> MFA_PROVIDER1_ID
        mfaProvider1.order >> MFA_PROVIDER1_ORDER
        mfaProvider1.friendlyName >> MFA_PROVIDER1_ID
        mfaProvider1.matches(MFA_PROVIDER1_ID) >> true
        mfaProvider1.matches(BOGUS_ID) >> false

        mfaProvider2.id >> MFA_PROVIDER2_ID
        mfaProvider2.order >> MFA_PROVIDER2_ORDER
        mfaProvider2.friendlyName >> MFA_PROVIDER2_ID
        mfaProvider2.matches(MFA_PROVIDER2_ID) >> true
        mfaProvider2.matches(BOGUS_ID) >> false
    }

    @Test
    verifySingleProviderProperties() {
        def variegatedProvider = createVariegatedProviderWith([mfaProvider1])
        variegatedProvider.id == MFA_PROVIDER1_ID
        variegatedProvider.order == MFA_PROVIDER1_ORDER
    }

    @Test
    verifyMultipleProvidersProperties() {
        def variegatedProvider = createVariegatedProviderWith([mfaProvider2, mfaProvider1])
        variegatedProvider.id == MFA_PROVIDER2_ID
        variegatedProvider.order == MFA_PROVIDER2_ORDER
    }

    @Test
    verifyProviderUnavailability() {
        mfaProvider1.isAvailable(registeredService) >> true
        mfaProvider2.isAvailable(registeredService) >> false
        def variegatedProvider = createVariegatedProviderWith([mfaProvider1, mfaProvider2])
        !variegatedProvider.isAvailable(registeredService)
    }

    @Test
    verifyProviderAvailability() {
        mfaProvider1.isAvailable(registeredService) >> true
        mfaProvider2.isAvailable(registeredService) >> true
        def variegatedProvider = createVariegatedProviderWith([mfaProvider1, mfaProvider2])
        variegatedProvider.isAvailable(registeredService)
    }

    @Test
    def verifyMatches() {
        def variegatedProvider = createVariegatedProviderWith([mfaProvider1, mfaProvider2])
        variegatedProvider.matches(MFA_PROVIDER1_ID)
        variegatedProvider.matches(MFA_PROVIDER2_ID)
        !variegatedProvider.matches(BOGUS_ID)
    }

    private static createVariegatedProviderWith(List<MultifactorAuthenticationProvider> providers) {
        new DefaultVariegatedMultifactorAuthenticationProvider().with { variegated ->
            providers.each {
                variegated.addProvider(it)
            }
            variegated
        }
    }
}
