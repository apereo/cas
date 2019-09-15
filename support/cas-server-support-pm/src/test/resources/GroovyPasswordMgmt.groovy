def change(Object[] args) {
    def credential = args[0]
    def passwordChangeBean = args[1]
    def logger = args[2]
    return true
}

def findEmail(Object[] args) {
    def username = args[0]
    def logger = args[1]
    return "cas@example.org"
}

def findPhone(Object[] args) {
    def username = args[0]
    def logger = args[1]
    return "3477463421"
}

def findUsername(Object[] args) {
    def email = args[0]
    def logger = args[1]
    return "casuser"
}

def getSecurityQuestions(Object[] args) {
    def username = args[0]
    def logger = args[1]
    return [securityQuestion1: "securityAnswer1"]
}
