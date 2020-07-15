package org.apereo.cas.support.oauth.web.audit;

import lombok.val;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apereo.inspektr.audit.spi.support.ReturnValueAsStringResourceResolver;
import org.aspectj.lang.JoinPoint;
import org.springframework.web.servlet.ModelAndView;


/**
 * The {@link OAuth20CodeResponseAuditResourceResolver} for audit advice.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
public class OAuth20CodeResponseAuditResourceResolver extends ReturnValueAsStringResourceResolver {

    @Override
    public String[] resolveFrom(final JoinPoint auditableTarget, final Object retval) {
        val model = (ModelAndView) retval;
        val builder = new ToStringBuilder(this, ToStringStyle.NO_CLASS_NAME_STYLE);
        model.getModel().forEach(builder::append);
        val result = builder.toString();
        return new String[]{result};
    }
}
