package org.apereo.cas.support.saml.web.idp.profile.builders.authn;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.idp.metadata.locator.SamlIdPSamlRegisteredServiceCriterion;
import org.apereo.cas.support.saml.util.AbstractSaml20ObjectBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileBuilderContext;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileObjectBuilder;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.spring.ApplicationContextProvider;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.shibboleth.shared.resolver.CriteriaSet;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.opensaml.saml.metadata.criteria.entity.impl.EvaluableEntityRoleEntityDescriptorCriterion;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.saml2.core.AuthenticatingAuthority;
import org.opensaml.saml.saml2.core.AuthnContext;
import org.opensaml.saml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.RequestedAuthnContext;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * This is {@link SamlProfileAuthnContextClassRefBuilder}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j

public class SamlProfileAuthnContextClassRefBuilder extends AbstractSaml20ObjectBuilder implements SamlProfileObjectBuilder<AuthnContext> {

    @Serial
    private static final long serialVersionUID = 5783371664834470257L;

    private final MetadataResolver samlIdPMetadataResolver;

    private final CasConfigurationProperties casProperties;

    public SamlProfileAuthnContextClassRefBuilder(final OpenSamlConfigBean configBean,
                                                  final MetadataResolver samlIdPMetadataResolver,
                                                  final CasConfigurationProperties casProperties) {
        super(configBean);
        this.samlIdPMetadataResolver = samlIdPMetadataResolver;
        this.casProperties = casProperties;
    }

    @Override
    public AuthnContext build(final SamlProfileBuilderContext context) throws Exception {
        val classRefValue = buildAuthnContextClassRefValue(context);

        val authnContext = newSamlObject(AuthnContext.class);

        val classRef = newSamlObject(AuthnContextClassRef.class);
        classRef.setURI(classRefValue);
        authnContext.setAuthnContextClassRef(classRef);

        buildDefaultAuthenticatingAuthority(context, authnContext);

        return authnContext;
    }

    protected void buildDefaultAuthenticatingAuthority(final SamlProfileBuilderContext context,
                                                       final AuthnContext authnContext) throws Exception {
        val entityIdCriteriaSet = new CriteriaSet(
            new EvaluableEntityRoleEntityDescriptorCriterion(IDPSSODescriptor.DEFAULT_ELEMENT_NAME),
            new SamlIdPSamlRegisteredServiceCriterion(context.getRegisteredService()));
        LOGGER.trace("Resolving entity id from SAML2 IdP metadata for signature signing configuration is [{}]",
            context.getRegisteredService().getName());
        val entityId = Objects.requireNonNull(samlIdPMetadataResolver.resolveSingle(entityIdCriteriaSet)).getEntityID();
        LOGGER.trace("Resolved entity id from SAML2 IdP metadata is [{}]", entityId);
        val authority = newSamlObject(AuthenticatingAuthority.class);
        authority.setURI(entityId);
        authnContext.getAuthenticatingAuthorities().add(authority);
    }

    private String buildAuthnContextClassRefValue(final SamlProfileBuilderContext context) {
        val requiredClass = SpringExpressionLanguageValueResolver.getInstance()
            .resolve(context.getRegisteredService().getRequiredAuthenticationContextClass());
        if (StringUtils.isNotBlank(requiredClass)) {
            LOGGER.debug("Using [{}] as indicated by SAML registered service [{}]",
                requiredClass, context.getRegisteredService().getName());
            if (!ResourceUtils.isUrl(requiredClass) && ResourceUtils.doesResourceExist(requiredClass)) {
                LOGGER.debug("Executing groovy script [{}] to determine authentication context class for [{}]",
                    requiredClass, context.getAdaptor().getEntityId());
                return ApplicationContextProvider.getScriptResourceCacheManager()
                    .map(cacheMgr -> {
                        val script = cacheMgr.resolveScriptableResource(requiredClass,
                            requiredClass, context.getAdaptor().getEntityId());
                        return FunctionUtils.doIfNotNull(script, () -> {
                            val args = CollectionUtils.wrap("context", context, "logger", LOGGER);
                            script.setBinding(args);
                            return script.execute(args.values().toArray(), String.class, true);
                        }, () -> null).get();
                    })
                    .orElseThrow(() -> new RuntimeException("Unable to locate script cache manager"));
            }
            return requiredClass;
        }

        val defClass = getDefaultAuthenticationContextClass();
        val requestedAuthnContext = context.getSamlRequest() instanceof AuthnRequest
            ? AuthnRequest.class.cast(context.getSamlRequest()).getRequestedAuthnContext() : null;
        if (requestedAuthnContext == null) {
            LOGGER.debug("No specific authN context is requested. Returning [{}]", defClass);
            return buildDefaultAuthenticationContextClass(defClass, context);
        }
        val authnContextClassRefs = requestedAuthnContext.getAuthnContextClassRefs();
        if (authnContextClassRefs == null || authnContextClassRefs.isEmpty()) {
            LOGGER.debug("Requested authN context class ref is unspecified. Returning [{}]", defClass);
            return buildDefaultAuthenticationContextClass(defClass, context);
        }

        val contextInAssertion = getAuthenticationContextByAssertion(context,
            requestedAuthnContext, authnContextClassRefs);
        val finalCtx = StringUtils.defaultIfBlank(contextInAssertion, defClass);
        LOGGER.debug("Returning authentication context [{}]", finalCtx);
        return finalCtx;
    }

