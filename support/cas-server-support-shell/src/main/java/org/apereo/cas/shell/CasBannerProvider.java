package org.apereo.cas.shell;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.shell.cli.CasCommandLineParser;
import org.apereo.cas.util.CasVersion;
import org.apereo.cas.util.spring.boot.DefaultCasBanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.shell.plugin.support.DefaultBannerProvider;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

/**
 * This is {@link CasBannerProvider}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Service("casBannerProvider")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CasBannerProvider extends DefaultBannerProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(CasBannerProvider.class);
    
    @Autowired
    private Environment environment;
    
    @Override
    public String getProviderName() {
        return "CAS Command-line Shell";
    }

    @Override
    public String getBanner() {
        
        if (CasCommandLineParser.isSkippingBanner(environment)) {
            return null;    
        }
        
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            final PrintStream ps = new PrintStream(baos, true, StandardCharsets.UTF_8.name());
            new DefaultCasBanner().printBanner(environment, getClass(), ps);
            final String content = new String(baos.toByteArray(), StandardCharsets.UTF_8);
            return content;
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return "CAS";
    }

    @Override
    public String getVersion() {
        if (StringUtils.isBlank(CasVersion.getVersion())) {
            return StringUtils.EMPTY;
        }
        return CasVersion.getVersion() + "#" + CasVersion.getSpecificationVersion();
    }

    @Override
    public String getWelcomeMessage() {
        return "Welcome to " + getProviderName() + ". For assistance press or type \"help\" then hit ENTER.";
    }
}
