package org.apereo.cas.nativex;

import org.apereo.cas.util.spring.boot.BannerContributor;
import org.springframework.core.env.Environment;
import java.util.Formatter;

/**
 * This is {@link CasNativeBannerContributor}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class CasNativeBannerContributor implements BannerContributor {
    @Override
    public void contribute(final Formatter formatter, final Environment environment) {
    }
}
