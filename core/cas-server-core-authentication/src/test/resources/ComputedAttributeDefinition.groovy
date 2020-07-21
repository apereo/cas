def run(Object[] args) {
    def attributeName = args[0]
    def attributeValues = args[1]
    def logger = args[2]
    logger.info("name: ${attributeName}, values: ${attributeValues}")
    return ["casuser", "groovy"]
}
