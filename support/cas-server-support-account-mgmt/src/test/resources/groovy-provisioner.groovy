import org.apereo.cas.acct.*
import org.springframework.context.ApplicationContext

def run(Object[] args) {
    def registrationRequest = args[0] as AccountRegistrationRequest
    def applicationContext = args[1] as ApplicationContext
    def logger = args[2]
    logger.info("Provisioning account registration request ${registrationRequest}")
    return AccountRegistrationResponse.success()
}
