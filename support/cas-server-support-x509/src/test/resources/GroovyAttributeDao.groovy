import java.util.*

def Map<String, List<Object>> run(final Object... args) {
    def uid = args[0]
    def attributes = args[1]
    def logger = args[2]
    def properties = args[3]
    def appContext = args[4]

    logger.info("The uid is [{}] with query attributes [{}]", uid, attributes.keySet())
    return[username:[uid], groovySubjectX500Principal: attributes['subjectX500Principal']]
}
