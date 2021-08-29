import org.apereo.inspektr.common.web.*;

def run(Object... args) {
    def (requestContext, applicationContext, logger) = args
    def clientInfo = ClientInfoHolder.getClientInfo()
    def clientIp = clientInfo.getClientIpAddress()
    logger.info("Client IP Address: ${clientIp}")
    requestContext.flowScope.put('clientIp', clientInfo.getClientIpAddress())
    requestContext.flowScope.put('userAgent', clientInfo.getUserAgent())
}
