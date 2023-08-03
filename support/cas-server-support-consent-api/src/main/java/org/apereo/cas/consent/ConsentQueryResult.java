package org.apereo.cas.consent;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.With;
import java.io.Serial;
import java.io.Serializable;

/**
 * This is {@link ConsentQueryResult}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@ToString
@RequiredArgsConstructor(staticName = "of")
@AllArgsConstructor
@Getter
@With
public class ConsentQueryResult implements Serializable {
    @Serial
    private static final long serialVersionUID = 742133551083867719L;

    private final boolean required;
    private ConsentDecision consentDecision;
    private Service service;
    private Authentication authentication;

    static ConsentQueryResult ignored() {
        return ConsentQueryResult.of(false);
    }

    static ConsentQueryResult required() {
        return ConsentQueryResult.of(true);
    }
}
