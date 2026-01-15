package org.apereo.cas.support.saml.idp;

import module java.base;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.SamlIdPConstants;
import org.apereo.cas.support.saml.SamlUtils;
import org.apereo.cas.support.saml.authentication.SamlIdPAuthenticationContext;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.web.support.ArgumentExtractor;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jooq.lambda.Unchecked;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.common.SignableSAMLObject;
import org.opensaml.saml.common.binding.SAMLBindingSupport;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.RequestAbstractType;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.jee.context.JEEContext;

/**
 * This is {@link SamlIdPSessionManager}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class SamlIdPSessionManager {
    private final OpenSamlConfigBean openSamlConfigBean;
    private final SessionStore sessionStore;

    /**
     * Build the saml idp session manager.
     *
     * @param openSamlConfigBean the open saml config bean
     * @param sessionStore       the session store
     * @return the saml id p session manager
     */
    public static SamlIdPSessionManager of(final OpenSamlConfigBean openSamlConfigBean,
                                           final SessionStore sessionStore) {
        return new SamlIdPSessionManager(openSamlConfigBean, sessionStore);
    }

    /**
     * Store saml request.
     *
     * @param webContext the web context
     * @param context    the context
     * @return the saml id p session manager
     * @throws Exception the exception
     */
    @CanIgnoreReturnValue
    public SamlIdPSessionManager store(final WebContext webContext,
                                       final Pair<? extends SignableSAMLObject, MessageContext> context) throws Exception {
        val authnRequest = (AuthnRequest) context.getLeft();
        val messageContext = context.getValue();
        try (val writer = SamlUtils.transformSamlObject(openSamlConfigBean, authnRequest)) {
            val samlRequest = EncodingUtils.encodeBase64(writer.toString().getBytes(StandardCharsets.UTF_8));
            val authnContext = SamlIdPAuthenticationContext.from(messageContext).encode();
            val entry = new SamlIdPSessionEntry()
                .setId(authnRequest.getID())
                .setSamlRequest(samlRequest)
                .setRelayState(SAMLBindingSupport.getRelayState(messageContext))
                .setContext(authnContext);
            val currentContext = sessionStore.get(webContext, SamlIdPSessionEntry.class.getName());
            val entries = currentContext.map(ctx -> (Map<String, SamlIdPSessionEntry>) ctx).orElseGet(HashMap::new);
            entries.put(entry.getId(), entry);
            sessionStore.set(webContext, SamlIdPSessionEntry.class.getName(), entries);
        }
        return this;
    }

    /**
     * Retrieve authn request authn request.
     *
     * @param context the context
     * @param clazz   the clazz
     * @return the request
     */
    public Optional<Pair<? extends RequestAbstractType, MessageContext>> fetch(
        final WebContext context, final Class<? extends RequestAbstractType> clazz) {
        LOGGER.trace("Attempting to fetch SAML2 authentication session from [{}]", context.getFullRequestURL());
        val currentContext = sessionStore.get(context, SamlIdPSessionEntry.class.getName());
        return currentContext.map(ctx -> (Map<String, SamlIdPSessionEntry>) ctx)
            .flatMap(ctx -> context.getRequestParameter(SamlIdPConstants.AUTHN_REQUEST_ID)
                .map(ctx::get)
                .or(Unchecked.supplier(() -> getSamlIdPSessionEntryFromRequest(context, ctx))))
            .filter(entry -> StringUtils.isNotBlank(entry.getSamlRequest()))
            .map(entry -> {
                val authnRequest = fetch(clazz, entry.getSamlRequest());
                val messageContext = SamlIdPAuthenticationContext.decode(entry.getContext()).toMessageContext(authnRequest);
                return Pair.of((AuthnRequest) messageContext.getMessage(), messageContext);
            });
    }

    /**
     * Retrieve saml request.
     *
     * @param <T>          the type parameter
     * @param clazz        the clazz
     * @param requestValue the request value
     * @return the t
     */
    public <T extends RequestAbstractType> T fetch(final Class<T> clazz, final String requestValue) {
        return SamlUtils.convertToSamlObject(openSamlConfigBean, requestValue, clazz);
    }

    private Optional<SamlIdPSessionEntry> getSamlIdPSessionEntryFromRequest(final WebContext context, final Map<String, SamlIdPSessionEntry> ctx) {
        val applicationContext = openSamlConfigBean.getApplicationContext();
        val argumentExtractor = applicationContext.getBean(ArgumentExtractor.BEAN_NAME, ArgumentExtractor.class);
        val service = argumentExtractor.extractService(((JEEContext) context).getNativeRequest());
        return Optional.ofNullable(service)
            .map(Unchecked.function(_ -> {
                val serviceSelectionPlan = applicationContext.getBean(AuthenticationServiceSelectionPlan.BEAN_NAME, AuthenticationServiceSelectionPlan.class);
                val resolvedService = serviceSelectionPlan.resolveService(service);
                val authnRequestId = resolvedService.getAttributes().get(SamlIdPConstants.AUTHN_REQUEST_ID);
                return CollectionUtils.firstElement(authnRequestId)
                    .map(Object::toString)
                    .map(ctx::get)
                    .orElse(null);
            }));
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @Accessors(chain = true)
    private static final class SamlIdPSessionEntry implements Serializable {
        @Serial
        private static final long serialVersionUID = 8119055575574523810L;

        private String id;

        private String samlRequest;

        private String relayState;

        private String context;
    }
}
