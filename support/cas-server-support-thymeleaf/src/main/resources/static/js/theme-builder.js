/*
 * Theme builder: edits theme properties and the variables custom.css controls, previews every change on the current
 * page and exports a theme properties file and a custom.css file. Nothing is sent to the server; the draft lives in
 * session storage for the current tab, per theme.
 *
 * Model (window.casThemeBuilderModel, rendered by fragments/themebuilder.html): the theme name and, for each property
 * declared in cas-theme-default.properties, its name, default value, example, description and current value.
 */
(() => {
    "use strict";

    const MARK_START = "/* cas-theme-builder:start */";
    const MARK_END = "/* cas-theme-builder:end */";
    const DEFAULT_THEME = "cas-theme-default";
    const DEFAULT_TITLE = "CAS - Central Authentication Service";
    const DEFAULT_LOGO = "/images/cas-logo.png";
    const DEFAULT_FAVICON = "/favicon.ico";
    const CUSTOM_CSS_FILE = "/css/custom.css";

    const GROUPS = [
        {key: "branding", title: "Branding", pattern: /^cas\.(theme\.|logo\.|favicon\.|hero-banner\.)/},
        {key: "layout", title: "Layout", pattern: /^cas\.main-content\./},
        {key: "header", title: "Header", pattern: /^cas\.(drawer-menu|notifications-menu|theme-builder|locale-selection)\./},
        {key: "login", title: "Login", pattern: /^cas\.(login-form|pm-links|public-workstation|warn-on-redirect|login-side-panel|login-providers|service-ui|successful-login)\./},
        {key: "mfa", title: "Multifactor", pattern: /^cas\.(mfa|webauthn|gauth|qr-code)\./},
        {key: "consent", title: "Consent", pattern: /^cas\.consent\./},
        {key: "errors", title: "Errors", pattern: /^cas\.error\./},
        {key: "footer", title: "Footer", pattern: /^cas\.footer\./},
        {key: "resources", title: "Resources", pattern: /^cas\.(standard|css|js|browser-storage)\./},
        {key: "other", title: "Other", pattern: /./}
    ];

    const FONTS = [
        {label: "Ubuntu (default)", value: "\"Ubuntu\", \"Open Sans\", Roboto, Arial, sans-serif"},
        {label: "System UI", value: "system-ui, -apple-system, \"Segoe UI\", Roboto, \"Helvetica Neue\", Arial, sans-serif"},
        {label: "Open Sans", value: "\"Open Sans\", Roboto, Arial, sans-serif"},
        {label: "Roboto", value: "Roboto, Arial, sans-serif"},
        {label: "Helvetica / Arial", value: "\"Helvetica Neue\", Helvetica, Arial, sans-serif"},
        {label: "Georgia (serif)", value: "Georgia, \"Times New Roman\", serif"},
        {label: "Monospace", value: "ui-monospace, SFMono-Regular, Menlo, Consolas, monospace"}
    ];

    const VALUE_TOGGLES = new Set(["cas.hero-banner.file", "cas.pm-links.forgot-password.url"]);

    const CHOICES = {
        "cas.main-content.position": ["topleft", "topcenter", "topright", "middleleft", "middlecenter", "middleright",
            "bottomleft", "bottomcenter", "bottomright"],
        "cas.login-side-panel.position": ["start", "end"],
        "cas.login-providers.position": ["after", "before"],
        "cas.login-providers.layout": ["stacked", "grid", "inline"],
        "cas.webauthn.registration.recommended": ["passkey", "security-key", "none"],
        "cas.gauth.registration.scratch-codes": ["chips", "list"],
        "cas.mfa.selection.layout": ["list", "grid", "cards"],
        "cas.mfa.device-name.prefill": ["random", "empty", "username"],
        "cas.consent.layout": ["table", "list"]
    };

    const ACRONYMS = new Set(["pm", "ui", "ip", "css", "js"]);

    const ROOT = ":root";
    const UI = ".cas-ui";

    const STYLES = [
        {
            title: "Colors", items: [
                {key: "--cas-theme-primary", label: "Primary", help: "Header bar, footer, links and accents.", type: "color", selector: ROOT},
                {key: "--cas-theme-primary-light", label: "Link hover", type: "color", selector: ROOT},
                {key: "--cas-theme-button-bg", label: "Buttons", type: "color", selector: ROOT},
                {key: "--cas-theme-button-bg-hover", label: "Button hover and focus", type: "color", selector: ROOT},
                {key: "--cas-theme-secondary", label: "Secondary", type: "color", selector: ROOT},
                {key: "--cas-theme-success", label: "Success", type: "color", selector: ROOT},
                {key: "--mdc-theme-error", label: "Errors", type: "color", selector: ROOT},
                {key: "--cas-theme-danger", label: "Danger text", type: "color", selector: ROOT},
                {key: "--cas-theme-danger-bg", label: "Danger background", type: "color", selector: ROOT},
                {key: "--cas-theme-warning", label: "Warning text", type: "color", selector: ROOT},
                {key: "--cas-theme-warning-bg", label: "Warning background", type: "color", selector: ROOT},
                {key: "--mdc-theme-body", label: "Page background", help: "Shown where no background image covers the page.", type: "color", selector: ROOT}
            ]
        },
        {
            title: "Cards and fields", items: [
                {key: "--cas-ui-surface", label: "Surface", help: "Cards, fields and panels.", type: "color", selector: UI},
                {key: "--cas-ui-text", label: "Text", type: "color", selector: UI},
                {key: "--cas-ui-muted", label: "Muted text", type: "color", selector: UI},
                {key: "--cas-ui-border", label: "Field borders", type: "color", selector: UI},
                {key: "--cas-ui-subtle", label: "Subtle fill", type: "color", selector: UI},
                {key: "--cas-ui-radius", label: "Corner radius", type: "length", min: 0, max: 28, selector: UI},
                {key: "--cas-ui-field-height", label: "Field height", type: "length", min: 36, max: 72, selector: UI}
            ]
        },
        {
            title: "Typography", items: [
                {key: "--mdc-typography-font-family", label: "Font family", type: "font", selector: ROOT}
            ]
        },
        {
            title: "Header and footer", items: [
                {key: "header-background", label: "Header background", type: "color", selector: "#app-bar, #app-bar > nav",
                    property: "background-color", important: true, probe: "#app-bar > nav"},
                {key: "logo-height", label: "Logo height", type: "length", min: 24, max: 120, selector: "header > nav .cas-brand",
                    property: "height", probe: ".cas-brand"},
                {key: "footer-background", label: "Footer background", type: "color", selector: ".cas-footer",
                    property: "background-color", probe: ".cas-footer"},
                {key: "footer-text", label: "Footer text", type: "color", selector: ".cas-footer",
                    property: "color", probe: ".cas-footer"},
                {key: "footer-links", label: "Footer links", type: "color", selector: ".cas-footer a",
                    property: "color", probe: ".cas-footer a"}
            ]
        },
        {
            title: "Background", items: [
                {key: "background-image", label: "Background image", help: "An image URL, or none to remove the image.",
                    type: "image", selector: ".bgimage", property: "background-image", probe: ".bgimage"}
            ]
        }
    ];

    const STYLE_ITEMS = STYLES.flatMap((group) => group.items);

    const state = {
        model: {theme: DEFAULT_THEME, properties: []},
        properties: new Map(),
        initialProperties: new Map(),
        styles: new Map(),
        initialStyles: new Map(),
        baselineStyles: new Set(),
        extraCss: "",
        initialExtraCss: "",
        customCssLink: null,
        customCssLoaded: false,
        initialTitle: document.title,
        initialThemeName: "",
        contextPath: "/",
        messages: {},
        panel: null,
        button: null,
        styleElement: null
    };

    function isTrue(value) {
        const text = String(value ?? "").trim().toLowerCase();
        return text.length > 0 && !["false", "off", "no"].includes(text);
    }

    function isBooleanProperty(property) {
        return ["true", "false"].includes(String(property.defaultValue).toLowerCase())
            || ["true", "false"].includes(String(property.value).toLowerCase());
    }

    function storageKey() {
        return `CasThemeBuilder:${state.model.theme}`;
    }

    function readDraft() {
        try {
            const value = window.sessionStorage.getItem(storageKey());
            return value ? JSON.parse(value) : null;
        } catch (e) {
            return null;
        }
    }

    function writeDraft(extra = {}) {
        try {
            const previous = readDraft() || {};
            const draft = {
                ...previous,
                properties: Object.fromEntries(changedProperties().map((name) => [name, state.properties.get(name)])),
                styles: Object.fromEntries(changedStyles().map((key) => [key, state.styles.get(key)])),
                extraCss: state.extraCss === state.initialExtraCss ? null : state.extraCss,
                ...extra
            };
            window.sessionStorage.setItem(storageKey(), JSON.stringify(draft));
        } catch (e) {
            console.debug("Unable to keep the theme builder draft", e);
        }
    }

    function clearDraft() {
        try {
            window.sessionStorage.removeItem(storageKey());
        } catch (e) {
            console.debug("Unable to clear the theme builder draft", e);
        }
    }

    function resolveUrl(value) {
        const text = String(value ?? "").trim();
        if (text.length === 0 || /^([a-z][a-z0-9+.-]*:|\/\/)/i.test(text) || !text.startsWith("/")) {
            return text;
        }
        return state.contextPath.replace(/\/$/, "") + text;
    }

    function labelOf(name) {
        const words = name.replace(/^cas\./, "").split(/[.-]/).filter((word) => word.length > 0)
            .map((word) => (ACRONYMS.has(word) ? word.toUpperCase() : word));
        const label = words.join(" ");
        return label.charAt(0).toUpperCase() + label.slice(1);
    }

    function groupOf(name) {
        return GROUPS.find((group) => group.pattern.test(name)) || GROUPS[GROUPS.length - 1];
    }

    function toggleTargets(name) {
        return Array.from(document.querySelectorAll(`[data-cas-theme-builder-toggle="${CSS.escape(name)}"]`));
    }

    function setShown(name, shown) {
        toggleTargets(name).forEach((element) => element.classList.toggle("cas-theme-builder-off", !shown));
    }

    const PREVIEWS = {
        "cas.theme.name": (value) => {
            const name = value.trim();
            const layoutTitle = state.initialThemeName || DEFAULT_TITLE;
            const rest = state.initialTitle.startsWith(layoutTitle) ? state.initialTitle.slice(layoutTitle.length) : "";
            document.title = (name || DEFAULT_TITLE) + rest;
            const display = name || "CAS";
            document.querySelectorAll("#app-bar .cas-brand h1, #app-drawer-title").forEach((element) => {
                element.textContent = display;
            });
            document.querySelectorAll("#cas-logo, #heroimg").forEach((element) => {
                element.setAttribute("alt", display);
            });
            const hero = document.getElementById("heroimg");
            if (hero) {
                hero.setAttribute("title", display);
            }
        },
        "cas.theme.description": (value) => {
            document.querySelectorAll("#app-drawer .mdc-drawer__subtitle").forEach((element) => {
                element.textContent = value.trim() || "Central Authentication Service";
            });
        },
        "cas.logo.file": (value) => {
            const logo = document.getElementById("cas-logo");
            if (logo) {
                logo.setAttribute("src", resolveUrl(value.trim() || DEFAULT_LOGO));
            }
        },
        "cas.favicon.file": (value) => {
            let icon = document.querySelector("link[rel='shortcut icon'], link[rel='icon']");
            if (!icon) {
                icon = document.createElement("link");
                icon.rel = "shortcut icon";
                document.head.appendChild(icon);
            }
            icon.setAttribute("href", resolveUrl(value.trim() || DEFAULT_FAVICON));
        },
        "cas.hero-banner.file": (value) => {
            const file = value.trim();
            setShown("cas.hero-banner.file", file.length > 0);
            const hero = document.getElementById("heroimg");
            if (hero && file.length > 0) {
                hero.setAttribute("src", resolveUrl(file));
            }
        },
        "cas.pm-links.forgot-password.url": (value) => {
            const url = value.trim();
            setShown("cas.pm-links.forgot-password.url", url.length > 0);
            document.querySelectorAll("#forgotPasswordExternal a").forEach((link) => link.setAttribute("href", url));
        },
        "cas.main-content.position": (value) => {
            const position = value.trim().toLowerCase().replace(/-/g, "") || "topcenter";
            document.querySelectorAll(".mdc-drawer-app-content").forEach((content) => {
                content.classList.toggle("cas-content-middle", position.startsWith("middle"));
                content.classList.toggle("cas-content-bottom", position.startsWith("bottom"));
                content.classList.toggle("cas-content-left", position.endsWith("left"));
                content.classList.toggle("cas-content-right", position.endsWith("right"));
            });
        },
        "cas.login-side-panel.position": (value) => {
            document.querySelectorAll(".cas-login-card").forEach((card) =>
                card.classList.toggle("cas-side-panel-end", value.trim().toLowerCase() === "end"));
        },
        "cas.login-providers.position": (value) => {
            document.querySelectorAll(".cas-login-card").forEach((card) =>
                card.classList.toggle("cas-providers-before", value.trim().toLowerCase() === "before"));
        },
        "cas.login-providers.layout": (value) => swapClass("#identityProvidersPanel", "cas-providers-", value, "stacked"),
        "cas.login-providers.sort": (value) => {
            document.querySelectorAll("#identityProvidersPanel .cas-providers-list").forEach((list) => {
                const items = Array.from(list.children);
                items.forEach((item, index) => {
                    item.dataset.ctbOrder ??= String(index);
                });
                const label = (item) => item.querySelector(".mdc-button__label")?.textContent.trim() ?? "";
                items.sort(isTrue(value)
                    ? (a, b) => label(a).localeCompare(label(b))
                    : (a, b) => Number(a.dataset.ctbOrder) - Number(b.dataset.ctbOrder));
                list.append(...items);
            });
        },
        "cas.mfa.selection.layout": (value) => swapClass(".cas-mfa-providers", "cas-mfa-layout-", value, "list"),
        "cas.gauth.registration.scratch-codes": (value) => {
            document.querySelectorAll("#scratchcodes").forEach((codes) =>
                codes.classList.toggle("cas-scratch-codes-list", value.trim().toLowerCase() === "list"));
        },
        "cas.consent.layout": (value) => {
            document.querySelectorAll("#divAttributes").forEach((attributes) =>
                attributes.classList.toggle("cas-consent-list", value.trim().toLowerCase() === "list"));
        },
        "cas.locale-selection.languages": (value) => {
            const selector = document.getElementById("languageSelector");
            if (!selector) {
                return;
            }
            const selected = selector.value;
            const languages = [...new Set(value.split(",").map((lang) => lang.trim()).filter((lang) => lang.length > 0))].sort();
            selector.replaceChildren(...languages.map((lang) => {
                const option = document.createElement("option");
                option.value = lang;
                option.textContent = lang.toUpperCase();
                option.selected = lang === selected;
                return option;
            }));
        }
    };

    function swapClass(selector, prefix, value, fallback) {
        const choice = (value.trim().toLowerCase() || fallback).replace(/[^a-z0-9-]/g, "");
        document.querySelectorAll(selector).forEach((target) => {
            Array.from(target.classList).filter((name) => name.startsWith(prefix)).forEach((name) => target.classList.remove(name));
            target.classList.add(prefix + choice);
        });
    }

    const PREVIEW_TARGETS = {
        "cas.main-content.position": ".mdc-drawer-app-content",
        "cas.login-side-panel.position": "#loginSidePanel",
        "cas.login-providers.position": "#loginProviders",
        "cas.login-providers.layout": "#identityProvidersPanel",
        "cas.login-providers.sort": "#identityProvidersPanel .cas-providers-list",
        "cas.mfa.selection.layout": ".cas-mfa-providers",
        "cas.gauth.registration.scratch-codes": "#scratchcodes",
        "cas.consent.layout": "#divAttributes"
    };

    function hasPreview(name) {
        if (PREVIEW_TARGETS[name]) {
            return document.querySelector(PREVIEW_TARGETS[name]) !== null;
        }
        if (PREVIEWS[name]) {
            return !VALUE_TOGGLES.has(name) || toggleTargets(name).length > 0;
        }
        return toggleTargets(name).length > 0;
    }

    function previewProperty(name) {
        const value = state.properties.get(name) ?? "";
        if (PREVIEWS[name]) {
            PREVIEWS[name](value);
        } else if (toggleTargets(name).length > 0) {
            setShown(name, isTrue(value));
        }
    }

    function changedProperties() {
        return state.model.properties
            .map((property) => property.name)
            .filter((name) => state.properties.get(name) !== state.initialProperties.get(name));
    }

    function exportedProperties() {
        const cssChanged = buildStyleBlock().length > 0 || state.extraCss.trim() !== state.initialExtraCss.trim();
        return state.model.properties
            .map((property) => {
                let value = state.properties.get(property.name) ?? "";
                if (property.name === "cas.standard.css.file" && cssChanged && !state.customCssLink
                    && !value.split(",").some((file) => file.trim().endsWith("/custom.css"))) {
                    value = value.trim().length > 0 ? `${value},${CUSTOM_CSS_FILE}` : CUSTOM_CSS_FILE;
                }
                return {...property, exported: value};
            })
            .filter((property) => property.exported !== property.defaultValue);
    }

    function escapeProperty(value, key) {
        let escaped = "";
        for (const ch of value) {
            const code = ch.codePointAt(0);
            if (ch === "\\") {
                escaped += "\\\\";
            } else if (key && /[=: #!]/.test(ch)) {
                escaped += `\\${ch}`;
            } else if (code > 0x7e || code < 0x20) {
                escaped += ch.split("").map((unit) => `\\u${unit.charCodeAt(0).toString(16).padStart(4, "0")}`).join("");
            } else {
                escaped += ch;
            }
        }
        return escaped.replace(/^ /, "\\ ");
    }

    function buildPropertiesFile() {
        const properties = exportedProperties();
        if (properties.length === 0) {
            return "";
        }
        const lines = [
            "# Theme properties generated by the CAS theme builder.",
            `# Theme: ${state.model.theme}`,
            "# Only properties that differ from cas-theme-default.properties are listed.",
            ""
        ];
        properties.forEach((property) => {
            if (property.description) {
                wrap(property.description, 100).forEach((line) => lines.push(`# ${line}`));
            }
            lines.push(`${escapeProperty(property.name, true)}=${escapeProperty(property.exported, false)}`, "");
        });
        return lines.join("\n");
    }

    function wrap(text, width) {
        const lines = [];
        let line = "";
        text.split(/\s+/).forEach((word) => {
            if (line.length > 0 && line.length + word.length + 1 > width) {
                lines.push(line);
                line = word;
            } else {
                line = line.length > 0 ? `${line} ${word}` : word;
            }
        });
        if (line.length > 0) {
            lines.push(line);
        }
        return lines;
    }

    function changedStyles() {
        return STYLE_ITEMS.map((item) => item.key)
            .filter((key) => (state.styles.get(key) ?? "") !== (state.initialStyles.get(key) ?? ""));
    }

    function exportedStyles() {
        const changed = new Set(changedStyles());
        return STYLE_ITEMS.filter((item) => (changed.has(item.key) || state.baselineStyles.has(item.key))
            && (state.styles.get(item.key) ?? "").trim().length > 0);
    }

    function cssValue(item) {
        const value = (state.styles.get(item.key) ?? "").trim();
        if (item.type === "length" && /^-?\d+(\.\d+)?$/.test(value)) {
            return `${value}px`;
        }
        if (item.type === "image" && value !== "none" && !/^(url|linear-gradient|radial-gradient)\(/i.test(value)) {
            return `url("${resolveUrl(value).replace(/["\\]/g, "\\$&")}")`;
        }
        return value.replace(/[;{}]/g, "");
    }

    function buildStyleBlock() {
        const items = exportedStyles();
        if (items.length === 0) {
            return "";
        }
        const rules = new Map();
        items.forEach((item) => {
            const declarations = rules.get(item.selector) || [];
            const property = item.property || item.key;
            declarations.push(`    ${property}: ${cssValue(item)}${item.important ? " !important" : ""};`);
            rules.set(item.selector, declarations);
        });
        const body = [...rules.entries()].map(([selector, declarations]) => `${selector} {\n${declarations.join("\n")}\n}`);
        return [`${MARK_START}\n/* Generated by the CAS theme builder; it reads this block back when it opens. */`, ...body, MARK_END].join("\n\n");
    }

    function buildCssFile() {
        const block = buildStyleBlock();
        const extra = state.extraCss.trim();
        if (block.length === 0 && extra === state.initialExtraCss.trim()) {
            return "";
        }
        return [block, extra].filter((part) => part.length > 0).join("\n\n") + "\n";
    }

    function absolutizeUrls(css, base) {
        if (!base) {
            return css;
        }
        return css.replace(/url\(\s*(['"]?)(?!data:|[a-z][a-z0-9+.-]*:|\/|#)([^'")]+)\1\s*\)/gi,
            (match, quote, path) => {
                try {
                    return `url("${new URL(path, base).href}")`;
                } catch (e) {
                    return match;
                }
            });
    }

    function applyStyles() {
        const extraChanged = state.extraCss !== state.initialExtraCss;
        const takeOver = state.customCssLoaded && state.customCssLink && extraChanged;
        if (state.customCssLink) {
            state.customCssLink.disabled = Boolean(takeOver);
        }
        const parts = [];
        if (takeOver) {
            parts.push(absolutizeUrls(state.extraCss, state.customCssLink.href));
        } else if (!state.customCssLink && state.extraCss.trim().length > 0) {
            parts.push(state.extraCss);
        }
        parts.push(buildStyleBlock());
        state.styleElement.textContent = parts.filter((part) => part.length > 0).join("\n\n");
    }

    function readComputed(item) {
        if (item.property) {
            const probe = document.querySelector(item.probe);
            if (!probe) {
                return "";
            }
            const value = getComputedStyle(probe).getPropertyValue(item.property).trim();
            if (item.type === "length") {
                return String(Math.round(parseFloat(value) || 0));
            }
            if (item.type === "image") {
                return value === "none" ? "none" : value;
            }
            return normalizeColor(value);
        }
        const host = item.selector === UI ? document.querySelector(".cas-ui:not(.cas-theme-builder)") || document.documentElement
            : document.documentElement;
        const value = getComputedStyle(host).getPropertyValue(item.key).trim();
        if (item.type === "length") {
            const number = parseFloat(value);
            return Number.isFinite(number) ? String(Math.round(number)) : "";
        }
        return item.type === "color" ? normalizeColor(value) : value;
    }

    function normalizeColor(value) {
        const text = String(value || "").trim();
        if (/^#[0-9a-f]{6}$/i.test(text)) {
            return text.toLowerCase();
        }
        if (/^#[0-9a-f]{3}$/i.test(text)) {
            return `#${text.slice(1).split("").map((ch) => ch + ch).join("")}`.toLowerCase();
        }
        const rgb = text.match(/^rgba?\(\s*(\d+)[\s,]+(\d+)[\s,]+(\d+)\s*(?:[,/]\s*(1|1\.0+|100%)\s*)?\)$/i);
        if (rgb) {
            return `#${rgb.slice(1, 4).map((part) => Number(part).toString(16).padStart(2, "0")).join("")}`;
        }
        return text;
    }

    function colorForPicker(value) {
        const text = String(value || "").trim();
        if (/^#[0-9a-f]{6}$/i.test(text)) {
            return text;
        }
        const match = text.match(/^rgba?\(\s*(\d+)[\s,]+(\d+)[\s,]+(\d+)/i);
        if (match) {
            return `#${match.slice(1, 4).map((part) => Number(part).toString(16).padStart(2, "0")).join("")}`;
        }
        const probe = document.createElement("span");
        probe.style.color = text;
        if (probe.style.color.length === 0) {
            return "#000000";
        }
        document.body.appendChild(probe);
        const computed = getComputedStyle(probe).color;
        probe.remove();
        return colorForPicker(computed === text ? "#000000" : computed);
    }

    function parseBuilderBlock(css) {
        const start = css.indexOf(MARK_START);
        const end = css.indexOf(MARK_END);
        if (start < 0 || end < start) {
            return {values: new Map(), rest: css.trim()};
        }
        const block = css.slice(start + MARK_START.length, end);
        const rest = (css.slice(0, start) + css.slice(end + MARK_END.length)).trim();
        const values = new Map();
        const rulePattern = /([^{}]+)\{([^{}]*)\}/g;
        let rule;
        while ((rule = rulePattern.exec(block)) !== null) {
            const selector = rule[1].replace(/\/\*[\s\S]*?\*\//g, "").trim();
            rule[2].split(";").forEach((declaration) => {
                const index = declaration.indexOf(":");
                if (index < 0) {
                    return;
                }
                const property = declaration.slice(0, index).trim();
                const value = declaration.slice(index + 1).replace(/!important/i, "").trim();
                const item = STYLE_ITEMS.find((candidate) => candidate.selector === selector
                    && (candidate.property || candidate.key) === property);
                if (item) {
                    values.set(item.key, fromCssValue(item, value));
                }
            });
        }
        return {values, rest};
    }

    function fromCssValue(item, value) {
        if (item.type === "length") {
            return String(parseFloat(value) || 0);
        }
        if (item.type === "image") {
            const match = value.match(/^url\(\s*(['"]?)(.*?)\1\s*\)$/i);
            return match ? match[2] : value;
        }
        return item.type === "color" ? normalizeColor(value) : value;
    }

    async function loadCustomCss() {
        state.customCssLink = Array.from(document.querySelectorAll("link[rel~='stylesheet']"))
            .find((link) => /(^|\/)custom(-[0-9a-f]{32})?\.css$/i.test(new URL(link.href, document.baseURI).pathname)) || null;
        if (!state.customCssLink) {
            return;
        }
        try {
            const response = await fetch(state.customCssLink.href, {credentials: "same-origin"});
            if (!response.ok) {
                return;
            }
            const parsed = parseBuilderBlock(await response.text());
            parsed.values.forEach((value, key) => state.baselineStyles.add(key));
            state.initialExtraCss = parsed.rest;
            state.extraCss = parsed.rest;
            state.customCssLoaded = true;
        } catch (e) {
            console.debug("Unable to read custom.css", e);
        }
    }

    function element(tag, attributes = {}, children = []) {
        const node = document.createElement(tag);
        Object.entries(attributes).forEach(([key, value]) => {
            if (value === undefined || value === null || value === false) {
                return;
            }
            if (key === "text") {
                node.textContent = value;
            } else if (key === "className") {
                node.className = value;
            } else {
                node.setAttribute(key, value === true ? "" : value);
            }
        });
        children.filter(Boolean).forEach((child) => node.append(child));
        return node;
    }

    function tag(text, className, title) {
        return element("span", {className: `ctb-tag ${className || ""}`.trim(), text, title});
    }

    function resetButton(onReset) {
        const button = element("button", {type: "button", className: "ctb-reset-one", text: state.messages.reset, hidden: true});
        button.addEventListener("click", onReset);
        return button;
    }

    function renderProperties() {
        const container = state.panel.querySelector("#ctb-properties");
        container.replaceChildren();
        const grouped = new Map(GROUPS.map((group) => [group.key, []]));
        state.model.properties.forEach((property) => grouped.get(groupOf(property.name).key).push(property));

        GROUPS.forEach((group) => {
            const properties = grouped.get(group.key);
            if (properties.length === 0) {
                return;
            }
            const section = element("div", {className: "ctb-group", "data-ctb-group": group.key}, [
                element("h3", {className: "ctb-group-title", text: group.title})
            ]);
            properties.forEach((property) => section.append(renderProperty(property)));
            container.append(section);
        });
    }

    function renderProperty(property) {
        const id = `ctb-prop-${property.name.replace(/[^a-z0-9]+/gi, "-")}`;
        const descriptionId = `${id}-desc`;
        const live = hasPreview(property.name);
        const booleanProperty = isBooleanProperty(property);
        let input;
        const choices = CHOICES[property.name];
        if (booleanProperty) {
            input = element("input", {type: "checkbox", role: "switch", id, className: "ctb-switch", "aria-describedby": descriptionId});
            input.checked = isTrue(state.properties.get(property.name));
        } else if (choices) {
            input = element("select", {id, className: "ctb-input", "aria-describedby": descriptionId});
            const current = state.properties.get(property.name) ?? "";
            const values = choices.includes(current) || current === "" ? choices : [...choices, current];
            input.append(...values.map((value) => element("option", {value, text: value})));
            input.value = current || property.defaultValue || choices[0];
        } else {
            input = element("input", {
                type: "text", id, className: "ctb-input", spellcheck: "false", autocomplete: "off",
                placeholder: property.example || property.defaultValue || "", "aria-describedby": descriptionId
            });
            input.value = state.properties.get(property.name) ?? "";
        }
        const row = element("div", {
            className: `ctb-row${booleanProperty ? "" : " is-wide"}`,
            "data-ctb-property": property.name,
            "data-ctb-search": `${property.name} ${labelOf(property.name)} ${property.description}`.toLowerCase()
        });
        const reset = resetButton(() => {
            setProperty(property.name, state.initialProperties.get(property.name) ?? "");
            if (booleanProperty) {
                input.checked = isTrue(state.properties.get(property.name));
            } else {
                input.value = state.properties.get(property.name) ?? "";
            }
            input.focus();
        });
        const meta = element("div", {className: "ctb-meta"}, [
            element("label", {className: "ctb-label", for: id, text: labelOf(property.name)}),
            element("code", {className: "ctb-name", text: property.name}),
            element("p", {className: "ctb-desc", id: descriptionId, text: property.description, title: property.description}),
            element("div", {className: "ctb-tags"}, [
                live ? tag(state.messages.live, "is-live") : tag(state.messages.deploy, "", state.messages.deployTitle),
                reset
            ])
        ]);
        row.append(meta, element("div", {className: "ctb-control"}, [input]));
        input.addEventListener(booleanProperty || choices ? "change" : "input", () => {
            setProperty(property.name, booleanProperty ? String(input.checked) : input.value);
        });
        updateRow(row, property.name, reset);
        return row;
    }

    function updateRow(row, name, reset) {
        const changed = state.properties.get(name) !== state.initialProperties.get(name);
        row.classList.toggle("is-changed", changed);
        (reset || row.querySelector(".ctb-reset-one")).hidden = !changed;
    }

    function setProperty(name, value) {
        state.properties.set(name, value);
        previewProperty(name);
        const row = state.panel.querySelector(`[data-ctb-property="${CSS.escape(name)}"]`);
        if (row) {
            updateRow(row, name);
        }
        changed();
    }

    function renderStyles() {
        const container = state.panel.querySelector("#ctb-styles");
        container.replaceChildren();
        STYLES.forEach((group) => {
            const section = element("div", {className: "ctb-group"}, [
                element("h3", {className: "ctb-group-title", text: group.title})
            ]);
            group.items.forEach((item) => {
                if (item.probe && !document.querySelector(item.probe)) {
                    return;
                }
                section.append(renderStyle(item));
            });
            if (section.children.length > 1) {
                container.append(section);
            }
        });
    }

    function renderStyle(item) {
        const id = `ctb-style-${item.key.replace(/^-+/, "").replace(/[^a-z0-9]+/gi, "-")}`;
        const helpId = `${id}-help`;
        const row = element("div", {className: "ctb-row is-wide", "data-ctb-style": item.key});
        const control = element("div", {className: "ctb-control"});
        const value = () => state.styles.get(item.key) ?? "";
        let sync;

        if (item.type === "color") {
            const picker = element("input", {type: "color", className: "ctb-color", "aria-label": `${item.label} picker`, tabindex: "-1"});
            const text = element("input", {type: "text", id, className: "ctb-input ctb-code", spellcheck: "false",
                autocomplete: "off", "aria-describedby": item.help ? helpId : null});
            sync = () => {
                text.value = value();
                picker.value = colorForPicker(value());
            };
            picker.addEventListener("input", () => {
                text.value = picker.value;
                setStyle(item.key, picker.value);
            });
            text.addEventListener("input", () => {
                setStyle(item.key, text.value);
                picker.value = colorForPicker(text.value);
            });
            control.append(picker, text);
        } else if (item.type === "length") {
            const range = element("input", {type: "range", className: "ctb-range", min: item.min, max: item.max, step: 1,
                "aria-label": item.label, tabindex: "-1"});
            const number = element("input", {type: "number", id, className: "ctb-input ctb-number", min: 0, step: 1,
                "aria-describedby": item.help ? helpId : null});
            sync = () => {
                range.value = value();
                number.value = value();
            };
            range.addEventListener("input", () => {
                number.value = range.value;
                setStyle(item.key, range.value);
            });
            number.addEventListener("input", () => {
                range.value = number.value;
                setStyle(item.key, number.value);
            });
            control.append(range, number, element("span", {className: "ctb-help", text: "px"}));
        } else if (item.type === "font") {
            const select = element("select", {className: "ctb-input", "aria-label": `${item.label} presets`});
            select.append(element("option", {value: "", text: "Custom"}), ...FONTS.map((font) => element("option", {value: font.value, text: font.label})));
            const text = element("input", {type: "text", id, className: "ctb-input ctb-code", spellcheck: "false", autocomplete: "off"});
            sync = () => {
                text.value = value();
                select.value = FONTS.some((font) => font.value === value()) ? value() : "";
            };
            select.addEventListener("change", () => {
                if (select.value) {
                    text.value = select.value;
                    setStyle(item.key, select.value);
                }
            });
            text.addEventListener("input", () => {
                setStyle(item.key, text.value);
                select.value = FONTS.some((font) => font.value === text.value) ? text.value : "";
            });
            control.append(element("div", {className: "ctb-stack"}, [select, text]));
        } else {
            const text = element("input", {type: "text", id, className: "ctb-input ctb-code", spellcheck: "false",
                autocomplete: "off", placeholder: "/images/background.jpg", "aria-describedby": item.help ? helpId : null});
            sync = () => {
                text.value = value();
            };
            text.addEventListener("input", () => setStyle(item.key, text.value));
            control.append(text);
        }

        const reset = resetButton(() => {
            setStyle(item.key, state.initialStyles.get(item.key) ?? "");
            sync();
        });
        row.append(element("div", {className: "ctb-meta"}, [
            element("label", {className: "ctb-label", for: id, text: item.label}),
            element("code", {className: "ctb-name", text: item.property ? `${item.selector} { ${item.property} }` : item.key}),
            item.help ? element("p", {className: "ctb-help", id: helpId, text: item.help}) : null,
            element("div", {className: "ctb-tags"}, [reset])
        ]), control);
        sync();
        updateStyleRow(row, item.key, reset);
        return row;
    }

    function updateStyleRow(row, key, reset) {
        const changed = (state.styles.get(key) ?? "") !== (state.initialStyles.get(key) ?? "");
        row.classList.toggle("is-changed", changed);
        (reset || row.querySelector(".ctb-reset-one")).hidden = !changed;
    }

    function setStyle(key, value) {
        state.styles.set(key, value);
        applyStyles();
        const row = state.panel.querySelector(`[data-ctb-style="${CSS.escape(key)}"]`);
        if (row) {
            updateStyleRow(row, key);
        }
        changed();
    }

    function renderOutput(id, text) {
        const output = state.panel.querySelector(id);
        output.classList.toggle("is-empty", text.length === 0);
        output.textContent = text.length > 0 ? text : state.messages.empty;
    }

    function fileName(kind) {
        if (kind === "css") {
            return "custom.css";
        }
        return state.model.theme === DEFAULT_THEME ? "cas-theme-custom.properties" : `${state.model.theme}.properties`;
    }

    function changed() {
        const properties = buildPropertiesFile();
        const css = buildCssFile();
        renderOutput("#ctb-output-properties", properties);
        renderOutput("#ctb-output-css", css);
        state.panel.querySelectorAll("[data-ctb-download='properties'], [data-ctb-copy='properties']")
            .forEach((button) => { button.disabled = properties.length === 0; });
        state.panel.querySelectorAll("[data-ctb-download='css'], [data-ctb-copy='css']")
            .forEach((button) => { button.disabled = css.length === 0; });
        state.panel.querySelector("#ctb-css-missing").hidden = Boolean(state.customCssLink) || css.length === 0;

        const exported = exportedProperties().length + exportedStyles().length + (css.length > 0 && state.extraCss !== state.initialExtraCss ? 1 : 0);
        const count = state.panel.querySelector("#ctb-change-count");
        count.hidden = exported === 0;
        count.textContent = String(exported);

        const drafted = changedProperties().length + changedStyles().length + (state.extraCss !== state.initialExtraCss ? 1 : 0);
        const dot = state.button.querySelector(".cas-theme-builder-dot");
        if (dot) {
            dot.hidden = drafted === 0;
        }
        writeDraft();
    }

    function download(kind) {
        const text = kind === "css" ? buildCssFile() : buildPropertiesFile();
        if (text.length === 0) {
            return;
        }
        const type = kind === "css" ? "text/css" : "text/plain";
        const url = URL.createObjectURL(new Blob([text], {type: `${type};charset=utf-8`}));
        const link = element("a", {href: url, download: fileName(kind)});
        document.body.append(link);
        link.click();
        link.remove();
        setTimeout(() => URL.revokeObjectURL(url), 1000);
    }

    async function copy(kind, button) {
        const text = kind === "css" ? buildCssFile() : buildPropertiesFile();
        try {
            await navigator.clipboard.writeText(text);
        } catch (e) {
            const area = element("textarea", {className: "ctb-offscreen", "aria-hidden": "true"});
            area.value = text;
            document.body.append(area);
            area.select();
            document.execCommand("copy");
            area.remove();
        }
        button.textContent = state.messages.copied;
        state.panel.querySelector("#ctb-status").textContent = state.messages.copied;
        setTimeout(() => {
            button.textContent = state.messages.copy;
        }, 1500);
    }

    function selectTab(tab, focus) {
        state.panel.querySelectorAll("[role='tab']").forEach((candidate) => {
            const selected = candidate === tab;
            candidate.setAttribute("aria-selected", String(selected));
            candidate.tabIndex = selected ? 0 : -1;
            state.panel.querySelector(`#${candidate.getAttribute("aria-controls")}`).hidden = !selected;
        });
        if (focus) {
            tab.focus();
        }
        writeDraft({tab: tab.id});
    }

    function open(focus = true) {
        const panel = state.panel;
        panel.hidden = false;
        requestAnimationFrame(() => panel.classList.add("is-open"));
        document.body.classList.add("cas-theme-builder-open");
        state.button.setAttribute("aria-expanded", "true");
        if (focus) {
            panel.focus({preventScroll: true});
        }
        writeDraft({open: true});
    }

    function close() {
        const panel = state.panel;
        panel.classList.remove("is-open");
        document.body.classList.remove("cas-theme-builder-open");
        state.button.setAttribute("aria-expanded", "false");
        const hide = () => {
            if (!panel.classList.contains("is-open")) {
                panel.hidden = true;
            }
        };
        if (matchMedia("(prefers-reduced-motion: reduce)").matches) {
            hide();
        } else {
            panel.addEventListener("transitionend", hide, {once: true});
            setTimeout(hide, 400);
        }
        state.button.focus();
        writeDraft({open: false});
    }

    function resetAll() {
        state.initialProperties.forEach((value, name) => {
            state.properties.set(name, value);
            previewProperty(name);
        });
        state.initialStyles.forEach((value, key) => state.styles.set(key, value));
        state.extraCss = state.initialExtraCss;
        state.panel.querySelector("#ctb-extra-css").value = state.extraCss;
        applyStyles();
        renderProperties();
        renderStyles();
        filter(state.panel.querySelector("#ctb-filter").value);
        changed();
        clearDraft();
        writeDraft({open: true});
    }

    function filter(text) {
        const query = text.trim().toLowerCase();
        state.panel.querySelectorAll("[data-ctb-property]").forEach((row) => {
            row.hidden = query.length > 0 && !row.dataset.ctbSearch.includes(query);
        });
        state.panel.querySelectorAll("[data-ctb-group]").forEach((group) => {
            group.hidden = !group.querySelector("[data-ctb-property]:not([hidden])");
        });
    }

    function wire() {
        const panel = state.panel;
        state.button.addEventListener("click", () => (panel.classList.contains("is-open") ? close() : open()));
        panel.querySelector("#cas-theme-builder-close").addEventListener("click", close);
        panel.addEventListener("keydown", (event) => {
            if (event.key === "Escape") {
                event.stopPropagation();
                close();
            }
        });
        const tabs = Array.from(panel.querySelectorAll("[role='tab']"));
        tabs.forEach((tab, index) => {
            tab.addEventListener("click", () => selectTab(tab, false));
            tab.addEventListener("keydown", (event) => {
                const offset = {ArrowRight: 1, ArrowLeft: -1}[event.key];
                if (offset) {
                    event.preventDefault();
                    selectTab(tabs[(index + offset + tabs.length) % tabs.length], true);
                } else if (event.key === "Home" || event.key === "End") {
                    event.preventDefault();
                    selectTab(tabs[event.key === "Home" ? 0 : tabs.length - 1], true);
                }
            });
        });
        panel.querySelector("#ctb-filter").addEventListener("input", (event) => filter(event.target.value));
        const extra = panel.querySelector("#ctb-extra-css");
        extra.value = state.extraCss;
        extra.addEventListener("input", () => {
            state.extraCss = extra.value;
            applyStyles();
            changed();
        });
        panel.querySelectorAll("[data-ctb-download]").forEach((button) => {
            button.addEventListener("click", () => download(button.dataset.ctbDownload));
        });
        panel.querySelectorAll("[data-ctb-copy]").forEach((button) => {
            button.addEventListener("click", () => copy(button.dataset.ctbCopy, button));
        });
        panel.querySelector("#ctb-reset").addEventListener("click", resetAll);
    }

    async function initialize() {
        state.panel = document.getElementById("cas-theme-builder");
        state.button = document.getElementById("cas-theme-builder-menu");
        if (!state.panel || !state.button) {
            return;
        }
        const model = window.casThemeBuilderModel || {};
        state.model = {
            theme: /^[\w.-]+$/.test(model.theme || "") ? model.theme : DEFAULT_THEME,
            properties: Array.isArray(model.properties) ? model.properties : []
        };
        state.contextPath = state.panel.dataset.contextPath || "/";
        const data = state.panel.dataset;
        state.messages = {
            live: data.msgLive, deploy: data.msgDeploy, deployTitle: data.msgDeployTitle, changed: data.msgChanged,
            reset: data.msgReset, copied: data.msgCopied, copy: data.msgCopy, empty: data.msgEmpty
        };
        state.panel.querySelector("#ctb-properties-file-name").textContent = fileName("properties");

        state.model.properties.forEach((property) => {
            state.properties.set(property.name, property.value ?? "");
            state.initialProperties.set(property.name, property.value ?? "");
        });
        state.initialThemeName = (state.initialProperties.get("cas.theme.name") || "").trim();

        await loadCustomCss();
        STYLE_ITEMS.forEach((item) => {
            const value = readComputed(item);
            state.initialStyles.set(item.key, value);
            state.styles.set(item.key, value);
        });

        state.styleElement = element("style", {id: "cas-theme-builder-preview"});
        document.head.append(state.styleElement);

        const draft = readDraft() || {};
        Object.entries(draft.properties || {}).forEach(([name, value]) => {
            if (state.properties.has(name)) {
                state.properties.set(name, String(value));
                previewProperty(name);
            }
        });
        Object.entries(draft.styles || {}).forEach(([key, value]) => {
            if (state.styles.has(key)) {
                state.styles.set(key, String(value));
            }
        });
        if (typeof draft.extraCss === "string") {
            state.extraCss = draft.extraCss;
        }
        applyStyles();

        renderProperties();
        renderStyles();
        wire();
        changed();

        const tab = draft.tab && state.panel.querySelector(`#${CSS.escape(draft.tab)}[role='tab']`);
        if (tab) {
            selectTab(tab, false);
        }
        if (draft.open) {
            open(false);
        }
    }

    document.addEventListener("DOMContentLoaded", () => setTimeout(initialize, 0));
})();
