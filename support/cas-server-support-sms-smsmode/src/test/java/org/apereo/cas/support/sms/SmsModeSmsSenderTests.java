package org.apereo.cas.support.sms;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.apereo.cas.config.SmsModeSmsConfiguration;
import org.apereo.cas.notifications.sms.SmsSender;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

/**
 * This is {@link ClickatellSmsSenderTests}.
 *
 * @author Jérôme Rautureau
 * @since 6.4.0
 */
@SpringBootTest(classes = { RefreshAutoConfiguration.class, SmsModeSmsConfiguration.class })
@Tag("SMS")
public class SmsModeSmsSenderTests {
	@Autowired
	@Qualifier("smsSender")
	private SmsSender smsSender;

	@Test
	public void verifyOperation() {
		assertNotNull(smsSender);
	}
}
