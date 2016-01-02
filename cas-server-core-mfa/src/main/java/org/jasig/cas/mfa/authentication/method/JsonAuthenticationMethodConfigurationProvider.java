package org.jasig.cas.mfa.authentication.method;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

/**
 * Loads authentication methods and their rank from an external configuration file
 * that is expected to be JSON.
 *
 * Example configuration:
 * <pre>
 * [ {
 * "rank" : 1,
 * "name" : "duo_two_factor"
 * }, {
 * "rank" : 2,
 * "name" : "strong_two_factor"
 * }, {
 * "rank" : 3,
 * "name" : "sample_two_factor"
 * } ]
 * </pre>
 *
 * @author Misagh Moayyed
 * @since 4.3
 */
@Component("jsonAuthenticationMethodConfigurationProvider")
public final class JsonAuthenticationMethodConfigurationProvider implements AuthenticationMethodConfigurationProvider {

    private final Set<AuthenticationMethod> authnMethods;

    /**
     * Instantiates a new Authentication method loader.
     * Loads supported authentication methods from
     * the specified resource.
     *
     * @param configuration the configuration
     * @throws IOException the iO exception
     */
    @Autowired(required=false)
    public JsonAuthenticationMethodConfigurationProvider(@Qualifier("jsonAuthenticationMethodResource") final Resource configuration) throws
            IOException {
        this.authnMethods = new TreeSet<>();
        final ObjectMapper objectMapper = new ObjectMapper();
        final String json = FileUtils.readFileToString(configuration.getFile());
        final Set<?> set = objectMapper.readValue(json, Set.class);
        for (final Iterator<?> it = set.iterator(); it.hasNext();) {
            final AuthenticationMethod method = objectMapper.convertValue(it.next(), AuthenticationMethod.class);
            this.authnMethods.add(method);
        }
    }

    /**
     * Instantiates a new Authentication method loader.
     * Populates the supported authn methods with the given set.
     *
     * @param authnMethods the authn methods
     */
    public JsonAuthenticationMethodConfigurationProvider(final Set<AuthenticationMethod> authnMethods) {
        this.authnMethods = authnMethods;
    }

    /**
     * Instantiates a new Authentication method loader.
     */
    public JsonAuthenticationMethodConfigurationProvider() {
        this.authnMethods = new TreeSet<>();
    }


    @Override
    public boolean contains(final String name) {
        return get(name) != null;
    }

    @Override
    public AuthenticationMethod get(final String name) {
        for (final Iterator<AuthenticationMethod> it = this.authnMethods.iterator(); it.hasNext();) {
            final AuthenticationMethod f = it.next();
            if (f.getName().equals(name)) {
                return f;
            }
        }
        return null;
    }
}
