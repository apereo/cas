package org.apereo.cas.services;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.scripting.ExecutableCompiledGroovyScript;
import org.apereo.cas.util.scripting.GroovyShellScript;
import org.apereo.cas.util.scripting.ScriptingUtils;
import org.apereo.cas.util.scripting.WatchableGroovyScriptResource;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.PostLoad;
import javax.persistence.Transient;

/**
 * Resolves the username for the service to be the default principal id.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
@Slf4j
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
public class GroovyRegisteredServiceUsernameProvider extends BaseRegisteredServiceUsernameAttributeProvider {

    private static final long serialVersionUID = 5823989148794052951L;

    private String groovyScript;

    @JsonIgnore
    @Transient
    @org.springframework.data.annotation.Transient
    private transient ExecutableCompiledGroovyScript executableScript;

    @JsonCreator
    public GroovyRegisteredServiceUsernameProvider(@JsonProperty("groovyScript") final String script) {
        this.groovyScript = script;
    }

    @PostLoad
    @SneakyThrows
    private void initializeWatchableScriptIfNeeded() {
        if (this.executableScript == null) {
            val matcherInline = ScriptingUtils.getMatcherForInlineGroovyScript(groovyScript);
            val matcherFile = ScriptingUtils.getMatcherForExternalGroovyScript(groovyScript);

            if (matcherFile.find()) {
                val script = SpringExpressionLanguageValueResolver.getInstance().resolve(matcherFile.group());
                val resource = ResourceUtils.getRawResourceFrom(script);
                this.executableScript = new WatchableGroovyScriptResource(resource);
            } else if (matcherInline.find()) {
                this.executableScript = new GroovyShellScript(matcherInline.group(1));
            }
        }
    }

    @Override
    public String resolveUsernameInternal(final Principal principal, final Service service, final RegisteredService registeredService) {
        if (StringUtils.isNotBlank(this.groovyScript)) {
            initializeWatchableScriptIfNeeded();
            val result = getGroovyAttributeValue(principal, service);
            if (result != null) {
                LOGGER.debug("Found username [{}] from script", result);
                return result.toString();
            }
        }
        LOGGER.warn("Groovy script [{}] is not valid. CAS will switch to use the default principal identifier [{}]", this.groovyScript, principal.getId());
        return principal.getId();
    }

    private Object getGroovyAttributeValue(final Principal principal, final Service service) {
        val args = CollectionUtils.<String, Object>wrap("attributes", principal.getAttributes(),
            "id", principal.getId(),
            "service", service,
            "logger", LOGGER);
        executableScript.setBinding(args);
        return executableScript.execute(args.values().toArray(), Object.class);
    }

}
