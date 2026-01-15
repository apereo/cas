package org.apereo.cas.oidc.assurance.entity;

import module java.base;
import lombok.Getter;
import lombok.Setter;

/**
 * This is {@link Verifier}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Getter
@Setter
public class Verifier implements Serializable {
    @Serial
    private static final long serialVersionUID = 7434984801114957053L;

    private String organization;
    private String txn;
}
