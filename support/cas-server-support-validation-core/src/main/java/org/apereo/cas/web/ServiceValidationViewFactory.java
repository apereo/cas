package org.apereo.cas.web;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.validation.ValidationResponseType;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * This is {@link ServiceValidationViewFactory}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
public class ServiceValidationViewFactory {

    /**
     * The Registered views.
     */
    private final Map<String, Pair<View, View>> registeredViews = new HashMap<>(0);

    /**
     * Register view.
     *
     * @param type the type
     * @param view the view
     */
    public void registerView(final ServiceValidationViewTypes type, final Pair<View, View> view) {
        registerView(type.getType(), view);
    }

    /**
     * Register view.
     *
     * @param ownerClass the owner class
     * @param view       the view
     */
    public void registerView(final Class ownerClass, final Pair<View, View> view) {
        registerView(ownerClass.getSimpleName(), view);
    }

    /**
     * Register view.
     * Success and failures are handled by the same view instance.
     *
     * @param type the type
     * @param view the view
     */
    public void registerView(final ServiceValidationViewTypes type, final View view) {
        registerView(type.getType(), Pair.of(view, view));
    }

    /**
     * Register view.
     *
     * @param ownerClass the owner class
     * @param view       the view
     */
    public void registerView(final Class ownerClass, final View view) {
        registerView(ownerClass.getSimpleName(), Pair.of(view, view));
    }

    /**
     * Register view.
     *
     * @param type the type
     * @param view the view
     */
    public void registerView(final String type, final Pair<View, View> view) {
        registeredViews.put(type, view);
    }

    /**
     * Register view.
     *
     * @param type the type
     * @param view the view
     */
    public void registerView(final String type, final View view) {
        registeredViews.put(type, Pair.of(view, view));
    }

    /**
     * Gets view.
     *
     * @param type the type
     * @return the view
     */
    public Pair<View, View> getView(final ServiceValidationViewTypes type) {
        return getView(type.getType());
    }

    /**
     * Gets view.
     *
     * @param type the type
     * @return the view
     */
    public Pair<View, View> getView(final String type) {
        return registeredViews.get(type);
    }

    /**
     * Gets view.
     *
     * @param request    the request
     * @param isSuccess  the is success
     * @param service    the service
     * @param ownerClass the owner class
     * @return the view
     */
    public View getView(final HttpServletRequest request,
                        final boolean isSuccess,
                        final WebApplicationService service,
                        final Class ownerClass) {
        val type = getValidationResponseType(request, service);
        if (type == ValidationResponseType.JSON) {
            return getSingleInstanceView(ServiceValidationViewTypes.JSON);
        }
        return isSuccess
            ? getSuccessView(ownerClass.getSimpleName())
            : getFailureView(ownerClass.getSimpleName());
    }

    /**
     * Gets view.
     *
     * @param type the type
     * @return the view
     */
    public View getSingleInstanceView(final ServiceValidationViewTypes type) {
        return getView(type.getType()).getKey();
    }

    /**
     * Gets view.
     *
     * @param type the type
     * @return the view
     */
    public View getSuccessView(final String type) {
        return getView(type).getKey();
    }

    /**
     * Gets success view.
     *
     * @param type the type
     * @return the success view
     */
    public View getSuccessView(final Class type) {
        return getView(type.getSimpleName()).getKey();
    }

    /**
     * Gets view.
     *
     * @param type the type
     * @return the view
     */
    public View getFailureView(final String type) {
        return getView(type).getValue();
    }

    /**
     * Gets failure view.
     *
     * @param type the type
     * @return the failure view
     */
    public View getFailureView(final Class type) {
        return getView(type.getSimpleName()).getValue();
    }

    /**
     * Contains view .
     *
     * @param type the type
     * @return true/false
     */
    public boolean containsView(final ServiceValidationViewTypes type) {
        return containsView(type.getType());
    }

    /**
     * Contains view .
     *
     * @param type the type
     * @return true/false
     */
    public boolean containsView(final String type) {
        return registeredViews.containsKey(type);
    }

    /**
     * Gets model and view.
     *
     * @param request    the request
     * @param isSuccess  the is success
     * @param service    the service
     * @param ownerClass the owner class
     * @return the model and view
     */
    public ModelAndView getModelAndView(final HttpServletRequest request,
                                        final boolean isSuccess,
                                        final WebApplicationService service,
                                        final Class ownerClass) {
        val view = getView(request, isSuccess, service, ownerClass);
        return new ModelAndView(view);
    }

    /**
     * Gets validation response type.
     *
     * @param request the request
     * @param service the service
     * @return the validation response type
     */
    private static ValidationResponseType getValidationResponseType(final HttpServletRequest request,
                                                                    final WebApplicationService service) {
        val format = request.getParameter(CasProtocolConstants.PARAMETER_FORMAT);
        final Function<String, ValidationResponseType> func = FunctionUtils.doIf(StringUtils::isNotBlank,
            t -> ValidationResponseType.valueOf(t.toUpperCase()),
            f -> service != null ? service.getFormat() : ValidationResponseType.XML);
        return func.apply(format);
    }
}
