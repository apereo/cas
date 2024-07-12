package org.apereo.cas.web.flow.actions;

import org.apereo.cas.util.MockRequestContext;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.binding.message.Severity;

/**
 * This is {@link PopulateMessageContextActionTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("WebflowActions")
class PopulateMessageContextActionTests {
    @Test
    void verifyOperation() throws Throwable {
        val context = MockRequestContext.create();

        new PopulateMessageContextAction.Info("code1").setEventId("result").execute(context);
        context.getMessageContext().getMessagesByCriteria(message -> message.getSeverity() == Severity.INFO);

        new PopulateMessageContextAction.Warning("code1").setEventId("result").execute(context);
        context.getMessageContext().getMessagesByCriteria(message -> message.getSeverity() == Severity.WARNING);

        new PopulateMessageContextAction.Errors("code1").setEventId("result").execute(context);
        context.getMessageContext().getMessagesByCriteria(message -> message.getSeverity() == Severity.ERROR);
    }
}
