def run(Object[] args) {
    def from = args[0]
    def to = args[1]
    def message = args[2]
    def logger = args[3]

    logger.debug("Sending message ${message} to ${to} from ${from}")
    true
}
