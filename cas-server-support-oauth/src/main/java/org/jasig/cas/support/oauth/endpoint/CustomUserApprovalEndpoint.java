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
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.View;

@Controller
@SessionAttributes("authorizationRequest")
public class CustomUserApprovalEndpoint {

    @RequestMapping("/confirm_access")
    public String getAccessConfirmation() {
        return OAuthConstants.CONFIRM_VIEW;
    }

    @RequestMapping("/error")
    public View handleError(HttpServletRequest request, Model model) {
      model.addAttribute("error", request.getAttribute("error"));
      return new SpelView(ERROR);
    }
    
    @ModelAttribute("authorizePath") 
    public String getAuthorizePath() {
        // TODO: This is hard-coded to what we want it to be, but it should 
        // really grab it from the oauth2HandlerMapping
        return "authorize";
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
    
    private static String ERROR = "<html><body><h1>OAuth Error</h1><p>${error.summary}</p></body></html>";
    
    // End of copy pasta

}
