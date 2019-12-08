def run(final Object... args) {
    def attributes = args[0]
    def logger = args[1]

    logger.info "Attributes currently resolved: ${attributes}"
    return [attributes["cn"][0] + "@example.org"]
}
