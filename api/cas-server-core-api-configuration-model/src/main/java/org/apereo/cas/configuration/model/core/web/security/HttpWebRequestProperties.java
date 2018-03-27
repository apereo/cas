package org.apereo.cas.configuration.model.core.web.security;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.support.RequiresModule;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import lombok.Getter;
import lombok.Setter;

/**
 * This is {@link HttpWebRequestProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RequiresModule(name = "cas-server-core-web", automated = true)
@Slf4j
@Getter
@Setter
public class HttpWebRequestProperties implements Serializable {

    private static final long serialVersionUID = -4711604991237695091L;

    /**
     * Control and specify the encoding for all http requests.
     */
    private String encoding = StandardCharsets.UTF_8.name();

    /**
     * Whether specified encoding should be forced for every request.
     * Whether the specified encoding is supposed to
     * override existing request and response encodings
     */
    private boolean forceEncoding = true;
}
