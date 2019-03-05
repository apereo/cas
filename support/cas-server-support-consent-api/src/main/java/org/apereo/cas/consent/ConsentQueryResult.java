package org.apereo.cas.consent;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

/**
 * This is {@link ConsentQueryResult}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@ToString
@RequiredArgsConstructor
@Getter
public class ConsentQueryResult implements Serializable {
    private static final long serialVersionUID = 742133551083867719L;

    private final boolean required;
    private final ConsentDecision consentDecision;

    /**
     * Required consent query result.
     *
     * @return the consent query result
     */
    public static ConsentQueryResult required() {
        return required(null);
    }

    /**
     * Required consent query result.
     *
     * @param decision the decision
     * @return the consent query result
     */
    public static ConsentQueryResult required(final ConsentDecision decision) {
        return new ConsentQueryResult(true, decision);
    }

    /**
     * Ignored consent query result.
     *
     * @return the consent query result
     */
    public static ConsentQueryResult ignored() {
        return new ConsentQueryResult(false, null);
    }
}
