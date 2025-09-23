package org.apereo.cas.support.saml.util;

import org.apereo.cas.support.saml.SamlUtils;

import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.LogoutRequest;
import org.opensaml.saml.saml2.core.LogoutResponse;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.Status;

import javax.xml.namespace.QName;
import java.time.ZonedDateTime;

/**
 * This is {@link Saml20ObjectBuilder}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public interface Saml20ObjectBuilder {

    /**
     * New saml object.
     *
     * @param <T>        the type parameter
     * @param objectType the name id class
     * @return the t
     */
    <T extends SAMLObject> T newSamlObject(Class<T> objectType);

    /**
     * Gets saml object Q name.
     *
     * @param objectType the object type
     * @return the saml object q name
     */
    default QName getSamlObjectQName(final Class objectType) {
        return SamlUtils.getSamlObjectQName(objectType);
    }

    /**
     * New status.
     *
     * @param codeValue     the code value
     * @param statusMessage the status message
     * @return the status
     */
    Status newStatus(String codeValue, String statusMessage);

    /**
     * New issuer.
     *
     * @param issuerValue the issuer value
     * @return the issuer
     */
    Issuer newIssuer(String issuerValue);

    /**
     * New logout response.
     *
     * @param id          the id
     * @param destination the destination
     * @param issuer      the issuer
     * @param status      the status
     * @param recipient   the recipient
     * @return the logout response
     */
    LogoutResponse newLogoutResponse(String id, String destination, Issuer issuer,
                                     Status status, String recipient);

    /**
     * New logout request.
     *
     * @param id           the id
     * @param issueInstant the issue instant
     * @param destination  the destination
     * @param issuer       the issuer
     * @param sessionIndex the session index
     * @param nameId       the name id
     * @return the logout request
     */
    LogoutRequest newLogoutRequest(String id, ZonedDateTime issueInstant,
                                   String destination, Issuer issuer,
                                   String sessionIndex, NameID nameId);

    /**
     * New name id.
     *
     * @param nameIdFormat the name id format
     * @param nameIdValue  the name id value
     * @return the name id
     */
    NameID newNameID(String nameIdFormat, String nameIdValue);
}
