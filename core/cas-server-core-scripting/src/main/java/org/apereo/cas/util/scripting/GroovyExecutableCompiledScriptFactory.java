package org.apereo.cas.util.scripting;

import groovy.text.GStringTemplateEngine;
import lombok.val;
import org.springframework.core.io.Resource;
import java.io.File;
import java.util.Map;

/**
 * This is {@link GroovyExecutableCompiledScriptFactory}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
public class GroovyExecutableCompiledScriptFactory implements ExecutableCompiledScriptFactory {
    @Override
    public ExecutableCompiledScript fromResource(final Resource resource, final boolean watchResource) {
        return new WatchableGroovyScriptResource(resource);
    }

    @Override
    public String createTemplate(final String contents, final Map<String, ?> templateParams) throws Exception{
        val engine = new GStringTemplateEngine();
        val template = engine.createTemplate(contents).make(templateParams);
        return template.toString();
    }

    @Override
    public String createTemplate(final File templateFile, final Map<String, ?> templateParams) throws Exception {
        val engine = new GStringTemplateEngine();
        val template = engine.createTemplate(templateFile).make(templateParams);
        return template.toString();
    }
}
