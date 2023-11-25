import org.apereo.cas.authentication.*

def run(Object[] args) {
    def (authentication, context, applicationContext, logger) = args
    var result = (authentication as Authentication).credentials.find
            { it.credentialMetadata.containsProperty("mustFail")}
    return result != null ? Optional.of(new AuthenticationException()) : Optional.empty()
}
