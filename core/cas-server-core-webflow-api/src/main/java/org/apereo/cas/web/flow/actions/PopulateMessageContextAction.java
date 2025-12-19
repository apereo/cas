package org.apereo.cas.web.flow.actions;

import module java.base;
import lombok.val;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.binding.message.Severity;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link PopulateMessageContextAction}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class PopulateMessageContextAction {

    public static class Warning extends ConsumerExecutionAction {
        public Warning(final String... codes) {
            super(requestContext -> addMessage(codes, requestContext, Severity.WARNING));
        }
    }

    public static class Errors extends ConsumerExecutionAction {
        public Errors(final String... codes) {
            super(requestContext -> addMessage(codes, requestContext, Severity.ERROR));
        }
    }

    public static class Info extends ConsumerExecutionAction {
        public Info(final String... codes) {
            super(requestContext -> addMessage(codes, requestContext, Severity.INFO));
        }
    }
    private static void addMessage(final String[] codes, final RequestContext requestContext, final Severity severity) {
        Arrays.stream(codes).forEach(code -> {
            val message = switch (severity) {
                case INFO -> new MessageBuilder().info().code(code).build();
                case FATAL, ERROR -> new MessageBuilder().error().code(code).build();
                case WARNING -> new MessageBuilder().warning().code(code).build();
            };
            requestContext.getMessageContext().addMessage(message);
        });
    }
}
