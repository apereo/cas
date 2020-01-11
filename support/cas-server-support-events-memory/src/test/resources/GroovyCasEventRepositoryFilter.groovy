def shouldSaveEvent(Object[] args) {
    def event = args[0]
    def logger = args[1]

    logger.debug("Decide whether ${event} should be saved...")
    true
}
