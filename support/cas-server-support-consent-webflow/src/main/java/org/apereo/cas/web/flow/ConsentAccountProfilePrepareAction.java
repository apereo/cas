package org.apereo.cas.web.flow;

import module java.base;
import org.apereo.cas.consent.ConsentEngine;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.support.WebUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import lombok.val;
import org.jooq.lambda.Unchecked;
import org.jspecify.annotations.Nullable;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import tools.jackson.databind.ObjectMapper;

/**
 * This is {@link ConsentAccountProfilePrepareAction}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@RequiredArgsConstructor
public class ConsentAccountProfilePrepareAction extends BaseCasWebflowAction {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    private final ConsentEngine consentEngine;

    @Override
    protected @Nullable Event doExecuteInternal(final RequestContext requestContext) {
        val authentication = WebUtils.getAuthentication(requestContext);
        val principal = authentication.getPrincipal();
        val decisions = consentEngine.getConsentRepository().findConsentDecisions(principal.getId());
        val resolved = decisions
            .stream()
            .map(Unchecked.function(d -> {
                val decision = AccountProfileConsentDecision.builder()
                    .id(d.getId())
                    .service(d.getService())
                    .createdDateTime(d.getCreatedDate())
                    .attributes(consentEngine.resolveConsentableAttributesFrom(d))
                    .options("screen.account.consent." + d.getOptions().name().toLowerCase(Locale.ENGLISH))
                    .reminder(String.format("%s - %s", d.getReminder(), d.getReminderTimeUnit()))
                    .build();
                decision.setJson(MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(decision));
                return decision;
            }))
            .collect(Collectors.toList());
        if (!resolved.isEmpty()) {
            requestContext.getFlowScope().put("consentDecisions", resolved);
        }
        return null;
    }

    @SuperBuilder
    @Getter
    private static final class AccountProfileConsentDecision implements Serializable {
        @Serial
        private static final long serialVersionUID = -5211708226232415390L;

        private final long id;

        private String service;

        private final Map<String, List<Object>> attributes;

        private final LocalDateTime createdDateTime;

        private final String options;

        private final String reminder;

        @Setter
        private String json;
    }
}
