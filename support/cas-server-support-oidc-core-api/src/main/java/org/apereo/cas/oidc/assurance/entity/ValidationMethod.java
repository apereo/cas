package org.apereo.cas.oidc.assurance.entity;

import module java.base;
import lombok.Getter;
import lombok.Setter;

/**
 * This is {@link ValidationMethod}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Getter
@Setter
public class ValidationMethod implements Serializable {
    @Serial
    private static final long serialVersionUID = -3277432141173651147L;
    
    private String type;

    private String policy;

    private String procedure;

    private String status;
}
