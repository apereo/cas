import org.apereo.cas.api.*

def run(Object[] args) {
    def username = args[0]
    def logger = args[1]
    logger.info("Testing username $username")

    return new PasswordlessUserAccount("casuser",
            "casuser@example.org", "123-456-7890", "CAS")
}
