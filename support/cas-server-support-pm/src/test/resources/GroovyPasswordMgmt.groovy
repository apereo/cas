import org.apereo.cas.pm.PasswordManagementQuery

def change(Object[] args) {
    def credential = args[0]
    def passwordChangeBean = args[1]
    def logger = args[2]
    return true
}

def findEmail(Object[] args) {
    def username = args[0].getUsername()
    def logger = args[1]
    return "cas@example.org"
}

def findPhone(Object[] args) {
    def username = args[0].getUsername()
    def logger = args[1]
    return "3477463421"
}

def findUsername(Object[] args) {
    def email = args[0].getEmail()
    def logger = args[1]
    return "casuser"
}

def getSecurityQuestions(Object[] args) {
    def username = args[0].getUsername()
    def logger = args[1]
    return [securityQuestion1: "securityAnswer1"]
}

def updateSecurityQuestions(Object[] args) {
    def query = args[0] as PasswordManagementQuery
    def logger = args[1]
    assert query.username != null
}
