package org.apereo.cas.support.openid.web.mvc;

import org.apereo.cas.configuration.CasConfigurationProperties;

import lombok.val;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletResponse;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

/**
 * This is {@link YadisController}.
 *
 * @author Misagh Moayyed
 * @deprecated 6.2
 * @since 5.1.0
 */
@Controller("yadisController")
@Deprecated(since = "6.2.0")
public class YadisController {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ResourceLoader resourceLoader;

    /**
     * Generates the Yadis XML snippet.
     *
     * @param response the response
     * @throws Exception the exception
     */
    @GetMapping(path = "/yadis.xml")
    public void yadis(final HttpServletResponse response) throws Exception {
        val template = this.resourceLoader.getResource("classpath:/yadis.template");
        try (val writer = new StringWriter()) {
            IOUtils.copy(template.getInputStream(), writer, StandardCharsets.UTF_8);
            val yadis = writer.toString().replace("$casLoginUrl", casProperties.getServer().getLoginUrl());
            response.setContentType("application/xrds+xml");
            val respWriter = response.getWriter();
            respWriter.write(yadis);
            respWriter.flush();
        }
    }
}
