package org.apereo.cas.nativex;

import org.apereo.cas.util.spring.boot.BannerContributor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.graalvm.home.Version;
import org.springframework.core.env.Environment;
import java.util.Formatter;

/**
 * This is {@link CasNativeBannerContributor}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class CasNativeBannerContributor implements BannerContributor {
    private static final String GRAALVM_VERSION = Version.getCurrent().toString();

    @Override
    public void contribute(final Formatter formatter, final Environment environment) {
        val javaVendor = System.getProperty("java.vendor.version");
        if (StringUtils.isNotBlank(javaVendor)) {
            formatter.format("Graal VM Version: %s%n", GRAALVM_VERSION);
        }
    }
}
