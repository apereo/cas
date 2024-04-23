def run(Object[] args) {
    def (context,logger) = args
    def principal = context.principal
    logger.info("Principal id is ${principal.id}")
    if (principal.id == 'Gandalf') {
        logger.info("User is too powerful; Releasing attributes is allowed")
        return true
    }
    return false
}
