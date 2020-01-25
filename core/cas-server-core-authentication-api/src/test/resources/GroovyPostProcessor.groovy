import org.apereo.cas.*

def run(Object[] args) {
    def builder = args[0]
    def transaction = args[1]
    def logger = args[2]

    def credential = transaction.getPrimaryCredential().get()
    builder.successes["SimpleTestUsernamePasswordAuthenticationHandler"].addWarning(new DefaultMessageDescriptor("some.authn.message"))
    true
}

def supports(Object[] args) {
    def credential = args[0]
    def logger = args[1]
    true
}
