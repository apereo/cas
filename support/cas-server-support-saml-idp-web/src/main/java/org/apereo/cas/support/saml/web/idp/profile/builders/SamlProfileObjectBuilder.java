package org.apereo.cas.support.saml.web.idp.profile.builders;

import module java.base;
import org.apereo.cas.support.saml.util.Saml20ObjectBuilder;
import org.opensaml.core.xml.XMLObject;

/**
 * The {@link SamlProfileObjectBuilder} defines the operations
 * required for building the saml response for an RP.
 *
 * @author Misagh Moayyed
 * @param <T> the type parameter
 * @since 5.0.0
 */
public interface SamlProfileObjectBuilder<T extends XMLObject> extends Saml20ObjectBuilder {

    /**
     * Build.
     *
     * @param context the context
     * @return the response
     * @throws Exception the exception
     */
    T build(SamlProfileBuilderContext context) throws Exception;
}
