package org.jasig.cas.support.saml.web.idp.profile.builders;

import org.jasig.cas.support.saml.SamlException;
import org.jasig.cas.support.saml.services.SamlRegisteredService;
import org.jasig.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.jasig.cas.support.saml.util.AbstractSaml20ObjectBuilder;
import org.jasig.cas.support.saml.web.idp.profile.builders.enc.SamlObjectSigner;

import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Statement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

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
@RefreshScope
@Component("samlProfileSamlAssertionBuilder")
public class SamlProfileSamlAssertionBuilder extends AbstractSaml20ObjectBuilder implements SamlProfileObjectBuilder<Assertion> {
    private static final long serialVersionUID = -3945938960014421135L;

    @Value("${cas.samlidp.entityid:}")
    private String entityId;

    @Autowired
    @Qualifier("samlProfileSamlAuthNStatementBuilder")
    private SamlProfileSamlAuthNStatementBuilder samlProfileSamlAuthNStatementBuilder;

    @Autowired
    @Qualifier("samlProfileSamlAttributeStatementBuilder")
    private SamlProfileSamlAttributeStatementBuilder samlProfileSamlAttributeStatementBuilder;

    @Autowired
    @Qualifier("samlProfileSamlSubjectBuilder")
    private SamlProfileSamlSubjectBuilder samlProfileSamlSubjectBuilder;

    @Autowired
    @Qualifier("samlProfileSamlConditionsBuilder")
    private SamlProfileSamlConditionsBuilder samlProfileSamlConditionsBuilder;

    @Autowired
    @Qualifier("samlObjectSigner")
    private SamlObjectSigner samlObjectSigner;


    @Override
    public Assertion build(final AuthnRequest authnRequest, final HttpServletRequest request, final HttpServletResponse response,
                           final org.jasig.cas.client.validation.Assertion casAssertion, final SamlRegisteredService service,
                           final SamlRegisteredServiceServiceProviderMetadataFacade adaptor) throws SamlException {

        final List<Statement> statements = new ArrayList<>();
        statements.add(samlProfileSamlAuthNStatementBuilder.build(authnRequest, request, response, casAssertion, service, adaptor));
        statements.add(samlProfileSamlAttributeStatementBuilder.build(authnRequest, request, response, casAssertion, service, adaptor));

        final String id = String.valueOf(Math.abs(new SecureRandom().nextLong()));
        final Assertion assertion = newAssertion(statements, this.entityId, ZonedDateTime.now(ZoneOffset.UTC), id);
        assertion.setSubject(samlProfileSamlSubjectBuilder.build(authnRequest, request, response, casAssertion, service, adaptor));
        assertion.setConditions(samlProfileSamlConditionsBuilder.build(authnRequest, request, response, casAssertion, service, adaptor));

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
                logger.info("SAML registered service [{}] requires assertions to be signed", adaptor.getEntityId());
                this.samlObjectSigner.encode(assertion, service, adaptor, response, request);
            } else {
                logger.info("SAML registered service [{}] does not require assertions to be signed", adaptor.getEntityId());
            }
        } catch (final Exception e) {
            throw new SamlException("Unable to marshall assertion for signing", e);
        }
    }

}
