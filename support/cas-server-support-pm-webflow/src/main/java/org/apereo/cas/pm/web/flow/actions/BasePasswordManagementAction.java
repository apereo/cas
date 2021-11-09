package org.apereo.cas.pm.web.flow.actions;

import lombok.val;
import org.springframework.webflow.action.AbstractAction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This is {@link BasePasswordManagementAction}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
public abstract class BasePasswordManagementAction extends AbstractAction {
    /**
     * Orders security questions consistently.
     *
     * @param questionMap A map of question/answer key/value pairs
     * @return A list of questions in a consistent order
     */
    protected static List<String> canonicalizeSecurityQuestions(final Map<String, String> questionMap) {
        val keys = new ArrayList<>(questionMap.keySet());
        keys.sort(String.CASE_INSENSITIVE_ORDER);
        return keys;
    }
}
