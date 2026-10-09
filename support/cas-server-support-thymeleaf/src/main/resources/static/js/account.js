/*
 * Behavior for the account profile (rail navigation, panels, inline record details, list filtering and paging,
 * device registration options, security questions) and the account management flows (caps lock hint).
 */
(() => {
    const STORAGE_KEY = "AccountProfileSelectedTab";
    const PAGE_SIZE = 10;
    const FILTER_THRESHOLD = 6;

    function readStorage() {
        try {
            return window.localStorage.getItem(STORAGE_KEY);
        } catch (e) {
            return null;
        }
    }

    function writeStorage(value) {
        try {
            window.localStorage.setItem(STORAGE_KEY, value);
        } catch (e) {
            console.debug("Unable to remember the selected account panel", e);
        }
    }

    function panelLinks(profile) {
        return Array.from(profile.querySelectorAll("#navigationMenu [data-account-panel]"));
    }

    function showPanel(profile, target, focusHeading) {
        const id = typeof target === "string" && target.length > 1 ? target.replace(/^#/, "") : "divOverview";
        const links = panelLinks(profile);
        let panel = document.getElementById(id);
        const known = links.some((link) => link.dataset.accountPanel === `#${id}`);
        if (!panel || !panel.classList.contains("profile-content") || !known) {
            panel = document.getElementById("divOverview");
        }
        if (!panel) {
            return;
        }
        profile.querySelectorAll(".profile-content").forEach((section) => {
            section.hidden = section !== panel;
        });
        links.forEach((link) => {
            if (link.dataset.accountPanel === `#${panel.id}`) {
                link.setAttribute("aria-current", "page");
            } else {
                link.removeAttribute("aria-current");
            }
        });
        const trail = document.getElementById("accountTrail");
        if (trail && panel.dataset.accountTrail) {
            trail.textContent = panel.dataset.accountTrail;
        }
        writeStorage(`#${panel.id}`);
        closeSections(profile, false);
        if (focusHeading) {
            const heading = panel.querySelector("h2");
            if (heading) {
                heading.setAttribute("tabindex", "-1");
                heading.focus({preventScroll: false});
            }
        }
    }

    function closeSections(profile, restoreFocus) {
        if (!profile.classList.contains("is-nav-open")) {
            return;
        }
        profile.classList.remove("is-nav-open");
        const toggle = profile.querySelector("[data-account-action='sections']");
        if (toggle) {
            toggle.setAttribute("aria-expanded", "false");
            if (restoreFocus) {
                toggle.focus();
            }
        }
    }

    function openSections(profile) {
        profile.classList.add("is-nav-open");
        const toggle = profile.querySelector("[data-account-action='sections']");
        if (toggle) {
            toggle.setAttribute("aria-expanded", "true");
        }
        const current = profile.querySelector("#navigationMenu [aria-current='page']")
            || profile.querySelector("#navigationMenu a, #navigationMenu button");
        if (current) {
            current.focus();
        }
    }

    function renderDetails(container) {
        const source = container.querySelector("[data-account-json]");
        if (!source || container.dataset.rendered === "true") {
            return;
        }
        container.dataset.rendered = "true";
        const text = source.textContent.trim();
        let parsed;
        try {
            parsed = JSON.parse(text);
        } catch {
            parsed = null;
        }
        if (parsed !== null && typeof parsed === "object" && !Array.isArray(parsed)) {
            const list = document.createElement("dl");
            list.className = "cas-account-kv";
            Object.entries(parsed)
                .filter(([key]) => key !== "@class")
                .forEach(([key, value]) => {
                    const term = document.createElement("dt");
                    term.textContent = key;
                    const definition = document.createElement("dd");
                    definition.textContent = value !== null && typeof value === "object"
                        ? JSON.stringify(value, null, 2)
                        : String(value);
                    list.append(term, definition);
                });
            source.replaceWith(list);
        } else {
            const pre = document.createElement("pre");
            const code = document.createElement("code");
            code.className = "language-json";
            code.textContent = parsed === null ? text : JSON.stringify(parsed, null, 2);
            pre.append(code);
            source.replaceWith(pre);
            if (typeof hljs !== "undefined") {
                hljs.highlightElement(code);
            }
        }
    }

    function toggleDetails(button) {
        const target = document.getElementById(button.getAttribute("aria-controls"));
        if (!target) {
            return;
        }
        const expand = target.hidden;
        if (expand) {
            renderDetails(target);
        }
        target.hidden = !expand;
        button.setAttribute("aria-expanded", String(expand));
        const label = button.querySelector("[data-account-toggle-label]") || button;
        label.textContent = expand ? button.dataset.labelHide : button.dataset.labelShow;
    }

    function enhanceList(list) {
        const items = Array.from(list.children);
        if (items.length < FILTER_THRESHOLD) {
            return;
        }
        let visibleLimit = PAGE_SIZE;
        const tools = document.createElement("div");
        tools.className = "cas-account-list-tools";
        const label = document.createElement("label");
        const caption = document.createElement("span");
        caption.textContent = list.dataset.filterLabel || "Filter";
        const input = document.createElement("input");
        input.type = "search";
        input.className = "acct-input";
        input.setAttribute("aria-controls", list.id);
        label.append(caption, input);
        const count = document.createElement("span");
        count.className = "cas-account-list-count";
        count.setAttribute("aria-live", "polite");
        tools.append(label, count);
        list.before(tools);

        const moreWrapper = document.createElement("div");
        moreWrapper.className = "cas-account-list-more";
        const more = document.createElement("button");
        more.type = "button";
        more.className = "acct-btn";
        more.textContent = list.dataset.moreLabel || "Show more";
        moreWrapper.append(more);
        list.after(moreWrapper);

        const update = () => {
            const query = input.value.trim().toLowerCase();
            const matching = items.filter((item) => !query || item.textContent.toLowerCase().includes(query));
            items.forEach((item) => {
                item.hidden = true;
            });
            matching.slice(0, visibleLimit).forEach((item) => {
                item.hidden = false;
            });
            const shown = Math.min(matching.length, visibleLimit);
            count.textContent = (list.dataset.countLabel || "{0} of {1}")
                .replace("{0}", String(shown))
                .replace("{1}", String(matching.length));
            moreWrapper.hidden = matching.length <= visibleLimit;
        };
        input.addEventListener("input", () => {
            visibleLimit = PAGE_SIZE;
            update();
        });
        more.addEventListener("click", () => {
            visibleLimit += PAGE_SIZE;
            update();
        });
        update();
    }

    function summarizeUserAgent(agent) {
        const browsers = [["Edg/", "Edge"], ["OPR/", "Opera"], ["Firefox/", "Firefox"], ["Chrome/", "Chrome"], ["Safari/", "Safari"]];
        const systems = [["iPhone", "iPhone"], ["iPad", "iPad"], ["Android", "Android"], ["Mac OS X", "macOS"], ["Windows", "Windows"], ["CrOS", "ChromeOS"], ["Linux", "Linux"]];
        const browser = browsers.find(([token]) => agent.includes(token));
        const system = systems.find(([token]) => agent.includes(token));
        if (!browser && !system) {
            return null;
        }
        return [browser ? browser[1] : null, system ? system[1] : null].filter(Boolean).join(" · ");
    }

    function renumberQuestions(container) {
        container.querySelectorAll(".cas-account-question").forEach((row, index) => {
            const number = String(index + 1);
            row.querySelectorAll("[data-question-number]").forEach((element) => {
                element.textContent = element.dataset.questionTemplate.replace("{0}", number);
            });
        });
    }

    function confirmAction(button, onConfirm) {
        if (typeof Swal === "undefined") {
            onConfirm();
            return;
        }
        Swal.fire({
            title: button.dataset.confirmTitle,
            text: button.dataset.confirmText,
            icon: "question",
            showCancelButton: true,
            confirmButtonText: button.dataset.confirmButton,
            cancelButtonText: button.dataset.cancelButton,
            customClass: {popup: "cas-account-confirm"}
        }).then((result) => {
            if (result.isConfirmed) {
                onConfirm();
            }
        });
    }

    function initializeProfile(profile) {
        profile.addEventListener("click", (event) => {
            const panelLink = event.target.closest("[data-account-panel]");
            if (panelLink) {
                event.preventDefault();
                window.history.replaceState(null, "", panelLink.dataset.accountPanel);
                showPanel(profile, panelLink.dataset.accountPanel, true);
                return;
            }
            const toggle = event.target.closest("[data-account-toggle]");
            if (toggle) {
                toggleDetails(toggle);
                return;
            }
            const action = event.target.closest("[data-account-action]");
            if (!action) {
                return;
            }
            switch (action.dataset.accountAction) {
            case "sections":
                if (profile.classList.contains("is-nav-open")) {
                    closeSections(profile, true);
                } else {
                    openSections(profile);
                }
                break;
            case "close-sections":
                closeSections(profile, true);
                break;
            case "password":
                confirmAction(action, () => document.forms.fmChangePsw.submit());
                break;
            case "logout":
                event.preventDefault();
                confirmAction(action, () => {
                    window.location.href = action.dataset.logoutUrl;
                });
                break;
            case "register": {
                const options = document.getElementById(action.getAttribute("aria-controls"));
                if (options) {
                    options.hidden = !options.hidden;
                    action.setAttribute("aria-expanded", String(!options.hidden));
                }
                break;
            }
            case "add-question": {
                const container = document.getElementById("securityQuestionsList");
                const template = document.getElementById("securityQuestionTemplate");
                const row = template.content.firstElementChild.cloneNode(true);
                container.append(row);
                renumberQuestions(container);
                row.querySelector("input").focus();
                break;
            }
            case "remove-question": {
                const container = document.getElementById("securityQuestionsList");
                const rows = container.querySelectorAll(".cas-account-question");
                if (rows.length > 1) {
                    action.closest(".cas-account-question").remove();
                    renumberQuestions(container);
                    document.getElementById("addSecurityQuestion").focus();
                } else {
                    rows[0].querySelectorAll("input").forEach((input) => {
                        input.value = "";
                    });
                }
                break;
            }
            default:
                break;
            }
        });

        document.addEventListener("keydown", (event) => {
            if (event.key === "Escape" && profile.classList.contains("is-nav-open")) {
                closeSections(profile, true);
            }
        });

        profile.querySelectorAll("[data-account-list]").forEach(enhanceList);

        profile.querySelectorAll("[data-account-useragent]").forEach((element) => {
            const summary = summarizeUserAgent(element.dataset.accountUseragent);
            if (summary) {
                element.textContent = summary;
            }
        });

        profile.querySelectorAll("[data-account-service-url]").forEach((link) => {
            const url = link.dataset.accountServiceUrl;
            if (typeof isValidURL === "function" && isValidURL(url)) {
                link.setAttribute("href", url);
            }
        });

        window.addEventListener("hashchange", () => showPanel(profile, window.location.hash, true));
        showPanel(profile, window.location.hash || readStorage(), false);
    }

    function initializeCapsLock() {
        const fields = document.querySelectorAll("[data-caps-check]");
        fields.forEach((field) => {
            const warning = document.getElementById(field.dataset.capsCheck);
            if (!warning) {
                return;
            }
            const check = (event) => {
                if (typeof event.getModifierState === "function") {
                    warning.hidden = !event.getModifierState("CapsLock");
                }
            };
            field.addEventListener("keydown", check);
            field.addEventListener("keyup", check);
            field.addEventListener("blur", () => {
                warning.hidden = true;
            });
        });
    }

    document.addEventListener("DOMContentLoaded", () => {
        const profile = document.getElementById("casAccountProfile");
        if (profile) {
            initializeProfile(profile);
        }
        initializeCapsLock();
    });
})();
