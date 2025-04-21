package org.apereo.cas.support.saml.web.idp.profile.builders.assertion;

import org.apereo.cas.configuration.support.TriStateBoolean;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.SamlIdPConstants;
import org.apereo.cas.support.saml.SamlIdPUtils;
import org.apereo.cas.support.saml.idp.metadata.locator.SamlIdPSamlRegisteredServiceCriterion;
import org.apereo.cas.support.saml.services.idp.metadata.MetadataEntityAttributeQuery;
import org.apereo.cas.support.saml.util.AbstractSaml20ObjectBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileBuilderContext;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileObjectBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.SamlIdPObjectSigner;
import org.apereo.cas.support.saml.web.idp.profile.builders.response.SamlIdPResponseCustomizer;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.shibboleth.shared.resolver.CriteriaSet;
import org.apache.commons.lang3.StringUtils;
import org.jooq.lambda.Unchecked;
import org.opensaml.saml.metadata.criteria.entity.impl.EvaluableEntityRoleEntityDescriptorCriterion;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeStatement;
import org.opensaml.saml.saml2.core.AuthnStatement;
import org.opensaml.saml.saml2.core.Conditions;
import org.opensaml.saml.saml2.core.Statement;
import org.opensaml.saml.saml2.core.Subject;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This is {@link SamlProfileSamlAssertionBuilder}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
public class SamlProfileSamlAssertionBuilder extends AbstractSaml20ObjectBuilder implements SamlProfileObjectBuilder<Assertion> {

    private final SamlProfileObjectBuilder<AuthnStatement> samlProfileSamlAuthNStatementBuilder;

    private final SamlProfileObjectBuilder<AttributeStatement> samlProfileSamlAttributeStatementBuilder;

    private final SamlProfileObjectBuilder<Subject> samlProfileSamlSubjectBuilder;

    private final SamlProfileObjectBuilder<Conditions> samlProfileSamlConditionsBuilder;

    private final SamlIdPObjectSigner samlObjectSigner;

    private final MetadataResolver samlIdPMetadataResolver;

    public SamlProfileSamlAssertionBuilder(
        final OpenSamlConfigBean configBean,
        final SamlProfileObjectBuilder<AuthnStatement> samlProfileSamlAuthNStatementBuilder,
        final SamlProfileObjectBuilder<AttributeStatement> samlProfileSamlAttributeStatementBuilder,
        final SamlProfileObjectBuilder<Subject> samlProfileSamlSubjectBuilder,
        final SamlProfileObjectBuilder<Conditions> samlProfileSamlConditionsBuilder,
        final SamlIdPObjectSigner samlObjectSigner,
        final MetadataResolver samlIdPMetadataResolver) {
        super(configBean);
        this.samlProfileSamlAuthNStatementBuilder = samlProfileSamlAuthNStatementBuilder;
        this.samlProfileSamlAttributeStatementBuilder = samlProfileSamlAttributeStatementBuilder;
        this.samlProfileSamlSubjectBuilder = samlProfileSamlSubjectBuilder;
        this.samlProfileSamlConditionsBuilder = samlProfileSamlConditionsBuilder;
        this.samlObjectSigner = samlObjectSigner;
        this.samlIdPMetadataResolver = samlIdPMetadataResolver;
    }

    @Override
    public Assertion build(final SamlProfileBuilderContext context) throws Exception {

        val statements = new ArrayList<Statement>();
        val authnStatement = samlProfileSamlAuthNStatementBuilder.build(context);
        statements.add(authnStatement);
        val attrStatement = samlProfileSamlAttributeStatementBuilder.build(context);

        if (!attrStatement.getAttributes().isEmpty() || !attrStatement.getEncryptedAttributes().isEmpty()) {
            statements.add(attrStatement);
        }

        val issuerId = FunctionUtils.doIf(StringUtils.isNotBlank(context.getRegisteredService().getIssuerEntityId()),
                context.getRegisteredService()::getIssuerEntityId,
                Unchecked.supplier(() -> {
                    val criteriaSet = new CriteriaSet(
                        new EvaluableEntityRoleEntityDescriptorCriterion(IDPSSODescriptor.DEFAULT_ELEMENT_NAME),
                        new SamlIdPSamlRegisteredServiceCriterion(context.getRegisteredService()));
                    LOGGER.trace("Resolving entity id from SAML2 IdP metadata to determine issuer for [{}]", context.getRegisteredService().getName());
                    val entityDescriptor = Objects.requireNonNull(samlIdPMetadataResolver.resolveSingle(criteriaSet));
                    return entityDescriptor.getEntityID();
                }))
            .get();

        val id = '_' + String.valueOf(RandomUtils.nextLong());
        val assertion = newAssertion(statements, issuerId, ZonedDateTime.now(ZoneOffset.UTC), id);
        assertion.setSubject(samlProfileSamlSubjectBuilder.build(context));
        assertion.setConditions(samlProfileSamlConditionsBuilder.build(context));

        val customizers = openSamlConfigBean.getApplicationContext()
            .getBeansOfType(SamlIdPResponseCustomizer.class).values();
        customizers.stream()
            .sorted(AnnotationAwareOrderComparator.INSTANCE)
            .forEach(customizer -> customizer.customizeAssertion(context, this, assertion));
        signAssertion(assertion, context);
        return assertion;
    }

    /**
     * Sign assertion.
     *
     * @param assertion the assertion
     * @param context   the context
     * @throws Exception the exception
     */
    protected void signAssertion(final Assertion assertion,
                                 final SamlProfileBuilderContext context) throws Exception {
        var signAssertions = (context.getRegisteredService().getSignAssertions() == TriStateBoolean.UNDEFINED && context.getAdaptor().isWantAssertionsSigned())
                             || context.getRegisteredService().getSignAssertions().isTrue();
        if (!signAssertions) {
            signAssertions = SamlIdPUtils.doesEntityDescriptorMatchEntityAttribute(context.getAdaptor().getEntityDescriptor(),
                List.of(MetadataEntityAttributeQuery.of(SamlIdPConstants.KnownEntityAttributes.SHIBBOLETH_SIGN_ASSERTIONS.getName(),
                    Attribute.URI_REFERENCE, List.of(Boolean.TRUE.toString()))));
        }
        
        if (signAssertions) {
            LOGGER.debug("SAML registered service [{}] requires assertions to be signed", context.getAdaptor().getEntityId());
            samlObjectSigner.encode(assertion, context.getRegisteredService(), context.getAdaptor(),
                context.getHttpResponse(), context.getHttpRequest(), context.getBinding(), context.getSamlRequest(),
                context.getMessageContext());
        } else {
            LOGGER.debug("SAML registered service [{}] does not require assertions to be signed", context.getAdaptor().getEntityId());
        }
    }
}
