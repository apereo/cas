package org.apereo.cas.support.saml.web.idp.profile.builders;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.saml.SamlException;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.support.saml.util.AbstractSaml20ObjectBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.SamlObjectSigner;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Statement;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.SecureRandom;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link SamlProfileSamlAssertionBuilder}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class SamlProfileSamlAssertionBuilder extends AbstractSaml20ObjectBuilder implements SamlProfileObjectBuilder<Assertion> {
    private static final long serialVersionUID = -3945938960014421135L;

    @Autowired
    private CasConfigurationProperties casProperties;
    
    private SamlProfileSamlAuthNStatementBuilder samlProfileSamlAuthNStatementBuilder;
    
    private SamlProfileSamlAttributeStatementBuilder samlProfileSamlAttributeStatementBuilder;
    
    private SamlProfileSamlSubjectBuilder samlProfileSamlSubjectBuilder;
    
    private SamlProfileSamlConditionsBuilder samlProfileSamlConditionsBuilder;

    private SamlObjectSigner samlObjectSigner;


    @Override
    public Assertion build(final AuthnRequest authnRequest, final HttpServletRequest request, final HttpServletResponse response,
                           final org.jasig.cas.client.validation.Assertion casAssertion, final SamlRegisteredService service,
                           final SamlRegisteredServiceServiceProviderMetadataFacade adaptor) throws SamlException {

        final List<Statement> statements = new ArrayList<>();
        statements.add(this.samlProfileSamlAuthNStatementBuilder.build(authnRequest, request, response, casAssertion, service, adaptor));
        statements.add(this.samlProfileSamlAttributeStatementBuilder.build(authnRequest, 
                request, response, casAssertion, service, adaptor));

        final String id = "_" + String.valueOf(Math.abs(new SecureRandom().nextLong()));
        final Assertion assertion = newAssertion(statements, casProperties.getAuthn().getSamlIdp().getEntityId(), 
                                                ZonedDateTime.now(ZoneOffset.UTC), id);
        assertion.setSubject(this.samlProfileSamlSubjectBuilder.build(authnRequest, request, response, casAssertion, service, adaptor));
        assertion.setConditions(this.samlProfileSamlConditionsBuilder.build(authnRequest, 
                request, response, casAssertion, service, adaptor));

        signAssertion(assertion, request, response, service, adaptor);
        return assertion;
    }

    /**
     * Sign assertion.
     *
     * @param assertion the assertion
     * @param request   the request
     * @param response  the response
     * @param service   the service
     * @param adaptor   the adaptor
     * @throws SamlException the saml exception
     */
    protected void signAssertion(final Assertion assertion,
                                 final HttpServletRequest request, final HttpServletResponse response,
                                 final SamlRegisteredService service,
                                 final SamlRegisteredServiceServiceProviderMetadataFacade adaptor) throws SamlException {
        try {
            if (service.isSignAssertions()) {
                logger.debug("SAML registered service [{}] requires assertions to be signed", adaptor.getEntityId());
                this.samlObjectSigner.encode(assertion, service, adaptor, response, request);
            } else {
                logger.debug("SAML registered service [{}] does not require assertions to be signed", adaptor.getEntityId());
            }
        } catch (final Exception e) {
            throw new SamlException("Unable to marshall assertion for signing", e);
        }
    }

    public void setSamlProfileSamlAuthNStatementBuilder(final SamlProfileSamlAuthNStatementBuilder samlProfileSamlAuthNStatementBuilder) {
        this.samlProfileSamlAuthNStatementBuilder = samlProfileSamlAuthNStatementBuilder;
    }

    public void setSamlProfileSamlAttributeStatementBuilder(final SamlProfileSamlAttributeStatementBuilder samlProfileSamlAttributeStatementBuilder) {
        this.samlProfileSamlAttributeStatementBuilder = samlProfileSamlAttributeStatementBuilder;
    }

    public void setSamlProfileSamlSubjectBuilder(final SamlProfileSamlSubjectBuilder samlProfileSamlSubjectBuilder) {
        this.samlProfileSamlSubjectBuilder = samlProfileSamlSubjectBuilder;
    }

    public void setSamlProfileSamlConditionsBuilder(final SamlProfileSamlConditionsBuilder samlProfileSamlConditionsBuilder) {
        this.samlProfileSamlConditionsBuilder = samlProfileSamlConditionsBuilder;
    }

    public void setSamlObjectSigner(final SamlObjectSigner samlObjectSigner) {
        this.samlObjectSigner = samlObjectSigner;
    }
}
