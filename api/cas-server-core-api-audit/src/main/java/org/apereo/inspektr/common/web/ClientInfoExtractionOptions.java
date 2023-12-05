package org.apereo.inspektr.common.web;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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
