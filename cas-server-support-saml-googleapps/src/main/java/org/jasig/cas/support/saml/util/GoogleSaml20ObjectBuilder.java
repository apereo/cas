package org.jasig.cas.support.saml.util;

import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.Status;
import org.opensaml.saml.saml2.core.StatusCode;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import java.lang.reflect.Field;

/**
 * This is {@link org.jasig.cas.support.saml.util.GoogleSaml20ObjectBuilder} that
 * attempts to build the saml response. QName based on the spec described here:
 * https://developers.google.com/google-apps/sso/saml_reference_implementation_web#samlReferenceImplementationWebSetupChangeDomain
 * @author Misagh Moayyed mmoayyed@unicon.net
 * @since 4.1.0
 */
public class GoogleSaml20ObjectBuilder extends AbstractSaml20ObjectBuilder {
    private static final long serialVersionUID = 2979638064754730668L;

    @Override
    public final QName getSamlObjectQName(final Class objectType) throws RuntimeException {
        try {
            final Field f = objectType.getField(DEFAULT_ELEMENT_LOCAL_NAME_FIELD);
            final String name = f.get(null).toString();

            if (objectType.equals(Response.class) || objectType.equals(Status.class)
                    || objectType.equals(StatusCode.class)) {
                return new QName(SAMLConstants.SAML20P_NS, name, "samlp");
            }
            return new QName(SAMLConstants.SAML20_NS, name, XMLConstants.DEFAULT_NS_PREFIX);
        } catch (final Exception e){
            throw new IllegalStateException("Cannot access field " + objectType.getName() + '.' + DEFAULT_ELEMENT_LOCAL_NAME_FIELD);
        }
    }
}
