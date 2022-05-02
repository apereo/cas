import java.util.*
import org.apereo.cas.authentication.exceptions.*
import javax.security.auth.login.*

def run(final Object... args) {
    def principal = args[0]
    def logger = args[1]

    if (conditionYouMayDesign() == true) {
        return new AccountDisabledException()
    }
    return null
}

def shouldResumeOnFailure(final Object... args) {
    def failure = args[0]
    def logger = args[1]

    if (failure instanceof AccountNotFoundException) {
        return true
    }
    return false
}
