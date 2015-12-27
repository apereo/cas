package org.jasig.cas.support.saml.web.idp.profile.builders;

import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.support.saml.SamlException;
import org.jasig.cas.support.saml.services.SamlRegisteredService;
import org.jasig.cas.support.saml.services.idp.metadata.SamlMetadataAdaptor;
import org.jasig.cas.support.saml.util.AbstractSaml20ObjectBuilder;
import org.joda.time.DateTime;
import org.opensaml.saml.common.SAMLVersion;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.Status;
import org.opensaml.saml.saml2.core.StatusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.SecureRandom;

/**
 * The {@link SamlProfileSamlResponseBuilder} is responsible for
 * building the final SAML assertion for the relying party.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@Component("samlProfileSamlResponseBuilder")
public class SamlProfileSamlResponseBuilder extends AbstractSaml20ObjectBuilder implements SamlProfileObjectBuilder<Response> {
    private static final long serialVersionUID = -1891703354216174875L;

    @Value("${cas.samlidp.entityid:}")
    private String entityId;

    @Autowired
    @Qualifier("samlProfileSamlAssertionBuilder")
    private SamlProfileSamlAssertionBuilder samlProfileSamlAssertionBuilder;

    @Override
    public Response build(final AuthnRequest authnRequest, final HttpServletRequest request,
                          final HttpServletResponse response, final Assertion casAssertion,
                          final SamlRegisteredService service, final SamlMetadataAdaptor adaptor) throws SamlException {
        final org.opensaml.saml.saml2.core.Assertion assertion =
                this.samlProfileSamlAssertionBuilder.build(authnRequest, request, response, casAssertion, service, adaptor);
        final Response finalResponse = buildResponse(assertion, authnRequest);
        return finalResponse;
    }

    /**
     * Build response response.
     *
     * @param assertion    the assertion
     * @param authnRequest the authn request
     * @return the response
     * @throws SamlException the saml exception
     */
    protected Response buildResponse(final org.opensaml.saml.saml2.core.Assertion assertion, final AuthnRequest authnRequest)
                throws SamlException {
        final String id = String.valueOf(Math.abs(new SecureRandom().nextLong()));
        final Response samlResponse = newResponse(id, new DateTime(), authnRequest.getID(), null);
        samlResponse.setVersion(SAMLVersion.VERSION_20);
        samlResponse.setIssuer(buildEntityIssuer());

        samlResponse.getAssertions().add(assertion);
        final Status status = newStatus(StatusCode.SUCCESS, StatusCode.SUCCESS);
        samlResponse.setStatus(status);
        return samlResponse;
    }

    /**
     * Build entity issuer issuer.
     *
     * @return the issuer
     */
    protected Issuer buildEntityIssuer() {
        final Issuer issuer = newIssuer(this.entityId);
        issuer.setFormat(Issuer.ENTITY);
        return issuer;
    }

}
