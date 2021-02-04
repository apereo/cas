import org.apereo.cas.pm.*

def change(Object[] args) {
    def credential = args[0]
    def passwordChangeBean = args[1]
    def logger = args[2]
    switch (credential.username) {
        case "bad-credential":
            return false
        case "error-credential":
            throw new InvalidPasswordException()
    }
    return true
}

def findEmail(Object[] args) {
    def username = (args[0] as PasswordManagementQuery).username
    def logger = args[1]
    if (username.equals("none")) {
        return null
    }
    return "cas@example.org"
}

def getSecurityQuestions(Object[] args) {
    def username = (args[0] as PasswordManagementQuery).username
    def logger = args[1]
    if (username.equals("noquestions")) {
        return [:]
    }
    return [securityQuestion1: "securityAnswer1"]
}

def findPhone(Object[] args) {
    def username = (args[0] as PasswordManagementQuery).username
    def logger = args[1]
    if (username.equals("none")) {
        return null
    }
    return "3477463421"
}

def findUsername(Object[] args) {
    def email = (args[0] as PasswordManagementQuery).email
    def logger = args[1]
    if (email.contains("@baddomain")) {
        return null
    }
    return "casuser"
}
