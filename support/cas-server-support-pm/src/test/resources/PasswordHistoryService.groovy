def exists(Object[] args) {
    def request = args[0]
    def logger = args[1]
    return false
}

def store(Object[] args) {
    def request = args[0]
    def logger = args[1]
    return true
}

def fetchAll(Object[] args) {
    def logger = args[0]
    return []
}

def fetch(Object[] args) {
    def username = args[0]
    def logger = args[1]
    return []
}

def remove(Object[] args) {
}

def removeAll(Object[] args) {
}
