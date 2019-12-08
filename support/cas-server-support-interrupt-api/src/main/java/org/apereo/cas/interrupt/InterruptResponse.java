package org.apereo.cas.interrupt;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This is {@link InterruptResponse}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@ToString
@Getter
@Setter
@EqualsAndHashCode
public class InterruptResponse implements Serializable {
    /**
     * The default message when flows are interrupted.
     */
    public static final String DEFAULT_MESSAGE = "Authentication flow is interrupted";

    private static final int MAP_SIZE = 8;

    private static final long serialVersionUID = 2558836528840508196L;

    private String message;

    private Map<String, String> links = new LinkedHashMap<>(MAP_SIZE);

    private boolean block;

    private boolean ssoEnabled;

    private boolean interrupt;

    private boolean autoRedirect;

    private long autoRedirectAfterSeconds = -1;

    private Map<String, List<String>> data = new LinkedHashMap<>(MAP_SIZE);

    public InterruptResponse(final boolean interrupt) {
        this.interrupt = interrupt;
    }

    public InterruptResponse(final String message, final boolean block, final boolean ssoEnabled) {
        this(true);
        this.message = message;
        this.block = block;
        this.ssoEnabled = ssoEnabled;
    }

    public InterruptResponse(final String message) {
        this(message, false, true);
    }

    public InterruptResponse() {
        this(DEFAULT_MESSAGE, false, true);
    }

    public InterruptResponse(final String message, final Map<String, String> links, final boolean block, final boolean ssoEnabled) {
        this(message, block, ssoEnabled);
        this.links = links;
    }

    /**
     * No interruptions interrupt response.
     *
     * @return the interrupt response
     */
    public static InterruptResponse none() {
        return new InterruptResponse(false);
    }

    /**
     * Interrupt interrupt response.
     *
     * @return the interrupt response
     */
    public static InterruptResponse interrupt() {
        return new InterruptResponse(DEFAULT_MESSAGE);
    }
}
