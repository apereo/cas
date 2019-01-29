def run(Object[] args) {
    def principal = args[0]
    def userProfile = args[1]
    def client = args[2]
    def logger = args[3]

    logger.info("Principal id is ${principal.id}, client id ${client.name} and profile id is ${userProfile.id}")
}
