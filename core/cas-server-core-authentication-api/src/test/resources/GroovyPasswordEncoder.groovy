def byte[] run(final Object... args) {
    def rawPassword = args[0]
    def generatedSalt = args[1]
    def logger = args[2]
    def casApplicationContext = args[3]

    logger.debug("Encoding password...")
    return rawPassword.toString().toUpperCase().getBytes("UTF-8")
}
