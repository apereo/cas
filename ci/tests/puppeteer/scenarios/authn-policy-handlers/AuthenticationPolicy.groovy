import org.apereo.cas.authentication.exceptions.*

def run(Object[] args) {
    def (authentication, context, applicationContext, logger) = args
    def username = authentication.principal.id
    logger.info("Evaluating Groovy authentication policy for {}", username)
    if (username == "blocked") {
        return Optional.of(new AccountDisabledException("Blocked by Groovy authentication policy"))
    }
    return Optional.empty()
}

def shouldResumeOnFailure(Object[] args) {
    def (failure, logger) = args
    return failure != null
}
