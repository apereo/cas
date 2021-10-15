import org.apereo.cas.audit.*
import org.apereo.cas.services.*

def run(Object[] args) {
    def context = args[0] as AuditableContext
    def logger = args[1]
    logger.info("Checking access for ${context.registeredService}")
    if (context.service.isPresent()) {
        def service = context.service.get()
        logger.info("Service request is ${service.id}")
        if (service.id.startsWith("https://apereo.github.io")) {
            def result = AuditableExecutionResult.builder().build()
            result.setException(new UnauthorizedServiceException("Service unauthorized"))
            return result
        }
    }
    return AuditableExecutionResult.builder().build()
}
