package org.apereo.cas.web.view;

import org.apereo.cas.util.LoggingUtils;

import com.samskivert.mustache.Mustache;
import lombok.Setter;
import lombok.val;
import org.springframework.boot.web.servlet.view.MustacheView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * This is {@link CasMustacheView}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Setter
public class CasMustacheView extends MustacheView {
    protected Mustache.Compiler compiler;

    @Override
    protected void renderMergedTemplateModel(final Map<String, Object> model, final HttpServletRequest request,
                                             final HttpServletResponse response) throws Exception {
        val resource = getApplicationContext().getResource(getUrl());
        try (val reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8);
             val writer = new StringWriter()) {
            val template = compiler.compile(reader);
            template.execute(model, writer);
            LoggingUtils.protocolMessage("CAS Validation Response", Map.of(), writer.toString());
            response.getWriter().write(writer.toString());
        }
    }
}
