import java.util.*

def run(final Object... args) {
    def attributes = args[0] as Map
    def logger = args[1]
    logger.info "Attributes currently resolved: ${attributes}"
    return [ COURSE: [attributes['groupMembership6'][0] + '-101'] ]
}
