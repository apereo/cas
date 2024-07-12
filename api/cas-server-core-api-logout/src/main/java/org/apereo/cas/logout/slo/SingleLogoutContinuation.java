package org.apereo.cas.logout.slo;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.springframework.http.HttpMethod;
import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * This is {@link SingleLogoutContinuation}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@SuperBuilder
@Getter
public class SingleLogoutContinuation implements Serializable {
    @Serial
    private static final long serialVersionUID = 1350244643948535816L;

    private final String content;
    private final String url;
    @Builder.Default
    private final Map<String, String> data = new HashMap<>();
    @Builder.Default
    private final HttpMethod method = HttpMethod.POST;
    @Builder.Default
    private final Map<String, Serializable> context = new HashMap<>();
}
