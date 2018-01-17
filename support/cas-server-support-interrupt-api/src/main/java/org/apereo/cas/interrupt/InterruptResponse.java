package org.apereo.cas.interrupt;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This is {@link InterruptResponse}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@ToString
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class InterruptResponse implements Serializable {

    private static final long serialVersionUID = 2558836528840508196L;

    private String message;

    private Map<String, String> links = new LinkedHashMap<>();

    private boolean block;

    private boolean ssoEnabled;

    private boolean interrupt;

    private boolean autoRedirect;

    private long autoRedirectAfterSeconds = -1;

    public InterruptResponse(final String message, final boolean block, final boolean ssoEnabled) {
        this.message = message;
        this.block = block;
        this.ssoEnabled = ssoEnabled;
        this.interrupt = true;
    }

    public InterruptResponse(final boolean interrupt) {
        this.interrupt = interrupt;
    }

    public InterruptResponse(final String message, final Map<String, String> links, final boolean block, final boolean ssoEnabled) {
        this.message = message;
        this.links = links;
        this.block = block;
        this.ssoEnabled = ssoEnabled;
        this.interrupt = true;
    }
}
