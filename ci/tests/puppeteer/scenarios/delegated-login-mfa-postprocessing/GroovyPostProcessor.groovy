def run(Object[] args) {
    def (builder, transaction, logger) = args
    
    logger.info("Current attributes: ${builder.principal.attributes}")
    builder.addAttribute('authnContextClass', ['mfa-duo'])
    true
}

def supports(Object[] args) {
    def (credential, logger) = args
    logger.info("Current credential is ${credential}")
    credential instanceof ClientCredential
}
