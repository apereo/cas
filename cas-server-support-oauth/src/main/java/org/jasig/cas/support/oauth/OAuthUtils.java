package org.jasig.cas.support.oauth;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

/**
 * Some usefull methods.
 * 
 * @author Jerome Leleu
 */
public class OAuthUtils {
    
    private static final Logger logger = LoggerFactory.getLogger(OAuthUtils.class);
    
    public static ModelAndView writeXmlError(HttpServletResponse response, String error) {
        return writeXml(response, "<error>" + error + "</error>");
    }
    
    public static ModelAndView writeXml(HttpServletResponse response, String xml) {
        return writeText(response, "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>" + xml);
    }
    
    public static ModelAndView writeTextError(HttpServletResponse response, String error) {
        return OAuthUtils.writeText(response, "error=" + error);
    }
    
    public static ModelAndView writeText(HttpServletResponse response, String text) {
        PrintWriter printWriter;
        try {
            printWriter = response.getWriter();
            printWriter.print(text);
        } catch (IOException e) {
            logger.warn("Failed to write to response", e);
        }
        return null;
    }
    
    public static ModelAndView redirectToError(String url, String error) {
        if (StringUtils.isBlank(url)) {
            url = "/";
        }
        return OAuthUtils.redirectTo(OAuthUtils.addParameter(url, "error", error));
    }
    
    public static ModelAndView redirectTo(String url) {
        return new ModelAndView(new RedirectView(url));
    }
    
    public static String addParameter(String url, String name, String value) {
        if (url.indexOf("?") >= 0) {
            return url + "&" + name + "=" + URLEncoder.encode(value);
        } else {
            return url + "?" + name + "=" + URLEncoder.encode(value);
        }
    }
}
