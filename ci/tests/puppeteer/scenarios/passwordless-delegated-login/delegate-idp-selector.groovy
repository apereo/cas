def run(Object[] args) {
    def passwordlessUser = args[0]
    def clients = (Set) args[1]
    def httpServletRequest = args[2]
    def logger = args[3]
    return clients[0]
}
