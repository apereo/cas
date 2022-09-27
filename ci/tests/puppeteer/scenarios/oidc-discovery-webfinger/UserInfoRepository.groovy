def findByUsername(Object[] args) {
    def username = args[0]
    def logger = args[1]
    return [username: "casuser"]
}

def findByEmailAddress(Object[] args) {
    def email = args[0]
    def logger = args[1]
    return [email: "casuser@localhost.org"]
}
