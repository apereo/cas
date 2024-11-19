import org.apereo.cas.api.PasswordlessUserAccount
import org.apereo.cas.configuration.support.TriStateBoolean

def run(Object[] args) {
    def account = args[0] as PasswordlessUserAccount
    def applicationContext = args[1]
    def logger = args[2]

    logger.info("Customizing $account")

    account.setMultifactorAuthenticationEligible(TriStateBoolean.TRUE)
    account.setRequestPassword(true)
    account.setAllowedDelegatedClients(List.of("CasClient"))
    account.setAttributes(Map.of("lastName", List.of("Smith", "Smithson")))
    return account
}
