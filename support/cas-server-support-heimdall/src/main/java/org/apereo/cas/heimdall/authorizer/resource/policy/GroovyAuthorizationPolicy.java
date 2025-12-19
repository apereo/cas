package org.apereo.cas.heimdall.authorizer.resource.policy;

import module java.base;
import org.apereo.cas.heimdall.AuthorizationRequest;
import org.apereo.cas.heimdall.authorizer.AuthorizationResult;
import org.apereo.cas.heimdall.authorizer.resource.AuthorizableResource;
import org.apereo.cas.util.DigestUtils;
import org.apereo.cas.util.spring.ApplicationContextProvider;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 * This is {@link GroovyAuthorizationPolicy}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class GroovyAuthorizationPolicy implements ResourceAuthorizationPolicy {
    @Serial
    private static final long serialVersionUID = -1344481042826672523L;

    private String script;

    @Override
    public AuthorizationResult evaluate(final AuthorizableResource resource, final AuthorizationRequest request) throws Throwable {
        val applicationContext = ApplicationContextProvider.getApplicationContext();
        val cacheManager = ApplicationContextProvider.getScriptResourceCacheManager().orElseThrow();
        val key = DigestUtils.sha256(script);
        val scriptToExec = cacheManager.resolveScriptableResource(script, key);
        val args = Map.of("resource", resource, "request", request, "applicationContext", applicationContext, "logger", LOGGER);
        scriptToExec.setBinding(args);
        return scriptToExec.execute(args.values().toArray(), AuthorizationResult.class);
    }
}
