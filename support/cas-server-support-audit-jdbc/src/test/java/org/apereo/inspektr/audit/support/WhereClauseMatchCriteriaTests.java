package org.apereo.inspektr.audit.support;

import module java.base;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link WhereClauseMatchCriteriaTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("Audits")
class WhereClauseMatchCriteriaTests {
    @Test
    void verifyOperation() {
        val criteria1 = new NoMatchWhereClauseMatchCriteria();
        assertTrue(criteria1.getParameterValues().isEmpty());
        assertNotNull(criteria1.toString());

        val criteria2 = new MaxAgeWhereClauseMatchCriteria(10);
        assertFalse(criteria2.getParameterValues().isEmpty());
        assertNotNull(criteria2.toString());

        val criteria3 = new DurationWhereClauseMatchCriteria("PT10S");
        assertFalse(criteria3.getParameterValues().isEmpty());
        assertNotNull(criteria3.toString());
    }

}
