import java.util.*

def run(final Object... args) {
    def uid = args[0]
    def logger = args[1]
    def attributes = args[2]

    logger.info("The uid is [{}], current attributes are [{}]", uid, attributes.keySet())
    if (uid.equals("nouser")) {
        return Map.of()
    }
    
    return [username:[uid], likes:["cheese", "food"], id:[1234,2,3,4,5],
           eppn:["casuser2"], groovyOldName: attributes['oldName'],
           groovyNewName: attributes['newName']]
}
