package org.apereo.cas.trusted.authentication.storage;

import module java.base;
import org.apereo.cas.configuration.model.support.mfa.trusteddevice.TrustedDevicesMultifactorProperties;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecord;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecordKeyGenerator;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.http.HttpUtils;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

/**
 * This is {@link RestMultifactorAuthenticationTrustStorage}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class RestMultifactorAuthenticationTrustStorage extends BaseMultifactorAuthenticationTrustStorage {
    private final RestClient restClient;

    public RestMultifactorAuthenticationTrustStorage(final TrustedDevicesMultifactorProperties properties,
                                                     final CipherExecutor<Serializable, String> cipherExecutor,
                                                     final MultifactorAuthenticationTrustRecordKeyGenerator keyGenerationStrategy,
                                                     final RestClient restClient) {
        super(properties, cipherExecutor, keyGenerationStrategy);
        this.restClient = restClient;
    }

    @Override
    public Set<? extends MultifactorAuthenticationTrustRecord> get(final String principal) {
        return getResults(getEndpointUrl(principal));
    }

    @Override
    public Set<? extends MultifactorAuthenticationTrustRecord> get(final ZonedDateTime onOrAfterDate) {
        return getResults(getEndpointUrl(onOrAfterDate.toString()));
    }

    @Override
    public MultifactorAuthenticationTrustRecord get(final long id) {
        val results = getResults(getEndpointUrl(String.valueOf(id)));
        return results.stream()
            .filter(entry -> entry.getId() == id)
            .sorted()
            .findFirst()
            .orElse(null);
    }

    @Override
    public Set<? extends MultifactorAuthenticationTrustRecord> getAll() {
        return getResults(getEndpointUrl(null));
    }

    @Override
    public void remove(final ZonedDateTime expirationDate) {
        restClient.method(HttpMethod.DELETE).uri(getEndpointUrl(null))
            .headers(headers -> headers.addAll(getHttpHeaders())).body(expirationDate)
            .retrieve().toEntity(Object.class);
    }

    @Override
    public void remove(final String key) {
        restClient.delete().uri(getEndpointUrl(key)).retrieve().toBodilessEntity();
    }

    private Set<MultifactorAuthenticationTrustRecord> getResults(final String url) {
        val responseEntity = restClient.get().uri(url)
            .headers(headers -> headers.addAll(getHttpHeaders()))
            .retrieve().toEntity(MultifactorAuthenticationTrustRecord[].class);
        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            val results = responseEntity.getBody();
            return Stream.of(Objects.requireNonNull(results)).collect(Collectors.toSet());
        }
        return new HashSet<>();
    }

    private HttpHeaders getHttpHeaders() {
        val rest = getTrustedDevicesMultifactorProperties().getRest();
        val headers = HttpUtils.createBasicAuthHeaders(rest.getBasicAuthUsername(), rest.getBasicAuthPassword());
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private String getEndpointUrl(final String path) {
        val endpoint = getTrustedDevicesMultifactorProperties().getRest().getUrl();
        return Strings.CI.appendIfMissing(endpoint, "/").concat(StringUtils.defaultString(path));
    }

    @Override
    protected MultifactorAuthenticationTrustRecord saveInternal(final MultifactorAuthenticationTrustRecord record) {
        val response = restClient.post().uri(getEndpointUrl(null))
            .headers(headers -> headers.addAll(getHttpHeaders())).body(record)
            .retrieve().toEntity(Object.class);
        return response.getStatusCode() == HttpStatus.OK ? record : null;
    }
}
