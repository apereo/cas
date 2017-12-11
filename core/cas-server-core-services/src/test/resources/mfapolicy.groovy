import org.apereo.cas.services.*

class GroovyMultifactorPolicy extends DefaultRegisteredServiceMultifactorPolicy {
    @Override
    Set<String> getMultifactorAuthenticationProviders() {
        ["mfa-something"]
    }

    @Override
    RegisteredServiceMultifactorPolicy.FailureModes getFailureMode() {
        RegisteredServiceMultifactorPolicy.FailureModes.OPEN
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
}
