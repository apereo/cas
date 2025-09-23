package org.apereo.cas.support.saml.web.idp.profile.builders.authn;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.SamlException;
import org.apereo.cas.support.saml.SamlIdPUtils;
import org.apereo.cas.support.saml.SamlUtils;
import org.apereo.cas.support.saml.util.AbstractSaml20ObjectBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileBuilderContext;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileObjectBuilder;
import org.apereo.cas.util.DateTimeUtils;
import org.apereo.cas.util.RandomUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.opensaml.saml.saml2.core.AuthnContext;
import org.opensaml.saml.saml2.core.AuthnStatement;
import org.opensaml.saml.saml2.core.SubjectLocality;

import java.time.Instant;
import java.util.Optional;

/**
 * This is {@link SamlProfileSamlAuthNStatementBuilder}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
public class SamlProfileSamlAuthNStatementBuilder extends AbstractSaml20ObjectBuilder implements SamlProfileObjectBuilder<AuthnStatement> {
    
    private final SamlProfileObjectBuilder<AuthnContext> authnContextClassRefBuilder;

    private final CasConfigurationProperties casProperties;

    public SamlProfileSamlAuthNStatementBuilder(final OpenSamlConfigBean configBean,
                                                final SamlProfileObjectBuilder<AuthnContext> authnContextClassRefBuilder,
                                                final CasConfigurationProperties casProperties) {
        super(configBean);
        this.authnContextClassRefBuilder = authnContextClassRefBuilder;
        this.casProperties = casProperties;
    }

    @Override
    public AuthnStatement build(final SamlProfileBuilderContext context) throws Exception {
        return buildAuthnStatement(context);
    }

    protected SubjectLocality buildSubjectLocality(final SamlProfileBuilderContext context) throws SamlException {
        val subjectLocality = SamlUtils.newSamlObject(SubjectLocality.class);
        val issuer = SamlIdPUtils.getIssuerFromSamlObject(context.getSamlRequest());
        val clientRemoteIpAddr = Optional.ofNullable(ClientInfoHolder.getClientInfo())
            .map(ClientInfo::getClientIpAddress)
            .orElse(StringUtils.EMPTY);
        val hostAddress = StringUtils.defaultIfBlank(context.getRegisteredService().getSubjectLocality(), clientRemoteIpAddr);
        LOGGER.debug("Built SAML2 subject locality address [{}] for [{}]", hostAddress, issuer);
        subjectLocality.setAddress(hostAddress);
        return subjectLocality;
    }

    protected AuthnStatement buildAuthnStatement(final SamlProfileBuilderContext context) throws Exception {
        
        val id = buildAuthnStatementSessionIdex(context);
        val authnInstant = DateTimeUtils.zonedDateTimeOf(context.getAuthenticatedAssertion().orElseThrow().getAuthenticationDate());

        val authnContextClass = authnContextClassRefBuilder.build(context);
        val statement = newAuthnStatement(authnContextClass, authnInstant, id);

        if (!context.getRegisteredService().isSkipGeneratingSessionNotOnOrAfter()) {
            statement.setSessionNotOnOrAfter(buildSessionNotOnOrAfter(context));
        }

        val subjectLocality = buildSubjectLocality(context);
        if (subjectLocality != null) {
            statement.setSubjectLocality(subjectLocality);
        }
        return statement;
    }

    private static String buildAuthnStatementSessionIdex(final SamlProfileBuilderContext context) {
        var id = context.getSessionIndex();
        if (StringUtils.isBlank(id)) {
            LOGGER.info("Unable to locate service ticket as the session index; Generating random identifier instead...");
            id = '_' + String.valueOf(RandomUtils.nextLong());
        }
        return id;
    }

    protected Instant buildSessionNotOnOrAfter(final SamlProfileBuilderContext context) {
        val dt = DateTimeUtils.zonedDateTimeOf(context.getAuthenticatedAssertion().orElseThrow().getValidUntilDate());
        val skewAllowance = context.getRegisteredService().getSkewAllowance() != 0
            ? context.getRegisteredService().getSkewAllowance()
            : Beans.newDuration(casProperties.getAuthn().getSamlIdp().getResponse().getSkewAllowance()).toSeconds();
        return dt.plusSeconds(skewAllowance).toInstant();
    }
}
