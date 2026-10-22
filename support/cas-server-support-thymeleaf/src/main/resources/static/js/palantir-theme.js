/**
 * Light and dark themes for Palantir.
 *
 * The page carries the resolved theme on the root element as data-theme, light or dark, and the
 * operator's choice as data-theme-preference: system, light or dark. System follows the
 * operating system's color scheme and keeps following it while the page is open. A small inline
 * script in the page head applies the stored choice before the first paint, so a dark dashboard
 * never flashes light; this class takes over once the scripts have loaded.
 *
 * Everything the stylesheet can reach switches through CSS custom properties. What it cannot reach
 * is told here: Chart.js reads its default text and grid colors from the theme, and other code can
 * listen for the palantir:theme-changed event on window.
 */
class PalantirTheme {

    static STORAGE_KEY = "PalantirTheme";

    static CHANGED_EVENT = "palantir:theme-changed";

    static PREFERENCES = Object.freeze(["system", "light", "dark"]);

    static mediaQuery = null;

    /**
     * The stored preference.
     *
     * @returns {string} system, light or dark; system when nothing valid is stored
     */
    static preference() {
        try {
            const stored = window.localStorage.getItem(PalantirTheme.STORAGE_KEY);
            return PalantirTheme.PREFERENCES.includes(stored) ? stored : "system";
        } catch (error) {
            return "system";
        }
    }

    /**
     * Resolve a preference to the theme that is actually shown.
     *
     * @param preference system, light or dark
     * @returns {string} light or dark
     */
    static resolve(preference) {
        if (preference === "light" || preference === "dark") {
            return preference;
        }
        return window.matchMedia && window.matchMedia("(prefers-color-scheme: dark)").matches ? "dark" : "light";
    }

    /**
     * The theme on screen.
     *
     * @returns {string} light or dark
     */
    static current() {
        return document.documentElement.getAttribute("data-theme") === "dark" ? "dark" : "light";
    }

    /**
     * Apply and remember a preference.
     *
     * @param preference system, light or dark
     */
    static apply(preference) {
        const chosen = PalantirTheme.PREFERENCES.includes(preference) ? preference : "system";
        try {
            window.localStorage.setItem(PalantirTheme.STORAGE_KEY, chosen);
        } catch (error) {
            console.debug("Palantir could not persist the theme preference", error);
        }
        PalantirTheme.render(chosen);
    }

    /**
     * Put a preference on the page without storing it.
     *
     * @param preference system, light or dark
     */
    static render(preference) {
        const root = document.documentElement;
        const theme = PalantirTheme.resolve(preference);
        const changed = root.getAttribute("data-theme") !== theme;
        root.setAttribute("data-theme", theme);
        root.setAttribute("data-theme-preference", preference);
        PalantirTheme.updateToggle();
        PalantirTheme.updateCharts();
        if (changed) {
            window.dispatchEvent(new CustomEvent(PalantirTheme.CHANGED_EVENT, {detail: {theme: theme}}));
        }
    }

    /**
     * Switch between light and dark, leaving the system preference behind.
     */
    static toggle() {
        PalantirTheme.apply(PalantirTheme.current() === "dark" ? "light" : "dark");
    }

    /**
     * Name the toggle after what pressing it does, and show the icon of the theme it switches to.
     */
    static updateToggle() {
        const toggle = document.getElementById("palantirThemeToggle");
        if (!toggle) {
            return;
        }
        const dark = PalantirTheme.current() === "dark";
        const label = dark ? "Switch to light theme" : "Switch to dark theme";
        toggle.setAttribute("aria-label", label);
        toggle.setAttribute("title", label);
        const icon = toggle.querySelector(".mdi");
        if (icon) {
            icon.classList.toggle("mdi-weather-night", !dark);
            icon.classList.toggle("mdi-white-balance-sunny", dark);
        }
    }

    /**
     * Read a theme color from the stylesheet.
     *
     * @param name the custom property name, with its leading dashes
     * @param fallback the color to use when the property is not defined
     * @returns {string} the computed color
     */
    static color(name, fallback) {
        const value = getComputedStyle(document.documentElement).getPropertyValue(name).trim();
        return value || fallback;
    }

    /**
     * Point Chart.js at the theme's text and grid colors and redraw the charts already on screen.
     *
     * The grid and axis lines are themed through the scale defaults rather than the global
     * {@code Chart.defaults.borderColor}: Chart.js only fills in its dataset palette while the global
     * border and background colors are left at their stock values, so changing the global border color
     * leaves every chart that relies on that palette with gray, nearly invisible bars.
     */
    static updateCharts() {
        if (typeof Chart === "undefined") {
            return;
        }
        const grid = PalantirTheme.color("--palantir-chart-grid", "rgba(0, 0, 0, .1)");
        Chart.defaults.color = PalantirTheme.color("--palantir-text-muted", "#52606d");
        Chart.defaults.scale.grid.color = grid;
        Chart.defaults.scale.border.color = grid;
        Object.values(Chart.instances || {}).forEach(chart => {
            try {
                chart.update("none");
            } catch (error) {
                console.debug("Palantir could not redraw a chart for the new theme", error);
            }
        });
    }

    /**
     * Wire the toggle and follow the operating system while the preference is system.
     */
    static initialize() {
        PalantirTheme.render(PalantirTheme.preference());
        $(document).off("click.palantirTheme").on("click.palantirTheme", "#palantirThemeToggle", event => {
            event.preventDefault();
            PalantirTheme.toggle();
        });
        if (window.matchMedia && PalantirTheme.mediaQuery === null) {
            PalantirTheme.mediaQuery = window.matchMedia("(prefers-color-scheme: dark)");
            PalantirTheme.mediaQuery.addEventListener("change", () => {
                if (PalantirTheme.preference() === "system") {
                    PalantirTheme.render("system");
                }
            });
        }
    }
}
