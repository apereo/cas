package org.apereo.cas.authentication

import org.apereo.cas.services.MultifactorAuthenticationProvider
import org.apereo.cas.services.RegisteredService
import spock.lang.Specification

/**
 * @author Dmitriy Kopylenko
 */
class DefaultVariegatedMfaProviderTests extends Specification {

    def mfaProvider1 = Stub(MultifactorAuthenticationProvider)

    def mfaProvider2 = Stub(MultifactorAuthenticationProvider)

    def registeredService = Mock(RegisteredService)

    static MFA_PROVIDER1_ID = 'MFA1'

    static MFA_PROVIDER1_ORDER = 0

    static MFA_PROVIDER2_ID = 'MFA2'

    static MFA_PROVIDER2_ORDER = 100

    static BOGUS_ID = 'BOGUS'

    def setup() {
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

    def "correct usage of id and order properties with one provider"() {
        when: 'variegated wrapper configured with 1 provider'
        def variegatedProvider = createVariegatedProviderWith([mfaProvider1])

        then: 'it will return id and order properties from this only provider'
        variegatedProvider.id == MFA_PROVIDER1_ID
        variegatedProvider.order == MFA_PROVIDER1_ORDER
    }

    def 'correct usage of id and order properties with two providers'() {
        when: 'variegated wrapper configured with 2 providers, placing provider2 first on the list'
        def variegatedProvider = createVariegatedProviderWith([mfaProvider2, mfaProvider1])

        then: 'it will return id and order from the first one added to it'
        variegatedProvider.id == MFA_PROVIDER2_ID
        variegatedProvider.order == MFA_PROVIDER2_ORDER
    }

    def 'unavailable based on providers availability'() {
        given: 'two providers: first is available and second is NOT available'
        mfaProvider1.isAvailable(registeredService) >> true
        mfaProvider2.isAvailable(registeredService) >> false

        when: 'these providers are added to their variegated wrapper'
        def variegatedProvider = createVariegatedProviderWith([mfaProvider1, mfaProvider2])

        then: 'variegated provider is unavailable because NOT ALL its wrapped providers are available'
        !variegatedProvider.isAvailable(registeredService)
    }

    def 'available based on providers availability'() {
        given: 'two providers: BOTH available'
        mfaProvider1.isAvailable(registeredService) >> true
        mfaProvider2.isAvailable(registeredService) >> true

        when: 'these providers are added to their variegated wrapper'
        def variegatedProvider = createVariegatedProviderWith([mfaProvider1, mfaProvider2])

        then: 'variegated provider is available because ALL its wrapped providers are available'
        variegatedProvider.isAvailable(registeredService)
    }

    def 'correct `matches()` method behavior'() {
        when: 'variegated wrapper configured with 2 providers'
        def variegatedProvider = createVariegatedProviderWith([mfaProvider1, mfaProvider2])

        then: 'matches their ids'
        variegatedProvider.matches(MFA_PROVIDER1_ID)
        variegatedProvider.matches(MFA_PROVIDER2_ID)

        and: 'does not match on unavailable id passed'
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
