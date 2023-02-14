package org.apereo.cas.services;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import java.io.Serial;

/**
 * Mutable registered service that uses Java regular expressions for service matching.
 * Matching is case insensitive, and is successful, if, and only if, the entire region
 * sequence matches the pattern.
 *
 * @author Marvin S. Addison
 * @author Misagh Moayyed
 * @since 3.4
 * @deprecated This class is scheduled to be replaced with {@code CasRegi}
 */
@EqualsAndHashCode(callSuper = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@Deprecated(since = "6.6.0")
@Slf4j
public class RegexRegisteredService extends BaseWebBasedRegisteredService {
    @Serial
    private static final long serialVersionUID = -8258660210826975771L;

    public RegexRegisteredService() {
        LOGGER.warn("CAS has located a service definition type that is now tagged as [RegexRegisteredService]. "
                    + "This registered service definition type is deprecated and scheduled for removal and should no longer be "
                    + "used for CAS-enabled applications, and MUST be replaced with [{}] instead. We STRONGLY advise "
                    + "that you update your service definitions and make the replacement to facilitate future CAS upgrades.",
            CasRegisteredService.class.getName());
    }

    @JsonIgnore
    @Override
    public String getFriendlyName() {
        return CasRegisteredService.FRIENDLY_NAME;
    }
}
