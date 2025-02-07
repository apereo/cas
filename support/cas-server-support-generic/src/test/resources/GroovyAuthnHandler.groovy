import org.apereo.cas.authentication.DefaultAuthenticationHandlerExecutionResult
import org.apereo.cas.authentication.credential.UsernamePasswordCredential

import javax.security.auth.login.FailedLoginException

def authenticate(final Object... args) {
    def authenticationHandler = args[0]
    def credential = args[1]
    def servicesManager = args[2]
    def principalFactory = args[3]
    def logger = args[4]
    if (credential.username == credential.toPassword()) {
        def principal = principalFactory.createPrincipal(credential.username)
        return new DefaultAuthenticationHandlerExecutionResult(authenticationHandler,
                credential, principal, new ArrayList<>())
    }
    throw new FailedLoginException()
}

def supportsCredential(final Object... args) {
    def credential = args[0]
    def logger = args[1]

    return credential != null
}

def supportsCredentialClass(final Object... args) {
    def credentialClazz = args[0]
    def logger = args[1]

    return credentialClazz == UsernamePasswordCredential.class
}
