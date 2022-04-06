def run(final Object... args) {
    def samlContext = args[0]
    def logger = args[1]
    
    logger.info("Building context for entity {}", samlContext.adaptor.entityId)
    return "https://refeds.org/profile/mfa"
}
