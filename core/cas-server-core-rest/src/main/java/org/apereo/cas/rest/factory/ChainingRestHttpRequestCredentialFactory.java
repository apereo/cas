package org.apereo.cas.rest.factory;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.util.MultiValueMap;

import javax.servlet.http.HttpServletRequest;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This is {@link ChainingRestHttpRequestCredentialFactory}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Getter
@RequiredArgsConstructor
public class ChainingRestHttpRequestCredentialFactory implements RestHttpRequestCredentialFactory {
    private final List<RestHttpRequestCredentialFactory> chain;

    public ChainingRestHttpRequestCredentialFactory(final RestHttpRequestCredentialFactory... chain) {
        this.chain = Stream.of(chain)
            .sorted(Comparator.comparing(RestHttpRequestCredentialFactory::getOrder))
            .collect(Collectors.toList());
    }

    /**
     * Add credential factory.
     *
     * @param factory the factory
     */
    public void registerCredentialFactory(final RestHttpRequestCredentialFactory factory) {
        this.chain.add(factory);
        AnnotationAwareOrderComparator.sort(this.chain);
    }

    @Override
    public List<Credential> fromRequest(final HttpServletRequest request,
                                        final MultiValueMap<String, String> requestBody) {
        return this.chain
            .stream()
            .sorted(Comparator.comparing(RestHttpRequestCredentialFactory::getOrder))
            .map(f -> f.fromRequest(request, requestBody))
            .flatMap(List::stream)
            .collect(Collectors.toList());
    }

    @Override
    public List<Credential> fromAuthentication(final HttpServletRequest request,
                                               final MultiValueMap<String, String> requestBody,
                                               final Authentication authentication,
                                               final MultifactorAuthenticationProvider provider) {
        return this.chain
            .stream()
            .sorted(Comparator.comparing(RestHttpRequestCredentialFactory::getOrder))
            .map(f -> f.fromAuthentication(request, requestBody, authentication, provider))
            .flatMap(List::stream)
            .collect(Collectors.toList());
    }
}
