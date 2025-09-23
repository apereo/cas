package org.apereo.cas.support.saml.web.idp.profile.builders.authn;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.idp.metadata.locator.SamlIdPSamlRegisteredServiceCriterion;
import org.apereo.cas.support.saml.util.AbstractSaml20ObjectBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileBuilderContext;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileObjectBuilder;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.scripting.ExecutableCompiledScriptFactory;
import org.apereo.cas.util.scripting.ScriptResourceCacheManager;
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
import org.springframework.beans.factory.ObjectProvider;
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

    private final MetadataResolver samlIdPMetadataResolver;

    private final CasConfigurationProperties casProperties;

    private final ObjectProvider<ScriptResourceCacheManager> scriptResourceCacheManager;

    public SamlProfileAuthnContextClassRefBuilder(final OpenSamlConfigBean configBean,
                                                  final MetadataResolver samlIdPMetadataResolver,
                                                  final CasConfigurationProperties casProperties,
                                                  final ObjectProvider<ScriptResourceCacheManager> scriptResourceCacheManager) {
        super(configBean);
        this.samlIdPMetadataResolver = samlIdPMetadataResolver;
        this.casProperties = casProperties;
        this.scriptResourceCacheManager = scriptResourceCacheManager;
    }

    @Override
    public AuthnContext build(final SamlProfileBuilderContext context) throws Exception {
        val classRefValue = buildAuthnContextClassRefValue(context);
        val authnContext = newSamlObject(AuthnContext.class);
        if (StringUtils.isNotBlank(classRefValue)) {
            val classRef = newSamlObject(AuthnContextClassRef.class);
            classRef.setURI(classRefValue);
            authnContext.setAuthnContextClassRef(classRef);
        }
        buildDefaultAuthenticatingAuthority(context, authnContext);
        return authnContext;
    }

    protected void buildDefaultAuthenticatingAuthority(final SamlProfileBuilderContext context,
                                                       final AuthnContext authnContext) throws Exception {
        if (!context.getRegisteredService().isSkipGeneratingAuthenticatingAuthority()) {
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
    }

    private String buildAuthnContextClassRefValue(final SamlProfileBuilderContext context) {
        val assignedContextClass = context.getRegisteredService().getRequiredAuthenticationContextClass();
        if (StringUtils.isNotBlank(assignedContextClass)) {
            LOGGER.debug("Using [{}] as indicated by SAML registered service [{}]",
                assignedContextClass, context.getRegisteredService().getName());
            val scriptFactory = ExecutableCompiledScriptFactory.findExecutableCompiledScriptFactory();
            if (scriptFactory.isPresent() && scriptFactory.get().isScript(assignedContextClass)) {
                val requiredClass = scriptFactory.get().isExternalScript(assignedContextClass)
                    ? SpringExpressionLanguageValueResolver.getInstance().resolve(assignedContextClass)
                    : assignedContextClass;
                return buildScriptedAuthnContextClassRef(context, requiredClass);
            }
            return SpringExpressionLanguageValueResolver.getInstance().resolve(assignedContextClass);
        }

        val defClass = getDefaultAuthenticationContextClass();
        val requestedAuthnContext = context.getSamlRequest() instanceof final AuthnRequest authnRequest
            ? authnRequest.getRequestedAuthnContext() : null;
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

    protected String buildScriptedAuthnContextClassRef(final SamlProfileBuilderContext context, final String requiredClass) {
        LOGGER.debug("Executing groovy script [{}] to determine authentication context class for [{}]",
            requiredClass, context.getAdaptor().getEntityId());
        return scriptResourceCacheManager
            .stream()
            .map(cacheMgr -> {
                val script = cacheMgr.resolveScriptableResource(requiredClass,
                    requiredClass, context.getAdaptor().getEntityId());
                return FunctionUtils.doIfNotNull(script,
                    () -> {
                        val args = CollectionUtils.wrap("context", context, "logger", LOGGER);
                        script.setBinding(args);
                        return script.execute(args.values().toArray(), String.class, true);
                    },
                    () -> requiredClass).get();
            })
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Unable to locate script cache manager or execute groovy script"));
    }

    protected String buildDefaultAuthenticationContextClass(final String defClass,
                                                            final SamlProfileBuilderContext context) {
        val contextValues = CollectionUtils.toCollection(context.getAuthenticatedAssertion()
            .orElseThrow().getAttributes().get(casProperties.getAuthn().getMfa().getCore().getAuthenticationContextAttribute()));
        val definedContexts = CollectionUtils.convertDirectedListToMap(
            casProperties.getAuthn().getSamlIdp().getCore().getContext().getAuthenticationContextClassMappings());
        return definedContexts.entrySet()
            .stream()
            .filter(entry -> contextValues.contains(entry.getValue()))
            .map(Map.Entry::getKey)
            .findFirst()
            .orElse(defClass);
    }

    protected String getDefaultAuthenticationContextClass() {
        return StringUtils.defaultIfBlank(
            casProperties.getAuthn().getSamlIdp().getCore().getContext().getDefaultAuthenticationContextClass(),
            AuthnContext.PPT_AUTHN_CTX);
    }

    protected String getAuthenticationContextByAssertion(final SamlProfileBuilderContext context,
                                                         final RequestedAuthnContext requestedAuthnContext,
                                                         final List<AuthnContextClassRef> authnContextClassRefs) {
        LOGGER.debug("AuthN context comparison to use [{}]", requestedAuthnContext.getComparison());
        authnContextClassRefs.forEach(ref -> LOGGER.debug("Requested AuthN Context [{}]", ref.getURI()));

        val definedContexts = CollectionUtils.convertDirectedListToMap(
            casProperties.getAuthn().getSamlIdp().getCore().getContext().getAuthenticationContextClassMappings());
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
        val attributes = context.getAuthenticatedAssertion().orElseThrow().getAttributes();
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
