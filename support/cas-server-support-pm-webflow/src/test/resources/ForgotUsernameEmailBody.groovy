import org.apereo.cas.authentication.principal.*
import org.apereo.cas.util.*

def run(Object[] args) {
    def parameters = args[0] as Map
    def logger = args[1]
    logger.info("Parameters are {}", parameters)
    def email = parameters["email"]
    def principal = parameters["principal"] as Principal

    def attributes = principal.getAttributes()
    return String.format("Hello %s with email %s, your affiliation is %s",
            CollectionUtils.firstElement(attributes.get("uid")).get().toString(),
            email,
            CollectionUtils.firstElement(attributes.get("eduPersonAffiliation")).get().toString())
}
