import org.apereo.cas.api.PasswordlessAuthenticationRequest
import org.apereo.cas.api.PasswordlessUserAccount

def run(Object[] args) {
    def request = args[0] as PasswordlessAuthenticationRequest
    def logger = args[1]
    
    logger.info("Testing username $request")

    return PasswordlessUserAccount
            .builder()
            .email("casuser@example.org")
            .phone("1234567890")
            .username("casuser")
            .name("casuser")
            .attributes(Map.of("lastName", List.of("Smith")))
            .build()
}
