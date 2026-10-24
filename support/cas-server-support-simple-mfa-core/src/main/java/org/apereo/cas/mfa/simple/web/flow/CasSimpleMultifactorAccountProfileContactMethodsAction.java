package org.apereo.cas.mfa.simple.web.flow;

import module java.base;
import org.apereo.cas.configuration.model.support.mfa.simple.CasSimpleMultifactorAuthenticationProperties;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.support.WebUtils;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link CasSimpleMultifactorAccountProfileContactMethodsAction}.
 * Shows in the account profile the email addresses and phone numbers that receive
 * one-time codes, read from the principal attributes that simple multifactor authentication uses.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequiredArgsConstructor
public class CasSimpleMultifactorAccountProfileContactMethodsAction extends BaseCasWebflowAction {
    /**
     * Flow scope attribute for the contact methods, each a map with {@code type} (email or phone) and {@code value}.
     */
    public static final String FLOW_SCOPE_ATTR_CONTACT_METHODS = "simpleMfaContactMethods";

    /**
     * Flow scope attribute that tells whether the user may register an email address.
     */
    public static final String FLOW_SCOPE_ATTR_EMAIL_REGISTRATION_ENABLED = "simpleMfaEmailRegistrationEnabled";

    private final CasSimpleMultifactorAuthenticationProperties properties;

    @Override
    protected @Nullable Event doExecuteInternal(final RequestContext requestContext) {
        val attributes = WebUtils.getAuthentication(requestContext).getPrincipal().getAttributes();
        val methods = new ArrayList<Map<String, String>>();
        collect(attributes, properties.getMail().getAttributeName(), "email", methods);
        val phoneAttributes = new ArrayList<>(properties.getSms().getAttributeName());
        phoneAttributes.add(properties.getPhone().getAttributeName());
        collect(attributes, phoneAttributes, "phone", methods);
        requestContext.getFlowScope().put(FLOW_SCOPE_ATTR_CONTACT_METHODS, methods);
        requestContext.getFlowScope().put(FLOW_SCOPE_ATTR_EMAIL_REGISTRATION_ENABLED, properties.getMail().isRegistrationEnabled());
        return null;
    }

    private static void collect(final Map<String, List<Object>> attributes, final List<String> attributeNames,
                                final String type, final List<Map<String, String>> methods) {
        attributeNames
            .stream()
            .filter(StringUtils::isNotBlank)
            .map(attributes::get)
            .filter(Objects::nonNull)
            .flatMap(List::stream)
            .map(Object::toString)
            .filter(StringUtils::isNotBlank)
            .distinct()
            .filter(value -> methods.stream().noneMatch(method -> value.equals(method.get("value"))))
            .forEach(value -> methods.add(Map.of("type", type, "value", value)));
    }
}
