def run(final Object... args) {
    def (attributes,username,registeredService,service,logger) = args
    logger.debug("Current attributes received are {}", attributes)
    return username == "interrupted"
}
