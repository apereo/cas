import java.util.*

def run(final Object... args) {
    def attributes = args[0]
    def logger = args[1]
    
    logger.debug("Current attributes received are {}", attributes)
    return ['DOMAIN\\' + attributes['uid'][0], 'testing']
}
