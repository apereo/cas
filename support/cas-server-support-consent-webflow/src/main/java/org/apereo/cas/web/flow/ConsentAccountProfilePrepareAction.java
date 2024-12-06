package org.apereo.cas.web.flow;

import org.apereo.cas.consent.ConsentEngine;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.support.WebUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import lombok.val;
import org.jooq.lambda.Unchecked;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

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
    protected Event doExecuteInternal(final RequestContext requestContext) {
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
