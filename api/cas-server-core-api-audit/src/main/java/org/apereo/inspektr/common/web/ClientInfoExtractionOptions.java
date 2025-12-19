package org.apereo.inspektr.common.web;

import module java.base;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * This is {@link ClientInfoExtractionOptions}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@SuperBuilder
@Getter
@ToString
public class ClientInfoExtractionOptions implements Serializable {
    @Serial
    private static final long serialVersionUID = 133116081945557963L;

    private final String alternateServerAddrHeaderName;
    private final String alternateLocalAddrHeaderName;
    private final boolean useServerHostAddress;
    @Builder.Default
    private final List<String> httpRequestHeaders = new ArrayList<>();
}
