package org.apereo.cas.support.oauth.web.audit;

import lombok.val;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apereo.inspektr.audit.spi.support.ReturnValueAsStringResourceResolver;
import org.aspectj.lang.JoinPoint;
import org.springframework.web.servlet.ModelAndView;

import static org.apache.commons.lang3.builder.ToStringStyle.NO_CLASS_NAME_STYLE;

/**
 * The {@link AccessTokenResponseAuditResourceResolver} for audit advice.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
public class AccessTokenResponseAuditResourceResolver extends ReturnValueAsStringResourceResolver {

    @Override
    public String[] resolveFrom(final JoinPoint auditableTarget, final Object retval) {
        val model = (ModelAndView) retval;
        val builder = new ToStringBuilder(this, NO_CLASS_NAME_STYLE);
        model.getModel().forEach(builder::append);
        val result = builder.toString();
        return new String[]{result};
    }
}
