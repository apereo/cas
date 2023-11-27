package org.apereo.cas.oidc.assurance.entity;

import lombok.Getter;
import lombok.Setter;
import java.io.Serial;
import java.io.Serializable;

/**
 * This is {@link VerificationMethod}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Getter
@Setter
public class VerificationMethod implements Serializable {
    @Serial
    private static final long serialVersionUID = 955939673207506776L;

    private String type;
    private String policy;
    private String procedure;
}
