package org.apereo.cas.support.saml.web.idp.profile.builders.authn;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileBuilderContext;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.spring.ApplicationContextProvider;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.opensaml.saml.saml2.core.AuthnContext;
import org.opensaml.saml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.RequestedAuthnContext;

import java.util.List;
import java.util.Map;

/**
 * This is {@link DefaultSamlProfileAuthnContextClassRefBuilder}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class DefaultSamlProfileAuthnContextClassRefBuilder implements SamlProfileAuthnContextClassRefBuilder {

    private final CasConfigurationProperties casProperties;

    @Override
    public String build(final SamlProfileBuilderContext context) throws Exception {
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
                        }, () -> null)
                            .get();
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
        LOGGER.debug("Returning authN context [{}]", finalCtx);
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
        LOGGER.debug("AuthN Context comparison is requested to use [{}]", requestedAuthnContext.getComparison());
        authnContextClassRefs.forEach(ref -> LOGGER.debug("Requested AuthN Context [{}]", ref.getURI()));

        val definedContexts = CollectionUtils.convertDirectedListToMap(
            casProperties.getAuthn().getSamlIdp().getCore().getAuthenticationContextClassMappings());
        LOGGER.debug("Defined authentication context mappings are [{}]", definedContexts);

        val mappedMethod = authnContextClassRefs.stream()
            .filter(ref -> StringUtils.isNotBlank(ref.getURI()))
            .filter(ref -> definedContexts.containsKey(ref.getURI()))
            .map(ref -> Pair.of(ref, definedContexts.get(ref.getURI())))
            .findFirst()
            .orElse(null);

        return getMappedAuthenticationContextClass(context, mappedMethod);
    }

    private String getMappedAuthenticationContextClass(final SamlProfileBuilderContext context,
                                                       final Pair<AuthnContextClassRef, String> mappedMethod) {
        val attributes = context.getAuthenticatedAssertion().get().getAttributes();
        val contextAttribute = casProperties.getAuthn().getMfa().getCore().getAuthenticationContextAttribute();
        LOGGER.debug("Checking for mapped authentication context method [{}] in attributes [{}] via [{}]",
            mappedMethod, attributes, contextAttribute);
        if (attributes.containsKey(contextAttribute) && mappedMethod != null) {
            val authnContext = attributes.get(contextAttribute);
            val satisfiedContext = CollectionUtils.firstElement(authnContext)
                .map(Object::toString)
                .orElse(null);
            LOGGER.debug("Comparig satisfied authentication context [{}] against [{}]", satisfiedContext, mappedMethod.getValue());
            if (StringUtils.equals(mappedMethod.getValue(), satisfiedContext)) {
                return mappedMethod.getLeft().getURI();
            }
        }
        return null;
    }
}
