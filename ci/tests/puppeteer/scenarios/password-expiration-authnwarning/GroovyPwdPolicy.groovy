import org.apereo.cas.authentication.*
import org.apereo.cas.authentication.support.password.*

List<MessageDescriptor> run(final Object... args) {
    def response = args[0]
    def configuration = args[1]
    def logger = args[2]
    
    logger.info("Handling password policy for [{}]", response)
    return [new PasswordExpiringWarningMessageDescriptor("Password expires in {0} days.", 0)]
}
