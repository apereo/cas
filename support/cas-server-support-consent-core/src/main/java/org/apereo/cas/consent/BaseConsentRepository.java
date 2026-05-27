package org.apereo.cas.consent;

import module java.base;
import lombok.Getter;
import lombok.Setter;

/**
 * This is {@link BaseConsentRepository}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@Getter
@Setter
public abstract class BaseConsentRepository implements ConsentRepository {
    @Serial
    private static final long serialVersionUID = 2486254621733196849L;
    /**
     * Arbitrary runtime properties that may be used to configure the repository.
     */
    private Map<String, Serializable> tags = new LinkedHashMap<>();
}
