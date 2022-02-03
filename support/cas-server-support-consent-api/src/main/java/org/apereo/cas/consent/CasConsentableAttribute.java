package org.apereo.cas.consent;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.List;

/**
 * This is {@link CasConsentableAttribute}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode
@SuperBuilder
public class CasConsentableAttribute implements Serializable {
    private static final long serialVersionUID = 6097374842509284286L;

    private String name;

    private String friendlyName;

    private List<Object> values;
}
