package org.apereo.cas.support.saml.banner;

import org.apereo.cas.util.spring.boot.BannerContributor;
import org.opensaml.core.Version;
import org.springframework.core.env.Environment;
import java.util.Formatter;

/**
 * This is {@link SamlBannerContributor}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
public class SamlBannerContributor implements BannerContributor {
    @Override
    public void contribute(final Formatter formatter, final Environment environment) {
        formatter.format("OpenSAML Version: %s%n", Version.getVersion());
    }
}
