import org.apereo.cas.audit.*
import org.apereo.cas.services.*

def run(Object[] args) {
    def context = args[0] as AuditableContext
    def logger = args[1]
    logger.debug("Checking access for ${context.registeredService}")
    def result = AuditableExecutionResult.builder().build()
    result.setException(new UnauthorizedServiceException("Service unauthorized"))
    return result
}
