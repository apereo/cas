def run(Object[] args) {
    def requestContext = args[0]
    def service = args[1]
    def registeredService = args[2]
    def providers = args[3] as Set<DelegatedClientIdentityProviderConfiguration>
    def appContext = args[4]
    def logger = args[5]

    providers.forEach(provider -> {
        logger.info("Checking provider ${provider.name} for service ${service?.id}...")
        if (service != null && service.id.startsWith("https://github.com/apereo/cas")) {
            provider.autoRedirectType = DelegationAutoRedirectTypes.CLIENT
            logger.info("Selected primary provider ${provider.name}")
            return provider
        }
    })
    return null
}
