package org.apereo.cas.support.events.audit;

import lombok.val;
import org.apereo.inspektr.audit.AuditActionContext;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasAuditActionContextRecordedEventTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Events")
public class CasAuditActionContextRecordedEventTests {

    @Test
    public void verifyOperation() {
        assertDoesNotThrow(new Executable() {
            @Override
            @SuppressWarnings("JavaUtilDate")
            public void execute() throws Throwable {
                val ctx = new AuditActionContext("casuser", "TEST", "TEST",
                    "CAS", new Date(), "1.2.3.4",
                    "1.2.3.4");
                new CasAuditActionContextRecordedEvent(this, ctx);
            }
        });
    }

}
