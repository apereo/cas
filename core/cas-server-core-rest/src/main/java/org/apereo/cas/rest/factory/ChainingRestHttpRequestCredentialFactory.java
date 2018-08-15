package org.apereo.cas.rest.factory;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.Credential;
import org.springframework.core.OrderComparator;
import org.springframework.util.MultiValueMap;

import javax.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This is {@link ChainingRestHttpRequestCredentialFactory}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
@Getter
@AllArgsConstructor
public class ChainingRestHttpRequestCredentialFactory implements RestHttpRequestCredentialFactory {
    private final List<RestHttpRequestCredentialFactory> chain;

    public ChainingRestHttpRequestCredentialFactory(final RestHttpRequestCredentialFactory... chain) {
        this.chain = Stream.of(chain).collect(Collectors.toList());
    }

    /**
     * Add credential factory.
     *
     * @param factory the factory
     */
    public void registerCredentialFactory(final RestHttpRequestCredentialFactory factory) {
        this.chain.add(factory);
    }

    @Override
    public List<Credential> fromRequest(final HttpServletRequest request, final MultiValueMap<String, String> requestBody) {
        OrderComparator.sort(this.chain);
        return this.chain
            .stream()
            .map(f -> f.fromRequest(request, requestBody))
            .flatMap(List::stream)
            .collect(Collectors.toList());
    }
}
