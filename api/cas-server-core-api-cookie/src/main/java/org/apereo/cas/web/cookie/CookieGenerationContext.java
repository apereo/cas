package org.apereo.cas.web.cookie;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

/**
 * This is {@link CookieGenerationContext}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@ToString
@Getter
@Builder
public class CookieGenerationContext implements Serializable {
    /**
     * Empty cookie generation context.
     */
    public static final CookieGenerationContext EMPTY = CookieGenerationContext.builder().build();

    private static final long serialVersionUID = -3058351444389458036L;

    private static final int DEFAULT_REMEMBER_ME_MAX_AGE = 7889231;

    @Setter
    private String name;

    @Builder.Default
    private String comment = "CAS Cookie";

    @Builder.Default
    private String path = StringUtils.EMPTY;

    @Builder.Default
    private int maxAge = -1;

    @Builder.Default
    private boolean secure = true;

    @Builder.Default
    private String domain = StringUtils.EMPTY;

    @Builder.Default
    private int rememberMeMaxAge = DEFAULT_REMEMBER_ME_MAX_AGE;

    @Builder.Default
    private boolean httpOnly = true;

    @Builder.Default
    private String sameSitePolicy = StringUtils.EMPTY;
}
