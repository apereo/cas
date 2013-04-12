package org.jasig.cas.support.oauth.endpoint;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.cas.support.oauth.OAuthConstants;
import org.springframework.context.expression.MapAccessor;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.PropertyPlaceholderHelper;
import org.springframework.util.PropertyPlaceholderHelper.PlaceholderResolver;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;

@Controller
//@RequestMapping("/oauth2.0")
@SessionAttributes("authorizationRequest")
public class CustomUserApprovalEndpoint {
    
//    private static final Logger log = LoggerFactory.getLogger(CustomUserApprovalEndpoint.class);

    @RequestMapping("/confirm_access")
    public ModelAndView getAccessConfirmation(@RequestParam String client_id, Model model) 
            throws Exception {
        model.addAttribute("serviceName", client_id);
        return new ModelAndView(OAuthConstants.CONFIRM_VIEW, model.asMap());
    }

    @RequestMapping("/error")
    public ModelAndView handleError(HttpServletRequest request) {
      Map<String, Object> model = new HashMap<String, Object>();
      model.put("error", request.getAttribute("error"));
      return new ModelAndView(new SpelView(ERROR), model);
    }
    
    // Shamelessly lifted from spring security ouath's WhitelabelApprovalEndpoint
    // Begin copy pasta
    /**
     * Simple String template renderer.
     *
     */
    private static class SpelView implements View {

      private final String template;

      private final SpelExpressionParser parser = new SpelExpressionParser();

      private final StandardEvaluationContext context = new StandardEvaluationContext();

      private PropertyPlaceholderHelper helper;

      private PlaceholderResolver resolver;

      public SpelView(String template) {
        this.template = template;
        this.context.addPropertyAccessor(new MapAccessor());
        this.helper = new PropertyPlaceholderHelper("${", "}");
        this.resolver = new PlaceholderResolver() {
          public String resolvePlaceholder(String name) {
            Expression expression = parser.parseExpression(name);
            Object value = expression.getValue(context);
            return value==null ? null : value.toString();
          }
        };
      }

      public String getContentType() {
        return "text/html";
      }

      public void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response)
          throws Exception {
        Map<String, Object> map = new HashMap<String, Object>(model);
        map.put("path", (Object) request.getContextPath() + request.getServletPath());
        context.setRootObject(map);
        String result = helper.replacePlaceholders(template, resolver);
        response.getWriter().append(result);
      }

    }

//    private static String APPROVAL = "<html><body><h1>OAuth Approval</h1>"
//        + "<p>Do you authorize '${authorizationRequest.clientId}' to access your protected resources?</p>"
//        + "<form id='confirmationForm' name='confirmationForm' action='${path}/authorize' method='post'><input name='user_oauth_approval' value='true' type='hidden'/><label><input name='authorize' value='Authorize' type='submit'></label></form>"
//        + "<form id='denialForm' name='denialForm' action='${path}/authorize' method='post'><input name='user_oauth_approval' value='false' type='hidden'/><label><input name='deny' value='Deny' type='submit'></label></form>"
//        + "</body></html>";

    private static String ERROR = "<html><body><h1>OAuth Error</h1><p>${error.summary}</p></body></html>";
    
    // End of copy pasta

}
