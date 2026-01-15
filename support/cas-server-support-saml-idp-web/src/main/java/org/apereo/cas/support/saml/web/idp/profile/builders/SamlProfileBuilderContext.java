package org.apereo.cas.support.saml.web.idp.profile.builders;

import module java.base;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceMetadataAdaptor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.RequestAbstractType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;

/**
 * This is {@link SamlProfileBuilderContext}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Getter
@SuperBuilder
@ToString(of = {"authenticatedAssertion", "registeredService", "binding"})
public class SamlProfileBuilderContext {
    private final RequestAbstractType samlRequest;

    private final HttpServletRequest httpRequest;

    private final HttpServletResponse httpResponse;

    private final Optional<AuthenticatedAssertionContext> authenticatedAssertion;

    @NotNull
    private final SamlRegisteredService registeredService;

    @NotNull
    private final SamlRegisteredServiceMetadataAdaptor adaptor;

    @NotNull
    @Builder.Default
    private final String binding = SAMLConstants.SAML2_POST_BINDING_URI;

    @Builder.Default
    private final MessageContext messageContext = new MessageContext();

    private final String sessionIndex;

    /**
     * Transfer to a new context.
     *
     * @param request   the query
     * @param toBinding the binding
     * @return the saml profile builder context
     */
    public SamlProfileBuilderContext transferTo(final RequestAbstractType request, final String toBinding) {
        return SamlProfileBuilderContext.builder()
            .samlRequest(request)
            .httpRequest(httpRequest)
            .httpResponse(httpResponse)
            .authenticatedAssertion(authenticatedAssertion)
            .registeredService(registeredService)
            .adaptor(adaptor)
            .binding(toBinding)
            .build();
    }
}
