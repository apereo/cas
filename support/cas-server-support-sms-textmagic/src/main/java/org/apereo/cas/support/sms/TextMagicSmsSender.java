package org.apereo.cas.support.sms;

import org.apereo.cas.configuration.model.support.sms.TextMagicProperties;
import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.util.io.SmsSender;

import com.squareup.okhttp.OkHttpClient;
import com.textmagic.sdk.ApiClient;
import com.textmagic.sdk.api.TextMagicApi;
import com.textmagic.sdk.model.SendMessageInputObject;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * This is {@link TextMagicSmsSender}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
public class TextMagicSmsSender implements SmsSender {
    private final TextMagicApi api;

    public TextMagicSmsSender(final TextMagicProperties properties,
                              final Optional<HttpClient> httpClient) {
        val client = new ApiClient();
        if (StringUtils.isNotBlank(properties.getUsername())) {
            client.setUsername(properties.getUsername());
        }
        if (StringUtils.isNotBlank(properties.getToken())) {
            client.setAccessToken(properties.getToken());
        }
        client.setDebugging(properties.isDebugging());
        client.setVerifyingSsl(properties.isVerifyingSsl());

        if (StringUtils.isNotBlank(properties.getPassword())) {
            client.setPassword(properties.getPassword());
        }

        client.setReadTimeout(properties.getReadTimeout());
        client.setConnectTimeout(properties.getConnectTimeout());
        client.setWriteTimeout(properties.getWriteTimeout());

        if (StringUtils.isNotBlank(properties.getUserAgent())) {
            client.setUserAgent(properties.getUserAgent());
        }

        if (StringUtils.isNotBlank(properties.getApiKey())) {
            client.setApiKey(properties.getApiKey());
            client.setApiKeyPrefix(properties.getApiKeyPrefix());
        }

        httpClient.ifPresent(c -> {
            val okHttpClient = new OkHttpClient();
            val httpClientFactory = c.getHttpClientFactory();
            okHttpClient.setSslSocketFactory(httpClientFactory.getSslContext().getSocketFactory());
            okHttpClient.setHostnameVerifier(httpClientFactory.getHostnameVerifier());
            okHttpClient.setConnectTimeout(httpClientFactory.getConnectionTimeout(), TimeUnit.SECONDS);
            client.setHttpClient(okHttpClient);
        });
        this.api = new TextMagicApi(client);
    }

    @Override
    public boolean send(final String from, final String to, final String text) {
        try {
            val message = new SendMessageInputObject();
            message.setFrom(from);
            message.setText(text);
            message.setContacts(to);
            val result = this.api.sendMessage(message);
            return result != null && result.getMessageId() > 0;
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return false;
    }
}


