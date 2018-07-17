package org.apereo.cas.services;

import org.apereo.cas.util.RegexUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Implementation of the {@link ServicesManager} interface that organizes services by domain into
 * a hash for quicker lookup.
 *
 * @author Travis Schmidt
 * @since 5.2.0
 */
@Slf4j
public class DomainServicesManager extends AbstractServicesManager {


    private static final long serialVersionUID = -8581398063126547772L;

    private static final String DEFAULT_DOMAIN_NAME = "default";

    private final Map<String, TreeSet<RegisteredService>> domains = new ConcurrentHashMap<>();

    /**
     * This regular expression is used to strip the domain form the serviceId that is set in
     * the Service and also passed as the service parameter to the login endpoint.
     */
    private final Pattern domainExtractor = RegexUtils.createPattern("^\\^?https?\\??://(.*?)(?:[(]?[:/]|$)");
    private final Pattern domainPattern = RegexUtils.createPattern("^[a-z0-9-.]*$");

    public DomainServicesManager(final ServiceRegistry serviceRegistry, final ApplicationEventPublisher eventPublisher) {
        super(serviceRegistry, eventPublisher);
    }

    @Override
    protected void deleteInternal(final RegisteredService service) {
        val domain = extractDomain(service.getServiceId());
        this.domains.get(domain).remove(service);
    }

    @Override
    protected Collection<RegisteredService> getCandidateServicesToMatch(final String serviceId) {
        val mappedDomain = StringUtils.isNotBlank(serviceId) ? extractDomain(serviceId) : StringUtils.EMPTY;
        LOGGER.debug("Domain mapped to the service identifier is [{}]", mappedDomain);

        val domain = domains.containsKey(mappedDomain) ? mappedDomain : DEFAULT_DOMAIN_NAME;
        LOGGER.debug("Looking up services under domain [{}] for service identifier [{}]", domain, serviceId);

        val registeredServices = getServicesForDomain(domain);
        if (registeredServices == null || registeredServices.isEmpty()) {
            LOGGER.debug("No services could be located for domain [{}]", domain);
            return new ArrayList<>(0);
        }
        return registeredServices;
    }

    @Override
    protected void saveInternal(final RegisteredService service) {
        addToDomain(service, this.domains);
    }

    @Override
    protected void loadInternal() {
        val localDomains = new ConcurrentHashMap<String, TreeSet<RegisteredService>>();
        getAllServices().forEach(r -> addToDomain(r, localDomains));
        this.domains.clear();
        this.domains.putAll(localDomains);
    }

    @Override
    public List<String> getDomains() {
        return this.domains.keySet().stream().sorted().collect(Collectors.toList());
    }

    @Override
    public Collection<RegisteredService> getServicesForDomain(final String domain) {
        return this.domains.containsKey(domain) ? this.domains.get(domain) : new ArrayList<>();
    }


    private String extractDomain(final String service) {
        val extractor = this.domainExtractor.matcher(service.toLowerCase());
        return extractor.lookingAt() ? validateDomain(extractor.group(1)) : "default";
    }

    private String validateDomain(final String providedDomain) {
        val domain = StringUtils.remove(providedDomain, "\\");
        val match = domainPattern.matcher(StringUtils.remove(domain, "\\"));
        return match.matches() ? domain : "default";
    }

    private void addToDomain(final RegisteredService r, final Map<String, TreeSet<RegisteredService>> map) {
        val domain = extractDomain(r.getServiceId());
        val services = map.containsKey(domain)
            ? map.get(domain)
            : new TreeSet<RegisteredService>();
        LOGGER.debug("Added service [{}] mapped to domain definition [{}]", r, domain);
        services.add(r);
        map.put(domain, services);
    }
}
