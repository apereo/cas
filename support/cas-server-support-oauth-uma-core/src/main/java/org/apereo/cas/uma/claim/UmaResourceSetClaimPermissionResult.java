package org.apereo.cas.uma.claim;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This is {@link UmaResourceSetClaimPermissionResult}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Data
public class UmaResourceSetClaimPermissionResult implements Serializable {
    private static final int MAP_SIZE = 8;
    private static final long serialVersionUID = -4793142197818018354L;

    private Map<Long, Details> details = new LinkedHashMap<>(MAP_SIZE);

    @JsonIgnore
    public boolean isSatisfied() {
        return details.isEmpty();
    }

    /**
     * The Details for analysis result.
     */
    @Data
    public static class Details implements Serializable {
        private static final long serialVersionUID = -4294568893393275983L;

        private Collection<String> unmatchedScopes = new ArrayList<>(MAP_SIZE);

        private Map<String, Object> unmatchedClaims = new LinkedHashMap<>(MAP_SIZE);

        @JsonIgnore
        public boolean isSatisfied() {
            return unmatchedScopes.isEmpty() && unmatchedClaims.isEmpty();
        }
    }
}
