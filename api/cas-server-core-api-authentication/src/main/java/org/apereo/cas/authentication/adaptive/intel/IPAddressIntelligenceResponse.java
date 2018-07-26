package org.apereo.cas.authentication.adaptive.intel;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

/**
 * This is {@link IPAddressIntelligenceResponse}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Builder
@Getter
public class IPAddressIntelligenceResponse implements Serializable {

    private static final long serialVersionUID = 6438211402312848819L;

    /**
     * Status of ip addresses after examination.
     */
    @RequiredArgsConstructor
    @Getter
    public enum IPAddressIntelligenceStatus {
        /**
         * The address is explicitly banned.
         */
        BANNED(1),
        /**
         * In-between status where a score is provided
         * to determine rank and probability.
         */
        RANKED(-1),
        /**
         * The address is explicitly allowed and open.
         */
        ALLOWED(0);

        private final int score;
    }

    private double score;

    /**
     * Properties.
     */
    @Builder.Default
    private Map<String, Object> properties = new TreeMap<>();

    @Builder.Default
    private IPAddressIntelligenceStatus status = IPAddressIntelligenceStatus.ALLOWED;

    public boolean isBanned() {
        return status == IPAddressIntelligenceStatus.BANNED;
    }

    public boolean isRanked() {
        return status == IPAddressIntelligenceStatus.RANKED;
    }

    public boolean isAllowed() {
        return status == IPAddressIntelligenceStatus.ALLOWED;
    }

    /**
     * Allowed ip address intelligence response.
     *
     * @return the ip address intelligence response
     */
    public static IPAddressIntelligenceResponse allowed() {
        return builder()
            .status(IPAddressIntelligenceStatus.ALLOWED)
            .score(IPAddressIntelligenceStatus.ALLOWED.getScore())
            .build();
    }

    /**
     * Banned ip address intelligence response.
     *
     * @return the ip address intelligence response
     */
    public static IPAddressIntelligenceResponse banned() {
        return builder()
            .status(IPAddressIntelligenceStatus.BANNED)
            .score(IPAddressIntelligenceStatus.BANNED.getScore())
            .build();
    }
}
