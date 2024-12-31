import org.apereo.cas.api.PasswordlessAuthenticationRequest
import org.apereo.cas.api.PasswordlessUserAccount

def run(Object[] args) {
    def request = args[0] as PasswordlessAuthenticationRequest
    def logger = args[1]
    logger.info("Testing username $request")

    if (request.username.equals("unknown")) {
        return null
    }

    if (request.username.equals("nouserinfo")) {
        return PasswordlessUserAccount.builder()
                .username("nouserinfo")
                .name("CAS")
                .build()
    }

    if (request.username.equals("needs-password")) {
        return PasswordlessUserAccount.builder()
                .username("nouserinfo")
                .name("CAS")
                .email("casuser@example.org")
                .phone("123-456-7890")
                .requestPassword(true)
                .build()
    }

    if (request.username.equals("needs-selection")) {
        return PasswordlessUserAccount.builder()
                .username("nouserinfo")
                .name("CAS")
                .email("casuser@example.org")
                .phone("123-456-7890")
                .allowSelectionMenu(true)
                .build()
    }

    if (request.username.equals("needs-password-user-without-email-or-phone")) {
        return PasswordlessUserAccount.builder()
                .username("nouserinfo")
                .name("CAS")
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
