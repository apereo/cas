class MfaPredicate implements Predicate<MultifactorAuthenticationProvider> {
    def service
    def principal
    def providers
    def logger

    MfaPredicate(service, principal, providers, logger) {
        this.service = service
        this.principal = principal
        this.providers = providers
        this.logger = logger
    }
    
    @Override
    boolean test(MultifactorAuthenticationProvider provider) {
        logger.info("Testing provider {}", provider.id)
        def result = provider.matches("mfa-gauth")
        logger.info("Provider {} result: {}", provider.id, result)
        return result
    }
}
