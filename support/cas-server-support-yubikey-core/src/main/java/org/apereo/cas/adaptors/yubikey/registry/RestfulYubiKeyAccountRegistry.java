package org.apereo.cas.adaptors.yubikey.registry;

import org.apereo.cas.adaptors.yubikey.YubiKeyAccount;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccountValidator;
import org.apereo.cas.adaptors.yubikey.YubiKeyDeviceRegistrationRequest;
import org.apereo.cas.adaptors.yubikey.YubiKeyRegisteredDevice;
import org.apereo.cas.configuration.model.support.mfa.yubikey.YubiKeyRestfulMultifactorProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.HttpUtils;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.springframework.http.HttpStatus;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This is {@link RestfulYubiKeyAccountRegistry}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class RestfulYubiKeyAccountRegistry extends BaseYubiKeyAccountRegistry {

    private static final ObjectMapper MAPPER = new ObjectMapper()
        .enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY)
        .findAndRegisterModules();

    private final YubiKeyRestfulMultifactorProperties restProperties;

    public RestfulYubiKeyAccountRegistry(final YubiKeyRestfulMultifactorProperties restProperties,
                                         final YubiKeyAccountValidator validator) {
        super(validator);
        this.restProperties = restProperties;
    }

    @Override
    public YubiKeyAccount save(final YubiKeyDeviceRegistrationRequest request, final YubiKeyRegisteredDevice... device) {
        HttpResponse response = null;
        try {
            val account = YubiKeyAccount.builder()
                .username(request.getUsername())
                .devices(CollectionUtils.wrapList(device))
                .build();
            update(account);
            return account;
        } finally {
            HttpUtils.close(response);
        }
    }

    @Override
    public boolean update(final YubiKeyAccount account) {
        HttpResponse response = null;
        try {
            response = HttpUtils.executePost(restProperties.getUrl(),
                restProperties.getBasicAuthUsername(), restProperties.getBasicAuthPassword(),
                MAPPER.writeValueAsString(account));
            return response != null && HttpStatus.valueOf(response.getStatusLine().getStatusCode()).is2xxSuccessful();
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            HttpUtils.close(response);
        }
        return false;
    }

    @Override
    public void delete(final String username, final long deviceId) {
        HttpResponse response = null;
        try {
            val url = StringUtils.appendIfMissing(restProperties.getUrl(), "/")
                .concat(username).concat("/").concat(String.valueOf(deviceId));
            response = HttpUtils.executeDelete(url,
                restProperties.getBasicAuthUsername(), restProperties.getBasicAuthPassword());
        } finally {
            HttpUtils.close(response);
        }
    }

    @Override
    public void delete(final String username) {
        HttpResponse response = null;
        try {
            val url = StringUtils.appendIfMissing(restProperties.getUrl(), "/").concat(username);
            response = HttpUtils.executeDelete(url,
                restProperties.getBasicAuthUsername(), restProperties.getBasicAuthPassword());
        } finally {
            HttpUtils.close(response);
        }
    }

    @Override
    public void deleteAll() {
        HttpResponse response = null;
        try {
            response = HttpUtils.executeDelete(restProperties.getUrl(),
                restProperties.getBasicAuthUsername(), restProperties.getBasicAuthPassword());
        } finally {
            HttpUtils.close(response);
        }
    }

    @Override
    protected YubiKeyAccount getAccountInternal(final String username) {
        HttpResponse response = null;
        try {
            val url = StringUtils.appendIfMissing(restProperties.getUrl(), "/").concat(username);
            response = HttpUtils.executeGet(url,
                restProperties.getBasicAuthUsername(), restProperties.getBasicAuthPassword());
            if (response != null) {
                val status = HttpStatus.valueOf(response.getStatusLine().getStatusCode());
                if (status.is2xxSuccessful()) {
                    val content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
                    return MAPPER.readValue(content, YubiKeyAccount.class);
                }
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            HttpUtils.close(response);
        }
        return null;
    }

    @Override
    protected Collection<? extends YubiKeyAccount> getAccountsInternal() {
        HttpResponse response = null;
        try {
            response = HttpUtils.executeGet(restProperties.getUrl(),
                restProperties.getBasicAuthUsername(), restProperties.getBasicAuthPassword());
            if (response != null) {
                val status = HttpStatus.valueOf(response.getStatusLine().getStatusCode());
                if (status.is2xxSuccessful()) {
                    val content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
                    return MAPPER.readValue(content, new TypeReference<List<YubiKeyAccount>>() {
                    });
                }
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            HttpUtils.close(response);
        }
        return new ArrayList<>(0);
    }
}
