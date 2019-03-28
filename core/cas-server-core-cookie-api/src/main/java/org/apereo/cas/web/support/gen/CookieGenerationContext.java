package org.apereo.cas.web.support.gen;

import lombok.Builder;
import lombok.Getter;
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
    private static final long serialVersionUID = -3058351444389458036L;

    private static final int DEFAULT_REMEMBER_ME_MAX_AGE = 7889231;

    /**
     * Empty cookie generation context.
     */
    public static final CookieGenerationContext EMPTY = CookieGenerationContext.builder().build();

    private String name;

    private String path = StringUtils.EMPTY;

    private int maxAge;

    @Builder.Default
    private boolean secure = true;

    private String domain = StringUtils.EMPTY;

    @Builder.Default
    private int rememberMeMaxAge = DEFAULT_REMEMBER_ME_MAX_AGE;

    @Builder.Default
    private boolean httpOnly = true;
}
