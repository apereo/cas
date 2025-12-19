package org.apereo.cas.oidc.assurance.entity;

import module java.base;
import lombok.Getter;
import lombok.Setter;

/**
 * This is {@link AssuranceProcess}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Getter
@Setter
public class AssuranceProcess implements Serializable {
    @Serial
    private static final long serialVersionUID = -3982701621389370247L;
    private String policy;
    private String procedure;
}
