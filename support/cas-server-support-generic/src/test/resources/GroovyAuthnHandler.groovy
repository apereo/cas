import org.apereo.cas.authentication.DefaultAuthenticationHandlerExecutionResult
import org.apereo.cas.authentication.credential.UsernamePasswordCredential
import org.apereo.cas.authentication.metadata.BasicCredentialMetaData

import javax.security.auth.login.AccountNotFoundException
import javax.security.auth.login.FailedLoginException

def authenticate(final Object... args) {
    def authenticationHandler = args[0]
    def credential = args[1]
    def servicesManager = args[2]
    def principalFactory = args[3]
    def logger = args[4]
    if (credential.username == credential.password) {
        def principal = principalFactory.createPrincipal(credential.username);
        return new DefaultAuthenticationHandlerExecutionResult(authenticationHandler,
                new BasicCredentialMetaData(credential),
                principal,
                new ArrayList<>(0));
    }
    throw new FailedLoginException();
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
