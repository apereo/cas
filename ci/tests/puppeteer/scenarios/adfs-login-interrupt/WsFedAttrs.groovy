Map run(final Object... args) {
    def attributes = (Map) args[0]
    def logger = (Logger) args[1]
    logger.info("Mutating attributes {}", attributes)
    def result = new LinkedHashMap(attributes)
    result["email"] = List.of("casuser@example.org")
    result["upn"] = List.of("casuser@apereo.org")
    return result
}
