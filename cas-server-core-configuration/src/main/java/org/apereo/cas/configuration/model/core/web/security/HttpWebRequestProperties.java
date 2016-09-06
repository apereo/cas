package org.apereo.cas.configuration.model.core.web.security;

import java.nio.charset.StandardCharsets;

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

    public static class Header {
        private boolean cache;
        private boolean hsts;
        private boolean xframe;
        private boolean xcontent;
        private boolean xss;

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


    
