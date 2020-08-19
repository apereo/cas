def run(Object[] args) {
    def configurer = args[0]
    def applicationContext = args[1]
    def logger = args[2]
    logger.info("Configuring the webflow with ${configurer}")
}
