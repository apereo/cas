import org.apereo.cas.authentication.principal.*
import org.apereo.cas.authentication.*
import org.apereo.cas.util.*

def run(Object[] args) {
    def username = args[0]
    def logger = args[1]
    def properties = args[2]
    def applicationContext = args[3]

    return [name: ["casuser"], nickname: ["CAS", "DaCAS"], givenName: ["CAS"], lastName: ["User"], username: [username]]
}
