import org.apereo.cas.api.PasswordlessUserAccount

def run(Object[] args) {
    def username = args[0]
    def logger = args[1]
    
    logger.info("Testing username $username")

    return PasswordlessUserAccount.builder()
            .email("casuser@example.org")
            .phone("1234567890")
            .username("casuser")
            .name("casuser")
            .attributes(Map.of("lastName", List.of("Smith")))
            .build();
}
