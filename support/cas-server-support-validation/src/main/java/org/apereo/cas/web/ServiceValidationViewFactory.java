package org.apereo.cas.web;

import groovy.util.logging.Slf4j;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.web.servlet.View;

import java.util.HashMap;
import java.util.Map;

/**
 * This is {@link ServiceValidationViewFactory}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
public class ServiceValidationViewFactory {

    /**
     * The official types of views
     * that can be rendered for validation events
     * to produce responses.
     */
    @RequiredArgsConstructor
    @Getter
    public enum ViewTypes {
        /**
         * JSON view.
         */
        JSON("json"),
        /**
         * JWT view.
         */
        JWT("jwt");

        private final String type;
    }

    private final Map<String, Pair<View, View>> registeredViews = new HashMap<>();

    /**
     * Register view.
     *
     * @param type the type
     * @param view the view
     */
    public void registerView(final ViewTypes type, final Pair<View, View> view) {
        registerView(type.getType(), view);
    }

    /**
     * Register view.
     * Success and failures are handled by the same view instance.
     *
     * @param type the type
     * @param view the view
     */
    public void registerView(final ViewTypes type, final View view) {
        registerView(type.getType(), Pair.of(view, view));
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
    public Pair<View, View> getView(final ViewTypes type) {
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
     * @param type the type
     * @return the view
     */
    public View getSingleInstanceView(final ViewTypes type) {
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
     * Gets view.
     *
     * @param type the type
     * @return the view
     */
    public View getFailureView(final String type) {
        return getView(type).getValue();
    }

    /**
     * Contains view .
     *
     * @param type the type
     * @return the boolean
     */
    public boolean containsView(final ViewTypes type) {
        return containsView(type.getType());
    }

    /**
     * Contains view .
     *
     * @param type the type
     * @return the boolean
     */
    public boolean containsView(final String type) {
        return registeredViews.containsKey(type);
    }
}