    protected String buildDefaultAuthenticationContextClass(final String defClass,
                                                            final SamlProfileBuilderContext context) {
        val contextValues = CollectionUtils.toCollection(context.getAuthenticatedAssertion()
            .get().getAttributes().get(casProperties.getAuthn().getMfa().getCore().getAuthenticationContextAttribute()));
        val definedContexts = CollectionUtils.convertDirectedListToMap(
            casProperties.getAuthn().getSamlIdp().getCore().getAuthenticationContextClassMappings());
        return definedContexts.entrySet()
            .stream()
            .filter(entry -> contextValues.contains(entry.getValue()))
            .map(Map.Entry::getKey)
            .findFirst()
            .orElse(defClass);
    }

    protected String getDefaultAuthenticationContextClass() {
        return StringUtils.defaultIfBlank(
            casProperties.getAuthn().getSamlIdp().getResponse().getDefaultAuthenticationContextClass(),
            AuthnContext.PPT_AUTHN_CTX);
    }

    protected String getAuthenticationContextByAssertion(final SamlProfileBuilderContext context,
                                                         final RequestedAuthnContext requestedAuthnContext,
                                                         final List<AuthnContextClassRef> authnContextClassRefs) {
        LOGGER.debug("AuthN context comparison to use [{}]", requestedAuthnContext.getComparison());
        authnContextClassRefs.forEach(ref -> LOGGER.debug("Requested AuthN Context [{}]", ref.getURI()));

        val definedContexts = CollectionUtils.convertDirectedListToMap(
            casProperties.getAuthn().getSamlIdp().getCore().getAuthenticationContextClassMappings());
        LOGGER.debug("Defined authentication context mappings are [{}]", definedContexts);

        return authnContextClassRefs.stream()
            .filter(ref -> StringUtils.isNotBlank(ref.getURI()))
            .filter(ref -> definedContexts.containsKey(ref.getURI()))
            .map(ref -> Pair.of(ref, definedContexts.get(ref.getURI())))
            .findFirst()
            .map(mappedMethod -> getMappedAuthenticationContextClass(context, mappedMethod))
            .orElse(StringUtils.EMPTY);
    }

    private String getMappedAuthenticationContextClass(final SamlProfileBuilderContext context,
                                                       final Pair<AuthnContextClassRef, String> mappedMethod) {

        val requestedMappedValues = new ArrayList<>(org.springframework.util.StringUtils.commaDelimitedListToSet(mappedMethod.getValue()));
        val attributes = context.getAuthenticatedAssertion().get().getAttributes();
        val contextAttributes = org.springframework.util.StringUtils.commaDelimitedListToSet(
            casProperties.getAuthn().getMfa().getCore().getAuthenticationContextAttribute());
        for (val contextAttribute : contextAttributes) {
            LOGGER.debug("Checking for mapped authentication context method [{}] in attributes [{}] via [{}]",
                requestedMappedValues, attributes, contextAttribute);
            if (attributes.containsKey(contextAttribute)) {
                val authnContext = attributes.get(contextAttribute);
                val satisfiedContext = CollectionUtils.firstElement(authnContext)
                    .map(Object::toString)
                    .orElse(StringUtils.EMPTY);
                LOGGER.debug("Comparing satisfied authentication context [{}] against [{}]", satisfiedContext, mappedMethod.getValue());
                if (requestedMappedValues.contains(satisfiedContext)) {
                    return mappedMethod.getLeft().getURI();
                }
            }
        }
        return null;
    }
}
