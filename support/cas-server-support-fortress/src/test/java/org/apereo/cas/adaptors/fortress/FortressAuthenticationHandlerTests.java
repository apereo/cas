package org.apereo.cas.adaptors.fortress;

import org.apereo.cas.adaptors.fortress.config.FortressAuthenticationConfiguration;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.config.*;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collection;

/**
 * This is {@link FortressAuthenticationHandler}.
 *
 * @author yudhi.k.surtan
 * @since 5.2.0
 */

public class FortressAuthenticationHandlerTests {
    private static final Logger LOGGER = LoggerFactory.getLogger(FortressAuthenticationHandlerTests.class);
}
