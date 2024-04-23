package org.apereo.cas.support.saml.web.idp.profile.builders.enc.encoder.sso;

import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceMetadataAdaptor;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.encoder.BaseHttpServletAwareSamlObjectEncoder;

import lombok.val;
import org.apache.velocity.app.VelocityEngine;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.binding.encoding.impl.BaseSAML2MessageEncoder;
import org.opensaml.saml.saml2.binding.encoding.impl.HTTPPostSimpleSignEncoder;
import org.opensaml.saml.saml2.core.Response;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This is {@link SamlResponsePostSimpleSignEncoder}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class SamlResponsePostSimpleSignEncoder extends BaseHttpServletAwareSamlObjectEncoder<Response> {

    public SamlResponsePostSimpleSignEncoder(final VelocityEngine velocityEngineFactory,
                                             final SamlRegisteredServiceMetadataAdaptor adaptor,
                                             final HttpServletResponse httpResponse,
                                             final HttpServletRequest httpRequest) {
        super(velocityEngineFactory, adaptor, httpResponse, httpRequest);
    }

    @Override
    protected String getBinding() {
        return SAMLConstants.SAML2_POST_SIMPLE_SIGN_BINDING_URI;
    }

    @Override
    protected BaseSAML2MessageEncoder getMessageEncoderInstance() {
        val encoder = new HTTPPostSimpleSignEncoder();
        encoder.setVelocityEngine(this.velocityEngineFactory);
        return encoder;
    }
}
