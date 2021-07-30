package org.apereo.cas.support.sms;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apereo.cas.configuration.model.support.sms.SmsModeProperties;
import org.apereo.cas.notifications.sms.SmsSender;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.HttpUtils;
import org.apereo.cas.util.HttpUtils.HttpExecutionRequest;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

/**
 * This is {@link SmsModeSmsSender}.
 *
 * @author Jérôme Rautureau
 * @since 6.4.0
 */
@Getter
@RequiredArgsConstructor
@Slf4j
public class SmsModeSmsSender implements SmsSender {
	private static final String SMSMODE_SENT_SMS_RESPONSE_CODE = "0";
	private final SmsModeProperties properties;

	@Override
	public boolean send(final String from, final String to, final String message) {
		HttpResponse response = null;
		try {
			val parameters = new HashMap<String, Object>();
			val holder = ClientInfoHolder.getClientInfo();
			if (holder != null) {
				parameters.put("clientIpAddress", holder.getClientIpAddress());
				parameters.put("serverIpAddress", holder.getServerIpAddress());
			}
			parameters.put("from", from);
			parameters.put(properties.getToAttributeName(), to);
			parameters.put(properties.getMessageAttributeName(), message);

			val headers = CollectionUtils.<String, Object>wrap("Content-Type", MediaType.TEXT_PLAIN);
			headers.putAll(properties.getHeaders());
			HttpExecutionRequest exec = HttpUtils.HttpExecutionRequest.builder().method(HttpMethod.GET)
					.url(properties.getSendMessageUrl()).parameters(parameters).headers(headers).build();

			response = HttpUtils.execute(exec);

			if (response != null) {
				val status = HttpStatus.valueOf(response.getStatusLine().getStatusCode());
				if (status.is2xxSuccessful()) {
					HttpEntity entity = response.getEntity();
					try {
						Charset charset = (entity.getContentEncoding() != null)
								? Charset.forName(entity.getContentEncoding().getValue())
								: StandardCharsets.ISO_8859_1;
						String resp = IOUtils.toString(entity.getContent(), charset);
						LOGGER.info("Response from SmsMode : '{}'",resp);
						return StringUtils.startsWith(resp, SMSMODE_SENT_SMS_RESPONSE_CODE);
					} catch (UnsupportedOperationException | IOException e) {
						LOGGER.warn("Unable to read smsMode response", e);
					}
				}
				return false;
			}
		} finally {
			HttpUtils.close(response);
		}
		return false;
	}

}
