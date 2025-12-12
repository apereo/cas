def run(Object[] args) {
    def (attributes, context, claim, logger) = args

    logger.info("Mapping claim ${claim}")
    if (claim == "family_name") {
        return ["PATTERSON"]
    }
    return []
}
