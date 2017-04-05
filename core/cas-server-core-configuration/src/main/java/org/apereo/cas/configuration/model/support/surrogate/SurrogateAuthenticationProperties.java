package org.apereo.cas.configuration.model.support.surrogate;

import org.apereo.cas.configuration.support.AbstractConfigProperties;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This is {@link SurrogateAuthenticationProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class SurrogateAuthenticationProperties {
    private String separator = "+";
    private Simple simple = new Simple();
    private Json json = new Json();
    private Ldap ldap = new Ldap();

    public Simple getSimple() {
        return simple;
    }

    public void setSimple(final Simple simple) {
        this.simple = simple;
    }

    public Json getJson() {
        return json;
    }

    public void setJson(final Json json) {
        this.json = json;
    }

    public Ldap getLdap() {
        return ldap;
    }

    public void setLdap(final Ldap ldap) {
        this.ldap = ldap;
    }

    public String getSeparator() {
        return separator;
    }

    public void setSeparator(final String separator) {
        this.separator = separator;
    }

    public static class Simple {
        private Map<String, String> surrogates = new LinkedHashMap<>();

        public Map<String, String> getSurrogates() {
            return surrogates;
        }

        public void setSurrogates(final Map<String, String> surrogates) {
            this.surrogates = surrogates;
        }
    }

    public static class Json extends AbstractConfigProperties {
    }

    public static class Ldap {

    }
}
