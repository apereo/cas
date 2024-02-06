def run(Object[] args) {
    def (registeredService,authentication,logger) = args
    logger.info("Checking SSO participation for ${registeredService.name}")

    def principal = authentication.principal
    logger.info("Principal id is ${principal.id}")
    if (principal.id == 'Gandalf') {
        logger.info("User is too powerful; SSO participation is allowed")
        return true
    }
    return false
}
