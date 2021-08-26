import org.apereo.cas.api.PasswordlessUserAccount

def run(Object[] args) {
    def username = args[0]
    def logger = args[1]
    logger.info("Testing username $username")

    if (username.equals("unknown")) {
        return null
    }

    if (username.equals("nouserinfo")) {
        return PasswordlessUserAccount.builder()
                .username("nouserinfo")
                .name("CAS")
                .build()
    }

    if (username.equals("needs-password")) {
        return PasswordlessUserAccount.builder()
                .username("nouserinfo")
                .name("CAS")
                .email("casuser@example.org")
                .phone("123-456-7890")
                .requestPassword(true)
                .build()
    }

    return PasswordlessUserAccount.builder()
            .email("casuser@example.org")
            .phone("123-456-7890")
            .username("casuser")
            .name("CAS")
            .build()
}
