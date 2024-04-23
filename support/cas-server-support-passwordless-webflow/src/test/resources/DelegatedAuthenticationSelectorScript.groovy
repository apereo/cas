import org.apereo.cas.api.*

def run(Object[] args) {
    def passwordlessUser = args[0] as PasswordlessUserAccount
    def clients = (Set) args[1]
    def httpServletRequest = args[2]
    def logger = args[3]
    
    logger.info("Testing username $passwordlessUser")

    return passwordlessUser.username == "unknown" ? null : clients[0]
}
