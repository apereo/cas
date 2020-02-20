import java.util.*

Map<String, List<Object>> run(final Object... args) {
    def uid = args[0]
    def logger = args[1]
    sleep(10)
    logger.debug("Things are happening just fine with uid: {}", uid)
    return[username:[uid], likes:["cheese", "food"], id:[1234,2,3,4,5], another:"attribute"]
}

