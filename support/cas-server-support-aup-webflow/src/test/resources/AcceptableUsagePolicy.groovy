import groovy.transform.Field
import org.apereo.cas.aup.AcceptableUsagePolicyStatus
import org.apereo.cas.aup.AcceptableUsagePolicyTerms

@Field int count = 0

AcceptableUsagePolicyStatus verify(Object[] args) {
    def requestContext = args[0]
    def applicationContext = args[1]
    def principal = args[2]
    def logger = args[3]

    if (count > 1) {
        return AcceptableUsagePolicyStatus.accepted(principal)
    }
    return AcceptableUsagePolicyStatus.denied(principal)
}

def submit(Object[] args) {
    def requestContext = args[0]
    def applicationContext = args[1]
    def principal = args[2]
    def logger = args[3]

    count++
    return true
}

AcceptableUsagePolicyTerms fetch(Object[] args) {
    def requestContext = args[0]
    def applicationContext = args[1]
    def principal = args[2]
    def logger = args[3]

    return AcceptableUsagePolicyTerms.builder()
            .defaultText("Hello, World")
            .code(AcceptableUsagePolicyTerms.CODE)
            .build()
}
