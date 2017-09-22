package org.apereo.cas.configuration.model.core.web.security;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link HttpWebRequestProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */

public class HttpWebRequestProperties {

    private boolean allowMultiValueParameters;
    private String onlyPostParams = "username,password";
    private String paramsToCheck =
            "ticket,service,renew,gateway,warn,method,target,SAMLart,"
                    + "pgtUrl,pgt,pgtId,pgtIou,targetService,entityId,token";

    private Web web = new Web();
    private Header header = new Header();
    private Cors cors = new Cors();

    public boolean isAllowMultiValueParameters() {
        return allowMultiValueParameters;
    }

    public void setAllowMultiValueParameters(final boolean allowMultiValueParameters) {
        this.allowMultiValueParameters = allowMultiValueParameters;
    }

    public String getOnlyPostParams() {
        return onlyPostParams;
    }

    public void setOnlyPostParams(final String onlyPostParams) {
        this.onlyPostParams = onlyPostParams;
    }

    public String getParamsToCheck() {
        return paramsToCheck;
    }

    public void setParamsToCheck(final String paramsToCheck) {
        this.paramsToCheck = paramsToCheck;
    }

    public Cors getCors() {
        return cors;
    }

    public void setCors(final Cors cors) {
        this.cors = cors;
    }

    public Web getWeb() {
        return web;
    }

    public void setWeb(final Web web) {
        this.web = web;
    }

    public Header getHeader() {
        return header;
    }

    public void setHeader(final Header header) {
        this.header = header;
    }

    public static class Web {
        private String encoding = StandardCharsets.UTF_8.name();
        private boolean forceEncoding = true;

        public String getEncoding() {
            return encoding;
        }

        public void setEncoding(final String encoding) {
            this.encoding = encoding;
        }

        public boolean isForceEncoding() {
            return forceEncoding;
        }

        public void setForceEncoding(final boolean forceEncoding) {
            this.forceEncoding = forceEncoding;
        }
    }

    public static class Cors {
        private boolean enabled;
        private boolean allowCredentials = true;
        private List<String> allowOrigins = new ArrayList<>();
        private List<String> allowMethods = new ArrayList<>();
        private List<String> allowHeaders = new ArrayList<>();
        private long maxAge = 3_600;
        private List<String> exposedHeaders = new ArrayList<>();

        public Cors() {
            this.allowMethods.add("*");
            this.allowHeaders.add("*");
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(final boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isAllowCredentials() {
            return allowCredentials;
        }

        public void setAllowCredentials(final boolean allowCredentials) {
            this.allowCredentials = allowCredentials;
        }

        public List<String> getAllowOrigins() {
            return allowOrigins;
        }

        public void setAllowOrigins(final List<String> allowOrigins) {
            this.allowOrigins = allowOrigins;
        }

        public List<String> getAllowMethods() {
            return allowMethods;
        }

        public void setAllowMethods(final List<String> allowMethods) {
            this.allowMethods = allowMethods;
        }

        public List<String> getAllowHeaders() {
            return allowHeaders;
        }

        public void setAllowHeaders(final List<String> allowHeaders) {
            this.allowHeaders = allowHeaders;
        }

        public long getMaxAge() {
            return maxAge;
        }

        public void setMaxAge(final long maxAge) {
            this.maxAge = maxAge;
        }

        public List<String> getExposedHeaders() {
            return exposedHeaders;
        }

        public void setExposedHeaders(final List<String> exposedHeaders) {
            this.exposedHeaders = exposedHeaders;
        }
    }

    public static class Header {
        private boolean cache = true;
        private boolean hsts = true;
        private boolean xframe = true;
        private boolean xcontent = true;
        private boolean xss = true;

        public boolean isCache() {
            return cache;
        }

        public void setCache(final boolean cache) {
            this.cache = cache;
        }

        public boolean isHsts() {
            return hsts;
        }

        public void setHsts(final boolean hsts) {
            this.hsts = hsts;
        }

        public boolean isXframe() {
            return xframe;
        }

        public void setXframe(final boolean xframe) {
            this.xframe = xframe;
        }

        public boolean isXcontent() {
            return xcontent;
        }

        public void setXcontent(final boolean xcontent) {
            this.xcontent = xcontent;
        }

        public boolean isXss() {
            return xss;
        }

        public void setXss(final boolean xss) {
            this.xss = xss;
        }
    }

}


    
