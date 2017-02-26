package commands

import org.crsh.cli.Command
import org.crsh.cli.Usage
import org.crsh.command.InvocationContext

class cas {

    @Usage("Output the current version of the CAS server")
    @Command
    def main(InvocationContext context) {
        
        def beans = context.attributes['spring.beanfactory']
        def environment = context.attributes['spring.environment']
        
        def ticketRegistry = beans.getBean("ticketRegistry")
        def serviceRegistry = beans.getBean("serviceRegistryDao")
        
        return "CAS version: " + org.apereo.cas.util.CasVersion.getVersion() +
                "\nTicket registry instance: " + ticketRegistry.getClass().getSimpleName() +
                "\nService registry instance: " + serviceRegistry.getClass().getSimpleName()
    }

}
