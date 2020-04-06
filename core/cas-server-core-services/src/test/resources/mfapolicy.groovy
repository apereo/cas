import org.apereo.cas.services.*

@Deprecated(since = "6.2.0")
class GroovyMultifactorPolicy extends DefaultRegisteredServiceMultifactorPolicy {
    @Override
    Set<String> getMultifactorAuthenticationProviders() {
        ["mfa-something"]
    }

    @Override
    RegisteredServiceMultifactorPolicyFailureModes getFailureMode() {
        RegisteredServiceMultifactorPolicyFailureModes.OPEN
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
