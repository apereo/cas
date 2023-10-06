import org.pac4j.saml.credentials.*
import org.opensaml.core.xml.*
import org.opensaml.saml.saml2.core.*

def run(Object[] args) {
    def attribute = args[0]
    def logger = args[1]
    
    logger.info("Converting attribute ${attribute}")
    def samlAttribute = new SAML2AuthenticationCredentials.SAMLAttribute()
    samlAttribute.setFriendlyName(attribute.getFriendlyName())
    samlAttribute.setName(attribute.getName())
    samlAttribute.setNameFormat(attribute.getNameFormat())
    attribute.getAttributeValues()
            .stream()
            .map(XMLObject::getDOM)
            .filter(dom -> dom != null && dom.getTextContent() != null)
            .map(dom -> "Hello-" + dom.getTextContent().trim())
            .forEach(attributeValue -> samlAttribute.getAttributeValues().add(attributeValue))
    return samlAttribute
}
