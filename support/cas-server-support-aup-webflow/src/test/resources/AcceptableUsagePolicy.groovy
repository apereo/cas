import org.apereo.cas.authentication.principal.*
import org.apereo.cas.authentication.*
import org.apereo.cas.util.*
import org.apereo.cas.aup.*
import org.springframework.webflow.execution.*
import groovy.transform.Field

@Field int count = 0

AcceptableUsagePolicyStatus verify(Object[] args) {
    def requestContext = args[0]
    def credential = args[1]
    def applicationContext = args[2]
    def principal = args[3]
    def logger = args[4]

    if (count > 1) {
        return AcceptableUsagePolicyStatus.accepted(principal)
    }
    return AcceptableUsagePolicyStatus.denied(principal)
}

def submit(Object[] args) {
    def requestContext = args[0]
    def credential = args[1]
    def applicationContext = args[2]
    def principal = args[3]
    def logger = args[4]

    count++
    return true
}
