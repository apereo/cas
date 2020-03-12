package org.apereo.cas.consent;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

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
@Builder
public class CasConsentableAttribute implements Serializable {
    private static final long serialVersionUID = 6097374842509284286L;

    private String name;

    private String friendlyName;

    private List<Object> values;
}
