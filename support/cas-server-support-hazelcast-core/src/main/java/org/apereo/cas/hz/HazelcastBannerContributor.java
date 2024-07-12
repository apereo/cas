package org.apereo.cas.hz;

import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.apereo.cas.util.spring.boot.BannerContributor;
import com.hazelcast.instance.BuildInfoProvider;
import lombok.val;
import org.springframework.core.env.Environment;
import java.util.Formatter;

/**
 * This is {@link HazelcastBannerContributor}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
public class HazelcastBannerContributor implements BannerContributor {
    @Override
    public void contribute(final Formatter formatter, final Environment environment) {
        if (CasRuntimeHintsRegistrar.notInNativeImage()) {
            val info = BuildInfoProvider.getBuildInfo();
            formatter.format("Hazelcast Version: %s.%s.%s%n", info.getVersion(), info.getRevision(), info.getBuild());
        }
    }
}
