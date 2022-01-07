import org.apereo.cas.oidc.jwks.*
import org.jose4j.jwk.*

def run(Object[] args) {
    def logger = args[0]
    logger.info("Generating JWKS for CAS...")
    def jsonWebKey = OidcJsonWebKeyStoreUtils.generateJsonWebKey("RSA", 2048, OidcJsonWebKeyUsage.SIGNING)
    jsonWebKey.setKeyId("caskid")

    def json = jsonWebKey.toJson(JsonWebKey.OutputControlLevel.INCLUDE_PRIVATE);
    logger.info("Generated JWKS ${json}")
    return new JsonWebKeySet(jsonWebKey).toJson()
}

def store(Object[] args) {
    def jwks = args[0] as JsonWebKeySet
    def logger = args[1]
    logger.info("Storing JWKS ${jwks.toJson(JsonWebKey.OutputControlLevel.INCLUDE_PRIVATE)} for CAS...")
    return jwks
}

def find(Object[] args) {
    def logger = args[0]
    logger.info("Looking up keystore...")
    def jsonWebKey = OidcJsonWebKeyStoreUtils.generateJsonWebKey("RSA", 2048, OidcJsonWebKeyUsage.SIGNING)
    return new JsonWebKeySet(jsonWebKey)
}
