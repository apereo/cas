package org.apereo.cas.web.view;

import module java.base;
import org.jspecify.annotations.NonNull;
import org.springframework.http.MediaType;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.View;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This is {@link DynamicHtmlView}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public record DynamicHtmlView(String html) implements View {
    @Override
    public String getContentType() {
        return MediaType.TEXT_HTML_VALUE;
    }

    @Override
    public void render(final Map<String, ?> model,
                       @NonNull
                       final HttpServletRequest request,
                       final HttpServletResponse response) throws Exception {
        response.setContentType(this.getContentType());
        if (StringUtils.hasText(this.html)) {
            FileCopyUtils.copy(this.html, response.getWriter());
        }
    }
}
