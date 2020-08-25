import org.apereo.cas.api.*

def run(Object[] args) {
    def username = args[0]
    def logger = args[1]
    logger.info("Testing username $username")

    if (username.equals("unknown")) {
        return null
    }
    
    return PasswordlessUserAccount.builder()
        .email("casuser@example.org")
        .phone("123-456-7890")
        .username("casuser")
        .name("CAS")
        .build();
}
