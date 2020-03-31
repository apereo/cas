import java.util.*

def Map<String, List<Object>> run(final Object... args) {
    def uid = args[0]
    def attributes = args[1]
    def logger = args[2]
    def properties = args[3]
    def appContext = args[4]

    logger.info("The uid is [{}]", uid)
    logger.info("Current attributes are [{}]", attributes.keySet())
    return[username:[uid], likes:["cheese", "food"], id:[1234,2,3,4,5],
           eppn:["casuser2"], groovyOldName: attributes['oldName'], groovyNewName: attributes['newName']]
}
