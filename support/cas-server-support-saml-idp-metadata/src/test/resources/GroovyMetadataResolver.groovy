import org.apereo.cas.authentication.principal.*
import org.apereo.cas.authentication.*
import org.apereo.cas.support.saml.*
import org.apereo.cas.util.*
import org.springframework.core.io.*

def run(Object[] args) {
    def registeredService = args[0]
    def samlConfigBean = args[1]
    def samlIdPProperties = args[2]
    def logger = args[3]

    return new InMemoryResourceMetadataResolver(new ClassPathResource("sample-sp.xml"), samlConfigBean);
}
