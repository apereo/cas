def run(Object[] args) {
    def authentication = args[0]
    def principal = args[1]
    def registeredService = args[2]
    def provider = args[3]
    def LOGGER = args[4]
    def request = args[5]

    return principal.id.equals("casuser")
}
