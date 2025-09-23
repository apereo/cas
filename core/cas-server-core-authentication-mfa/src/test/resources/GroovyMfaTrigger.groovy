def run(final Object... args) {
    def service = args[0] as WebApplicationService
    def registeredService = args[1]
    def authentication = args[2]
    def httpRequest = args[3]
    def logger = args[4]

    if ("nomfa".equalsIgnoreCase(service?.id)) {
        return null
    }
    if ("composite".equalsIgnoreCase(service?.id)) {
        return ChainingMultifactorAuthenticationProvider.DEFAULT_IDENTIFIER
    }
    return TestMultifactorAuthenticationProvider.ID
}
