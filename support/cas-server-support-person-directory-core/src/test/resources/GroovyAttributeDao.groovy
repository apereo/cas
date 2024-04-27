import org.apereo.cas.authentication.principal.*
import org.apereo.cas.authentication.*
import org.apereo.cas.util.*

def run(Object[] args) {
    def username = args[0]
    def attributes = args[1]
    def logger = args[2]
    def properties = args[3]
    def appContext = args[4]

    return [name: ["casuser"], nickname: ["CAS", "DaCAS"], givenName: ["CAS"], lastName: ["User"], username: [username]]
}
