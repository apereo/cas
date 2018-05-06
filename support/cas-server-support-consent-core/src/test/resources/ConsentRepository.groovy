def read(Object[] args) {
    def currentConsentDecisions = args[0]
    def logger = args[1]

    currentConsentDecisions
}

def write(Object[] args) {
    def consentDecision = args[0]
    def logger = args[1]
    true
}

def delete(Object[] args) {
    def decisionId = args[0]
    def principalId = args[1]
    def logger = args[2]
    true
}
