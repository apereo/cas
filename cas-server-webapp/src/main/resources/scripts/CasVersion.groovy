package scripts

def class CasVersion {
    def static run(def ctx) {
        def output = "CAS version: " + org.apereo.cas.CasVersion.getVersion()

        def ticketRegistry = ctx.getBean("ticketRegistry");
        output += "\nTicket registry instance: " + ticketRegistry.getClass().getSimpleName()
        return output
    }
}
