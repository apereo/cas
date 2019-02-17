import org.apereo.cas.*

def findByUsername(Object[] args) {
    def username = args[0]
    def logger = args[1]
    return [username: username]
}

def findByEmailAddress(Object[] args) {
    def email = args[0]
    def logger = args[1]
    return [email: email]
}
