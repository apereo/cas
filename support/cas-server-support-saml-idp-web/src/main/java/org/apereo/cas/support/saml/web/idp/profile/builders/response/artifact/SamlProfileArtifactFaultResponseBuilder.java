package org.apereo.cas.support.saml.web.idp.profile.builders.response.artifact;

import org.apereo.cas.support.saml.web.idp.profile.builders.response.SamlProfileSamlResponseBuilderConfigurationContext;
import org.apereo.cas.support.saml.web.idp.profile.builders.response.soap.SamlProfileSamlSoap11FaultResponseBuilder;

import java.io.Serial;

/**
 * This is {@link SamlProfileArtifactFaultResponseBuilder}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class SamlProfileArtifactFaultResponseBuilder extends SamlProfileSamlSoap11FaultResponseBuilder {
    @Serial
    private static final long serialVersionUID = -5582616946993706815L;

    public SamlProfileArtifactFaultResponseBuilder(final SamlProfileSamlResponseBuilderConfigurationContext configurationContext) {
        super(configurationContext);
    }
}
