package org.jasig.cas.web.support;

import org.jasig.cas.authentication.Credential;
import org.jasig.cas.authentication.UsernamePasswordCredential;
import org.jasig.cas.util.HttpResponse;
import org.jasig.cas.util.HttpTemplate;
import org.jasig.cas.util.ParamBodyWriteCallback;
import org.jasig.cas.util.StringResponseReadCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;

/**
 * Created by ruaa on 2015. 11. 4..
 */
public class NhnLegacyAuthenticationMediator {
    private RequestContext context;
    private Credential credential;

    private String legacyAuthUrl;
    private String legacyTargetUrl;
    private String legacyAgentName;
    private boolean isSelfSigned;

    /** Logger instance. **/
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    public NhnLegacyAuthenticationMediator(RequestContext context, Credential credential) {
        this.context = context;
        this.credential = credential;
    }

    public void setLegacyInfo(String legacyAuthUri, String legacyTargetUri, String legacyAgentName, boolean isSelfSigned){
        this.legacyAuthUrl = legacyAuthUri;
        this.legacyTargetUrl = legacyTargetUri;
        this.legacyAgentName = legacyAgentName;
        this.isSelfSigned = isSelfSigned;
    }

    public void run() {
        try {
            HttpResponse response = getLegacySsoCookie();
            setLegacySsoCookie(response.getCookieList());
        } catch (Exception e) {
            logger.error("Siteminder 연동 중 에러");
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * for Legacy SSO Server (Siteminder)
     *
     * @since Nhn Customizing
     */
    private HttpResponse getLegacySsoCookie() throws Exception {
        HashMap<String, String> header = new HashMap<>();
        header.put("Content-Type", "application/x-www-form-urlencoded");

        HashMap<String, String> body = new HashMap<>();
//        body.put("target", "http://sso.nhnent.com/index.nhn");
//        body.put("smauthreason", "0");
//        body.put("smagentname", "sso.nhnent.com");
//        body.put("USER", "ne11255");
//        body.put("PASSWORD", "RnaRnfk*0");

        body.put("target", this.legacyTargetUrl);
        body.put("smauthreason", "0");
        body.put("smagentname", this.legacyAgentName);
        body.put("USER", credential.getId());
        body.put("PASSWORD", ((UsernamePasswordCredential)credential).getPassword());
//        body.put("PASSWORD", "RnaRnfk*0");

        HttpTemplate httpTemplate = new HttpTemplate();
        HttpResponse<String> response = httpTemplate.request(this.legacyAuthUrl
                , "POST"
                , header
                , new ParamBodyWriteCallback()
                , new StringResponseReadCallback()
                , body
                , isSelfSigned);

        logger.info(response.getResponseBody());

        return response;
    }

    private void setLegacySsoCookie(List<Cookie> cookieList) {
        HttpServletResponse response = WebUtils.getHttpServletResponse(this.context);

        for (Cookie cookie : cookieList) {
            response.addCookie(cookie);
        }
    }
}
