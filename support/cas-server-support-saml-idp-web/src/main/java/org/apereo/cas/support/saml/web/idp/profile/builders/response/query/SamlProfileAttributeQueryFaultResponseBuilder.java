package org.apereo.cas.support.saml.web.idp.profile.builders.response.query;

import org.apereo.cas.support.saml.web.idp.profile.builders.response.SamlProfileSamlResponseBuilderConfigurationContext;
import org.apereo.cas.support.saml.web.idp.profile.builders.response.soap.SamlProfileSamlSoap11FaultResponseBuilder;

/**
 * This is {@link SamlProfileAttributeQueryFaultResponseBuilder}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class SamlProfileAttributeQueryFaultResponseBuilder extends SamlProfileSamlSoap11FaultResponseBuilder {
    private static final long serialVersionUID = -5582616946993706815L;

    public SamlProfileAttributeQueryFaultResponseBuilder(final SamlProfileSamlResponseBuilderConfigurationContext samlResponseBuilderConfigurationContext) {
        super(samlResponseBuilderConfigurationContext);
    }
}
