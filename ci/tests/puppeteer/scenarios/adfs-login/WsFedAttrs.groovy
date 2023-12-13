Map run(final Object... args) {
    def attributes = (Map) args[0]
    def logger = (Logger) args[1]
    logger.info("Mutating attributes {}", attributes)
    def result = new LinkedHashMap(attributes)
    result.put("email", List.of("casuser@example.org"))
    result.put("upn", List.of("casuser@apereo.org"))
    return result
}
