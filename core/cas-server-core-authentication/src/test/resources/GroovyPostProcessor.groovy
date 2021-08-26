import org.apereo.cas.*

def run(Object[] args) {
    def builder = args[0]
    def transaction = args[1]
    def logger = args[2]
    true
}

def supports(Object[] args) {
    def credential = args[0]
    def logger = args[1]
    true
}
