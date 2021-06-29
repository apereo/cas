import org.apereo.cas.configuration.model.support.mfa.*
import org.apereo.cas.services.*

@Deprecated(since = "6.2.0")
class GroovyMultifactorPolicy extends DefaultRegisteredServiceMultifactorPolicy {
    @Override
    Set<String> getMultifactorAuthenticationProviders() {
        ["mfa-something"]
    }

    @Override
    BaseMultifactorAuthenticationProviderProperties.MultifactorAuthenticationProviderFailureModes getFailureMode() {
        BaseMultifactorAuthenticationProviderProperties.MultifactorAuthenticationProviderFailureModes.OPEN
    }

    @Override
    String getPrincipalAttributeNameTrigger() {
        "Test"
    }

    @Override
    String getPrincipalAttributeValueToMatch() {
        "TestMatch"
    }

    @Override
    boolean isBypassEnabled() {
        true
    }

    @Override
    boolean isForceExecution() {
        false
    }

    @Override
    boolean isBypassTrustedDeviceEnabled() {
        true
    }
}
