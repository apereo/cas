def run(Object[] args) {
    def params = (args[0] as Map).values()
    def logger = args[1]

    logger.info("Parameters are {}", args[0])
    return String.format("%s, %s", params[0], params[1])
}
