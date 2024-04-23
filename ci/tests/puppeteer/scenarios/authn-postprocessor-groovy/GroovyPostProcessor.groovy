import org.apereo.cas.*
import org.apereo.cas.util.*
import org.apereo.cas.authentication.*
import org.apereo.cas.authentication.credential.*
import java.net.*
import groovy.json.*
import java.security.*

def run(Object[] args) {
    def builder = args[0]
    def transaction = args[1]
    def logger = args[2]

    def credential = transaction.getPrimaryCredential().get()
    def password = credential.password as char[]
    def passwordSha1 = DigestUtils.sha(new String(password)).toUpperCase()
    def passwordSha1Prefix = passwordSha1.substring(0, 5)
    def passwordSha1Suffix = passwordSha1.substring(5)

    logger.info "Password hash: ${passwordSha1}"
    logger.info "Password hash prefix: ${passwordSha1Prefix}"
    logger.info "Password hash suffix: ${passwordSha1Suffix}"
    
    /*
        Contact the API using the SHA-1 digest of the credential password.
        Parse through the results, check for matches and produce a warning
        where appropriate.
     */
    String text = null
    def apiUrl = new URL("https://api.pwnedpasswords.com/range/${passwordSha1Prefix}")
    def connection = apiUrl.openConnection()
    connection.addRequestProperty("Accept", "application/json")
    connection.with {
        doOutput = true
        requestMethod = 'GET'
        text = content.text
    }

    def passwordHasBeenPawned = text.readLines().any {line ->
        logger.debug line
        def hash = line.split(":")[0]
        hash.equalsIgnoreCase(passwordSha1Suffix)
    }
    logger.info "Password is pawned: ${passwordHasBeenPawned}"

    if (passwordHasBeenPawned) {
        builder.addWarning(new DefaultMessageDescriptor("password.pawned"))
    }
}

def supports(Object[] args) {
    def credential = args[0]
    def logger = args[1]
    credential instanceof UsernamePasswordCredential
}
