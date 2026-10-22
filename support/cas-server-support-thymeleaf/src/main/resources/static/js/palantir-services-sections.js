/**
 * Step-by-step navigation for the registered service editor.
 *
 * The editor carries more than thirty sections, most of which are expiration
 * policies that only apply to one protocol. Sections are collected into named
 * groups, the groups into a handful of steps (Basics, Protocol, Access and
 * attributes, Policies) followed by a Review, and the editor walks them in
 * order: a step bar on top, the current step's sections as tiles, one section
 * on screen at a time, and Back and Next to move along.
 *
 * The section markup is untouched. Every direct-child h3 of
 * #editServiceWizardSectionPanels is a section header, the element after it is
 * its panel, and data-section-group on the header says where it belongs.
 * Adding a section needs nothing here.
 *
 * Every available section always has a tile carrying data-section-key, even
 * when its step is not on screen, so a section can be reached by its key from
 * anywhere; clicking a tile brings its step along.
 */
class RegisteredServiceSections {

    static NAV_SELECTOR = "#editServiceWizardSectionNav";

    static PANELS_SELECTOR = "#editServiceWizardSectionPanels";

    static STEPS_SELECTOR = "#editServiceWizardSteps";

    static REVIEW_SELECTOR = "#editServiceWizardReview";

    static MENU_SELECTOR = "#editServiceWizardMenu";

    static CURRENT_CLASS = "is-current-section";

    static STORAGE_SECTION = "registeredServiceWizardSection";

    static OTHER_GROUP = "Other";

    static REVIEW_STEP = "review";

    /**
     * The order groups appear in, within and across steps.
     *
     * A group named on a header but missing here is appended under Other.
     */
    static GROUP_ORDER = Object.freeze([
        "Basics",
        "Protocol",
        "Attributes",
        "Access & Authentication",
        "Tickets & Tokens",
        "Sessions & Flow",
        "Security"
    ]);

    /**
     * The steps of the wizard and the groups each one walks.
     *
     * The last step that lists Other collects any group not named anywhere.
     */
    static STEPS = Object.freeze([
        {
            key: "basics",
            title: "Basics",
            description: "Name the application, say which requests it answers, and describe it to people.",
            groups: ["Basics"]
        },
        {
            key: "protocol",
            title: "Protocol",
            description: "Settings of the protocol this application speaks.",
            groups: ["Protocol"]
        },
        {
            key: "access",
            title: "Access & attributes",
            description: "Who may use the application, how they authenticate, and which attributes it receives.",
            groups: ["Attributes", "Access & Authentication"]
        },
        {
            key: "policies",
            title: "Policies",
            description: "Optional policies. A policy left empty keeps the CAS default.",
            groups: ["Tickets & Tokens", "Sessions & Flow", "Security", "Other"]
        }
    ]);

    static model = null;

    static filterText = "";

    static reviewing = false;

    static renderScheduled = false;

    static reviewScheduled = false;

    /**
     * Reads the section list out of the DOM, once.
     *
     * Header text is deliberately not cached: the wizard rewrites some headers
     * when the service type is known, so titles are read fresh on every render.
     *
     * @returns {Array} one entry per section, in document order
     */
    static sections() {
        if (RegisteredServiceSections.model !== null) {
            return RegisteredServiceSections.model;
        }
        const panels = document.querySelector(RegisteredServiceSections.PANELS_SELECTOR);
        if (!panels) {
            return [];
        }
        const model = [];
        [...panels.children].forEach((node, index) => {
            if (node.tagName !== "H3") {
                return;
            }
            const panel = node.nextElementSibling;
            if (!panel || panel.tagName === "H3") {
                return;
            }
            const key = node.id || panel.id || `service-section-${index}`;
            if (!node.id) {
                node.id = `${key}-header`;
            }
            panel.setAttribute("role", "region");
            panel.setAttribute("aria-labelledby", node.id);
            model.push({ key: key, header: node, panel: panel });
        });
        RegisteredServiceSections.model = model;
        return model;
    }

    /**
     * Whether a section applies to the service type being edited.
     *
     * The wizard hides inapplicable sections two different ways: hideElements
     * adds the hide and d-none classes, while the service-type switch calls
     * jQuery's hide(), which only writes an inline display. Both are checked.
     *
     * What is deliberately not checked is :visible, because this class hides
     * every section but the current one through a stylesheet rule; asking
     * whether a section is on screen would answer a different question.
     *
     * @param section an entry from sections()
     * @returns {boolean} true when the section should be offered
     */
    static isAvailable(section) {
        const header = section.header;
        if (header.classList.contains("hide") || header.classList.contains("d-none")) {
            return false;
        }
        return header.style.display !== "none";
    }

    /**
     * Reads the section title as it currently reads on screen.
     *
     * @param section an entry from sections()
     * @returns {string} the header's text
     */
    static titleOf(section) {
        return (section.header.textContent || "").trim();
    }

    /**
     * The group a section belongs to.
     *
     * @param section an entry from sections()
     * @returns {string} the declared group, or Other when none is declared
     */
    static groupOf(section) {
        return section.header.getAttribute("data-section-group") || RegisteredServiceSections.OTHER_GROUP;
    }

    /**
     * The step a section belongs to, through its group.
     *
     * @param section an entry from sections()
     * @returns {object} an entry of STEPS
     */
    static stepOf(section) {
        const group = RegisteredServiceSections.groupOf(section);
        const steps = RegisteredServiceSections.STEPS;
        return steps.find(step => step.groups.includes(group))
            || steps.find(step => step.groups.includes(RegisteredServiceSections.OTHER_GROUP))
            || steps[steps.length - 1];
    }

    /**
     * The available sections of one step, in group order and then document order.
     *
     * @param step an entry of STEPS
     * @returns {Array} entries from sections()
     */
    static sectionsOf(step) {
        const available = RegisteredServiceSections.sections().filter(RegisteredServiceSections.isAvailable)
            .filter(section => RegisteredServiceSections.stepOf(section).key === step.key);
        return RegisteredServiceSections.groupsOf(step, available).flatMap(entry => entry.sections);
    }

    /**
     * The sections of a step that apply to this service type but are out of view only because the
     * advanced options are hidden.
     *
     * Every section of the Policies step is an advanced option, so with the advanced options hidden
     * the step would otherwise have nothing to offer and could not be reached at all.
     *
     * @param step an entry of STEPS
     * @returns {Array} entries from sections(), empty when the advanced options are shown
     */
    static advancedSectionsOf(step) {
        if (String($("#hideAdvancedOptions").val()) !== "true") {
            return [];
        }
        const serviceClass = getLastWord($("#serviceClassType").text().trim());
        return RegisteredServiceSections.sections()
            .filter(section => RegisteredServiceSections.stepOf(section).key === step.key)
            .filter(section => {
                const classes = [...section.header.classList];
                const protocols = classes.filter(value => value.startsWith("class-"));
                return classes.includes("advanced-option")
                    && (protocols.length === 0 || protocols.includes(`class-${serviceClass}`));
            });
    }

    /**
     * Group a step's sections by their group, in GROUP_ORDER, unknown groups last.
     *
     * @param step an entry of STEPS
     * @param sections the step's sections
     * @returns {Array} one {group, sections} entry per group that has sections
     */
    static groupsOf(step, sections) {
        const buckets = new Map();
        sections.forEach(section => {
            const group = RegisteredServiceSections.groupOf(section);
            if (!buckets.has(group)) {
                buckets.set(group, []);
            }
            buckets.get(group).push(section);
        });
        const order = [...RegisteredServiceSections.GROUP_ORDER,
            ...[...buckets.keys()].filter(group => !RegisteredServiceSections.GROUP_ORDER.includes(group))];
        return order.filter(group => buckets.has(group)).map(group => ({group: group, sections: buckets.get(group)}));
    }

    /**
     * The key of the section currently on screen.
     *
     * @returns {string|null} the current section key, or null when none is shown
     */
    static currentKey() {
        const current = RegisteredServiceSections.sections()
            .find(section => section.header.classList.contains(RegisteredServiceSections.CURRENT_CLASS));
        return current ? current.key : null;
    }

    /**
     * The step on screen.
     *
     * @returns {string} the key of a step in STEPS, or review
     */
    static currentStepKey() {
        if (RegisteredServiceSections.reviewing) {
            return RegisteredServiceSections.REVIEW_STEP;
        }
        const current = RegisteredServiceSections.sections().find(section => section.key === RegisteredServiceSections.currentKey());
        return current ? RegisteredServiceSections.stepOf(current).key : RegisteredServiceSections.STEPS[0].key;
    }

    /**
     * Whether a section holds anything the operator entered.
     *
     * Text fields, text areas, ticked boxes and multi-value selections count. Switches and single
     * selects always carry a value, so they say nothing about whether the section was used.
     *
     * @param section an entry from sections()
     * @returns {boolean} true when the section holds a value
     */
    static hasValue(section) {
        return [...section.panel.querySelectorAll("input, textarea, select")].some(field => {
            if (field.disabled || field.closest(".ts-control")) {
                return false;
            }
            if (field.tagName === "SELECT") {
                return field.multiple && field.selectedOptions.length > 0;
            }
            if (field.type === "checkbox" || field.type === "radio") {
                return field.checked && !field.closest(".mdc-switch");
            }
            if (["hidden", "button", "submit", "file"].includes(field.type)) {
                return false;
            }
            return String(field.value || "").trim() !== "";
        });
    }

    /**
     * Shows one section and hides the rest, leaving the review.
     *
     * An unknown key, or one belonging to a section that does not apply to this
     * service type, falls back to the first available section so the editor is
     * never left blank.
     *
     * @param key the section key to show
     * @returns {string|null} the key actually shown
     */
    static activate(key) {
        const sections = RegisteredServiceSections.sections();
        const available = sections.filter(RegisteredServiceSections.isAvailable);
        const target = available.find(section => section.key === key) || available[0] || null;
        sections.forEach(section => {
            const isTarget = target !== null && section.key === target.key;
            section.header.classList.toggle(RegisteredServiceSections.CURRENT_CLASS, isTarget);
            section.panel.classList.toggle(RegisteredServiceSections.CURRENT_CLASS, isTarget);
        });
        if (target !== null) {
            RegisteredServiceSections.store(RegisteredServiceSections.STORAGE_SECTION, target.key);
        }
        RegisteredServiceSections.setReviewing(false);
        RegisteredServiceSections.markCurrentInNav();
        return target ? target.key : null;
    }

    /**
     * Enter or leave the review.
     *
     * @param reviewing true to show the review instead of the current section
     */
    static setReviewing(reviewing) {
        RegisteredServiceSections.reviewing = reviewing;
        $(RegisteredServiceSections.MENU_SELECTOR).toggleClass("is-reviewing", reviewing);
        $(RegisteredServiceSections.REVIEW_SELECTOR).toggleClass("hide", !reviewing);
    }

    /**
     * Open a step on its first section, or the review, scrolled to its top.
     *
     * A step whose sections are all hidden advanced options turns the advanced options on first,
     * through the same switch the operator uses, so the choice is remembered.
     *
     * @param stepKey the key of a step in STEPS, or review
     */
    static showStep(stepKey) {
        const menu = document.querySelector(RegisteredServiceSections.MENU_SELECTOR);
        if (menu) {
            menu.scrollTop = 0;
        }
        if (stepKey === RegisteredServiceSections.REVIEW_STEP) {
            RegisteredServiceSections.setReviewing(true);
            RegisteredServiceSections.render();
            RegisteredServiceSections.focusStepHeading();
            return;
        }
        const step = RegisteredServiceSections.STEPS.find(entry => entry.key === stepKey);
        if (step && RegisteredServiceSections.sectionsOf(step).length === 0
            && RegisteredServiceSections.advancedSectionsOf(step).length > 0) {
            $("#hideAdvancedOptionsButton").trigger("click");
        }
        const first = step ? RegisteredServiceSections.sectionsOf(step)[0] : null;
        if (first) {
            RegisteredServiceSections.activate(first.key);
            RegisteredServiceSections.render();
            RegisteredServiceSections.focusStepHeading();
        }
    }

    /**
     * Redraw whenever the definition preview changes. The preview is regenerated after the fields
     * change, including when an existing definition fills them in without any input event, so this is
     * what keeps the tiles, the step counts and the review current.
     */
    static followDefinition() {
        const element = document.getElementById("wizardServiceEditor");
        if (!element || element.dataset.reviewFollowed === "true" || typeof ace === "undefined") {
            return;
        }
        element.dataset.reviewFollowed = "true";
        ace.edit("wizardServiceEditor").session.on("change", () => {
            RegisteredServiceSections.scheduleRender();
            if (RegisteredServiceSections.reviewing) {
                RegisteredServiceSections.scheduleReview();
            }
        });
    }

    /**
     * Redraw the review on the next frame, at most once per frame.
     */
    static scheduleReview() {
        if (RegisteredServiceSections.reviewScheduled) {
            return;
        }
        RegisteredServiceSections.reviewScheduled = true;
        requestAnimationFrame(() => {
            RegisteredServiceSections.reviewScheduled = false;
            if (RegisteredServiceSections.reviewing) {
                RegisteredServiceSections.renderReview();
            }
        });
    }

    /**
     * The steps that have something to show, followed by the review.
     *
     * @returns {string[]} step keys in order
     */
    static reachableSteps() {
        return [...RegisteredServiceSections.STEPS
            .filter(step => RegisteredServiceSections.sectionsOf(step).length > 0)
            .map(step => step.key), RegisteredServiceSections.REVIEW_STEP];
    }

    /**
     * Move to the next or the previous step that has something to show.
     *
     * @param direction 1 for the next step, -1 for the previous one
     */
    static move(direction) {
        const steps = RegisteredServiceSections.reachableSteps();
        const index = steps.indexOf(RegisteredServiceSections.currentStepKey());
        const target = steps[index + direction];
        if (target) {
            RegisteredServiceSections.showStep(target);
        }
    }

    /**
     * Start the walk over: the first section of the first step.
     */
    static start() {
        RegisteredServiceSections.filterText = "";
        $("#editServiceWizardSectionFilter").val("");
        const first = RegisteredServiceSections.reachableSteps()[0];
        RegisteredServiceSections.showStep(first);
    }

    /**
     * Put focus on the heading of the step on screen, so a keyboard user hears where Next led.
     */
    static focusStepHeading() {
        const heading = RegisteredServiceSections.reviewing
            ? document.getElementById("editServiceWizardReviewTitle")
            : document.getElementById("editServiceWizardStepTitle");
        if (heading && $(heading).is(":visible")) {
            heading.focus({preventScroll: false});
        }
    }

    /**
     * Runs a callback against every section that applies to this service type.
     *
     * Each section is brought on screen before the callback runs, because the
     * editor only renders the current one and browser field validation cannot
     * report on a field that is not displayed. Returning false stops the walk.
     * The caller is responsible for restoring the previously shown section.
     *
     * @param callback invoked with each available section
     */
    static eachAvailableSection(callback) {
        const available = RegisteredServiceSections.sections().filter(RegisteredServiceSections.isAvailable);
        for (const section of available) {
            RegisteredServiceSections.activate(section.key);
            if (callback(section) === false) {
                return;
            }
        }
    }

    /**
     * Writes a value to local storage, ignoring browsers that refuse.
     *
     * @param key the storage key
     * @param value the value to store
     */
    static store(key, value) {
        try {
            window.localStorage.setItem(key, value);
        } catch (error) {
            console.debug("Palantir could not persist the service editor section state", error);
        }
    }

    /**
     * Reads a value from local storage, tolerating browsers that refuse.
     *
     * @param key the storage key
     * @returns {string|null} the stored value, or null
     */
    static read(key) {
        try {
            return window.localStorage.getItem(key);
        } catch (error) {
            return null;
        }
    }

    /**
     * Builds the parts of the section area that survive a re-render.
     *
     * The filter box is created once so that typing in it does not destroy
     * the element being typed into.
     */
    static buildToolbar() {
        const nav = document.querySelector(RegisteredServiceSections.NAV_SELECTOR);
        if (!nav || nav.children.length > 0) {
            return;
        }
        nav.innerHTML = `
            <div class="registered-service-sections__toolbar">
                <div class="registered-service-sections__intro">
                    <h3 id="editServiceWizardStepTitle" tabindex="-1"></h3>
                    <p id="editServiceWizardStepDescription"></p>
                </div>
                <label class="registered-service-sections__search" for="editServiceWizardSectionFilter">
                    <i class="mdi mdi-magnify" aria-hidden="true"></i>
                    <span class="palantir-sr-only">Find a section</span>
                    <input type="search" id="editServiceWizardSectionFilter"
                           autocomplete="off" placeholder="Find a section">
                </label>
            </div>
            <div id="editServiceWizardSectionList" class="registered-service-sections__list"></div>
            <p id="editServiceWizardSectionStatus" class="palantir-sr-only" role="status" aria-live="polite"></p>`;
    }

    /**
     * Redraws the step bar, the tiles of every step and, while reviewing, the review.
     */
    static render() {
        RegisteredServiceSections.renderSteps();
        RegisteredServiceSections.renderTiles();
        RegisteredServiceSections.renderIdentity();
        RegisteredServiceSections.renderFooter();
        if (RegisteredServiceSections.reviewing) {
            RegisteredServiceSections.renderReview();
        }
    }

    /**
     * Redraw on the next frame, at most once per frame, as the operator types.
     */
    static scheduleRender() {
        if (RegisteredServiceSections.renderScheduled) {
            return;
        }
        RegisteredServiceSections.renderScheduled = true;
        requestAnimationFrame(() => {
            RegisteredServiceSections.renderScheduled = false;
            RegisteredServiceSections.renderSteps();
            RegisteredServiceSections.renderTiles();
            RegisteredServiceSections.renderIdentity();
        });
    }

    /**
     * Draws the step bar: the fixed Type step, each step that has sections, and the review. A step whose
     * sections are only hidden as advanced options stays clickable and counts the ones that hold values.
     * On a narrow screen the bar scrolls sideways, and is scrolled to keep the current step in view.
     */
    static renderSteps() {
        const bar = document.querySelector(RegisteredServiceSections.STEPS_SELECTOR);
        if (!bar) {
            return;
        }
        const reachable = RegisteredServiceSections.reachableSteps();
        const currentStep = RegisteredServiceSections.currentStepKey();
        const currentIndex = reachable.indexOf(currentStep);
        const entries = [...RegisteredServiceSections.STEPS, {key: RegisteredServiceSections.REVIEW_STEP, title: "Review"}];
        const items = entries.map((step, position) => {
            const index = reachable.indexOf(step.key);
            const advanced = index < 0 && step.key !== RegisteredServiceSections.REVIEW_STEP
                ? RegisteredServiceSections.advancedSectionsOf(step) : [];
            const empty = index < 0 && advanced.length === 0;
            const state = empty ? "is-empty"
                : index < 0 ? "is-upcoming is-advanced"
                    : index < currentIndex ? "is-done" : index === currentIndex ? "is-current" : "is-upcoming";
            const shown = index < 0 ? advanced : RegisteredServiceSections.sectionsOf(step);
            const count = empty || step.key === RegisteredServiceSections.REVIEW_STEP ? 0
                : shown.filter(RegisteredServiceSections.hasValue).length;
            const hint = count === 0 ? "" : `<span class="registered-service-steps__count">${count} set</span>`;
            const title = advanced.length > 0 ? "title=\"Shows the advanced options\"" : "";
            return `
                <li class="registered-service-steps__item ${state}">
                    <button type="button" class="registered-service-steps__button" data-wizard-step="${step.key}" ${title}
                            ${state === "is-current" ? "aria-current=\"step\"" : ""} ${empty ? "disabled" : ""}>
                        <span class="registered-service-steps__bar" aria-hidden="true"></span>
                        <span class="registered-service-steps__label">
                            <span class="registered-service-steps__number" aria-hidden="true">${position + 2}</span>
                            <span>${RegisteredServiceSections.escape(step.title)}</span>
                            ${hint}
                        </span>
                    </button>
                </li>`;
        }).join("");
        bar.innerHTML = `
            <li class="registered-service-steps__item is-done is-fixed">
                <span class="registered-service-steps__button">
                    <span class="registered-service-steps__bar" aria-hidden="true"></span>
                    <span class="registered-service-steps__label">
                        <span class="registered-service-steps__number" aria-hidden="true"><i class="mdi mdi-check"></i></span>
                        <span>Type</span>
                    </span>
                </span>
            </li>${items}`;
        const current = bar.querySelector(".registered-service-steps__item.is-current");
        if (current && bar.scrollWidth > bar.clientWidth) {
            const left = current.offsetLeft - bar.offsetLeft;
            if (left < bar.scrollLeft || left + current.offsetWidth > bar.scrollLeft + bar.clientWidth) {
                bar.scrollLeft = Math.max(0, left - 16);
            }
        }
    }

    /**
     * Draws a tile for every available section, grouped by step and group.
     *
     * Only the current step's tiles are shown; while the filter holds a term,
     * every matching tile is shown under its step instead.
     */
    static renderTiles() {
        const list = document.getElementById("editServiceWizardSectionList");
        if (!list) {
            return;
        }
        const term = RegisteredServiceSections.filterText.trim().toLowerCase();
        const currentKey = RegisteredServiceSections.currentKey();
        const currentStep = RegisteredServiceSections.currentStepKey();
        let matches = 0;
        const markup = RegisteredServiceSections.STEPS.map(step => {
            const sections = RegisteredServiceSections.sectionsOf(step);
            if (sections.length === 0) {
                return "";
            }
            const groups = RegisteredServiceSections.groupsOf(step, sections);
            const visibleCount = sections.filter(section => RegisteredServiceSections.matches(section, term)).length;
            matches += visibleCount;
            const shown = term === "" ? step.key === currentStep : visibleCount > 0;
            const body = groups.map(entry => {
                const tiles = entry.sections.map(section => {
                    const filled = RegisteredServiceSections.hasValue(section);
                    const selected = section.key === currentKey;
                    const hidden = !RegisteredServiceSections.matches(section, term);
                    return `
                        <li${hidden ? " hidden" : ""}>
                            <button type="button" class="registered-service-sections__link${selected ? " is-selected" : ""}${filled ? " has-value" : ""}"
                                    data-section-key="${section.key}" ${selected ? "aria-current=\"true\"" : ""}>
                                <span class="registered-service-sections__dot" aria-hidden="true"></span>
                                <span class="registered-service-sections__link-text">${RegisteredServiceSections.escape(RegisteredServiceSections.titleOf(section))}</span>
                                <span class="registered-service-sections__state">${filled ? "Has values" : "Not set"}</span>
                            </button>
                        </li>`;
                }).join("");
                const heading = groups.length > 1 || term !== ""
                    ? `<h4 class="registered-service-sections__group-heading">${RegisteredServiceSections.escape(entry.group)}</h4>` : "";
                return `<div class="registered-service-sections__group">${heading}<ul class="registered-service-sections__tiles">${tiles}</ul></div>`;
            }).join("");
            const single = sections.length === 1 && term === "";
            return `
                <div class="registered-service-sections__step${single ? " is-single" : ""}" data-step="${step.key}"${shown ? "" : " hidden"}>
                    ${term !== "" ? `<p class="registered-service-sections__step-name">${RegisteredServiceSections.escape(step.title)}</p>` : ""}
                    ${body}
                </div>`;
        }).join("");
        list.innerHTML = markup + (term !== "" && matches === 0
            ? "<p class=\"registered-service-sections__empty\">No section matches that name.</p>" : "");

        const step = RegisteredServiceSections.STEPS.find(entry => entry.key === currentStep);
        $("#editServiceWizardStepTitle").text(step ? step.title : "Review");
        $("#editServiceWizardStepDescription").text(step ? step.description : "");
        $("#editServiceWizardSectionStatus").text(term === "" ? "" : `${matches} section${matches === 1 ? "" : "s"} found`);
    }

    /**
     * Whether a section's name, or its group's, contains the filter term.
     *
     * @param section an entry from sections()
     * @param term the lower-cased filter term
     * @returns {boolean} true when the section matches, or when there is no term
     */
    static matches(section, term) {
        return term === "" || RegisteredServiceSections.titleOf(section).toLowerCase().includes(term)
            || RegisteredServiceSections.groupOf(section).toLowerCase().includes(term);
    }

    /**
     * Shows the application type and name above the steps.
     */
    static renderIdentity() {
        const serviceClass = $("#editServiceWizardForm").data("service-class");
        if (!serviceClass) {
            return;
        }
        const protocol = applicationProtocolOf(serviceClass);
        $("#editServiceWizardTypeBadge")
            .attr("class", `palantir-protocol-badge is-${protocol.key}`)
            .text(protocol.short);
        $("#editServiceWizardTypeName").text(protocol.label);
        const name = String($("#registeredServiceName").val() || "").trim();
        $("#editServiceWizardServiceName").text(name || "Unnamed application");
    }

    /**
     * Enables Back and Next for where the walk stands; Next names the step it leads to.
     */
    static renderFooter() {
        const steps = RegisteredServiceSections.reachableSteps();
        const index = steps.indexOf(RegisteredServiceSections.currentStepKey());
        const next = steps[index + 1];
        const nextTitle = next === RegisteredServiceSections.REVIEW_STEP ? "Review"
            : RegisteredServiceSections.STEPS.find(step => step.key === next)?.title;
        $("#previousServiceWizardStep").prop("disabled", index <= 0);
        $("#nextServiceWizardStep")
            .toggleClass("hide", next === undefined)
            .find("span").text(nextTitle ? `Next: ${nextTitle}` : "Next");
    }

    /**
     * Draws the review: what the definition currently says, and what each step holds.
     */
    static renderReview() {
        const review = document.querySelector(RegisteredServiceSections.REVIEW_SELECTOR);
        if (!review) {
            return;
        }
        let definition;
        try {
            definition = JSON.parse(ace.edit("wizardServiceEditor").getValue() || "{}");
        } catch (error) {
            definition = {};
        }
        const scalar = Object.entries(definition)
            .filter(([key, value]) => key !== "@class" && value !== null && ["string", "number", "boolean"].includes(typeof value));
        const policies = Object.entries(definition)
            .filter(([key, value]) => value !== null && typeof value === "object" && !Array.isArray(value))
            .map(([key]) => key);
        const rows = scalar.map(([key, value]) => `
            <div class="registered-service-review__row">
                <dt>${RegisteredServiceSections.escape(RegisteredServiceSections.humanize(key))}</dt>
                <dd>${RegisteredServiceSections.escape(value)}</dd>
            </div>`).join("");
        const steps = RegisteredServiceSections.STEPS
            .filter(step => RegisteredServiceSections.sectionsOf(step).length > 0)
            .map(step => {
                const sections = RegisteredServiceSections.sectionsOf(step);
                const filled = sections.filter(RegisteredServiceSections.hasValue);
                const links = filled.map(section => `
                    <li><button type="button" class="registered-service-review__link" data-review-section="${section.key}">
                        ${RegisteredServiceSections.escape(RegisteredServiceSections.titleOf(section))}
                    </button></li>`).join("");
                return `
                    <div class="registered-service-review__step">
                        <div class="registered-service-review__step-head">
                            <h4>${RegisteredServiceSections.escape(step.title)}</h4>
                            <button type="button" class="registered-service-review__edit" data-review-step="${step.key}"
                                    aria-label="Edit ${RegisteredServiceSections.escape(step.title)}">Edit</button>
                        </div>
                        <p>Values in ${filled.length} of ${sections.length} section${sections.length === 1 ? "" : "s"}</p>
                        ${links ? `<ul>${links}</ul>` : ""}
                    </div>`;
            }).join("");
        review.innerHTML = `
            <div class="registered-service-sections__intro">
                <h3 id="editServiceWizardReviewTitle" tabindex="-1">Review</h3>
                <p>Check what the definition says, validate it, then save.</p>
            </div>
            <div class="registered-service-review__grid">
                <div class="registered-service-review__card">
                    <h4>Definition</h4>
                    ${rows ? `<dl>${rows}</dl>` : "<p>The definition holds no values yet.</p>"}
                    ${policies.length > 0 ? `<p class="registered-service-review__policies">Includes ${policies.map(key => RegisteredServiceSections.escape(RegisteredServiceSections.humanize(key))).join(", ")}.</p>` : ""}
                </div>
                <div class="registered-service-review__steps">${steps}</div>
            </div>`;
    }

    /**
     * Turns a property name into words: evaluationOrder becomes Evaluation order.
     *
     * @param key the property name
     * @returns {string} the readable name
     */
    static humanize(key) {
        const words = String(key).replace(/([a-z0-9])([A-Z])/g, "$1 $2").replace(/[_-]+/g, " ").toLowerCase();
        return words.charAt(0).toUpperCase() + words.slice(1);
    }

    /**
     * Keeps the selected tile in step with the section on screen.
     */
    static markCurrentInNav() {
        const currentKey = RegisteredServiceSections.currentKey();
        document.querySelectorAll("[data-section-key]").forEach(link => {
            const selected = link.getAttribute("data-section-key") === currentKey;
            link.classList.toggle("is-selected", selected);
            if (selected) {
                link.setAttribute("aria-current", "true");
            } else {
                link.removeAttribute("aria-current");
            }
        });
    }

    /**
     * Escapes text for insertion into markup.
     *
     * @param value the text to escape
     * @returns {string} the escaped text
     */
    static escape(value) {
        return String(value).replace(/[&<>"']/g, character => ({
            "&": "&amp;", "<": "&lt;", ">": "&gt;", "\"": "&quot;", "'": "&#39;"
        })[character]);
    }

    /**
     * Rebuilds the steps and tiles and makes sure a valid section is on screen.
     *
     * Called after the service type is chosen, after advanced options are
     * toggled, and when the JSON editor is shown or hidden. A review in progress
     * stays on screen.
     */
    static refresh() {
        if (document.querySelector(RegisteredServiceSections.PANELS_SELECTOR) === null) {
            return;
        }
        RegisteredServiceSections.buildToolbar();
        const reviewing = RegisteredServiceSections.reviewing;
        const current = RegisteredServiceSections.currentKey()
            || RegisteredServiceSections.read(RegisteredServiceSections.STORAGE_SECTION);
        RegisteredServiceSections.activate(RegisteredServiceSections.nearestAvailable(current));
        RegisteredServiceSections.setReviewing(reviewing);
        RegisteredServiceSections.render();
    }

    /**
     * The section to show in place of one that may no longer apply.
     *
     * Hiding the advanced options can take away the section on screen. Rather than going back to the
     * first section of all, the walk stays in that section's step when the step still has a section,
     * and otherwise falls back to the nearest earlier step that has one.
     *
     * @param key the key of the section that was on screen
     * @returns {string|null} the key to show, or null to let activate pick the first section
     */
    static nearestAvailable(key) {
        const section = RegisteredServiceSections.sections().find(entry => entry.key === key);
        if (!section || RegisteredServiceSections.isAvailable(section)) {
            return key;
        }
        const steps = RegisteredServiceSections.STEPS;
        const index = steps.indexOf(RegisteredServiceSections.stepOf(section));
        for (let position = index; position >= 0; position--) {
            const fallback = RegisteredServiceSections.sectionsOf(steps[position])[0];
            if (fallback) {
                return fallback.key;
            }
        }
        return null;
    }

    /**
     * Wires the steps up. Safe to call more than once.
     */
    static initialize() {
        const nav = document.querySelector(RegisteredServiceSections.NAV_SELECTOR);
        if (!nav || nav.dataset.initialized === "true") {
            return;
        }
        nav.dataset.initialized = "true";
        RegisteredServiceSections.buildToolbar();

        nav.addEventListener("click", event => {
            const link = event.target.closest("[data-section-key]");
            if (link && nav.contains(link)) {
                RegisteredServiceSections.activate(link.getAttribute("data-section-key"));
                RegisteredServiceSections.render();
                document.querySelector(RegisteredServiceSections.PANELS_SELECTOR)?.scrollIntoView({block: "nearest", behavior: "smooth"});
            }
        });

        nav.addEventListener("input", event => {
            const filter = event.target.closest("#editServiceWizardSectionFilter");
            if (filter && nav.contains(filter)) {
                RegisteredServiceSections.filterText = filter.value || "";
                RegisteredServiceSections.renderTiles();
            }
        });

        $(RegisteredServiceSections.STEPS_SELECTOR).on("click", "[data-wizard-step]", event => {
            RegisteredServiceSections.showStep($(event.currentTarget).data("wizard-step"));
        });

        $(RegisteredServiceSections.REVIEW_SELECTOR)
            .on("click", "[data-review-section]", event => {
                RegisteredServiceSections.activate($(event.currentTarget).data("review-section"));
                RegisteredServiceSections.render();
                RegisteredServiceSections.focusStepHeading();
            })
            .on("click", "[data-review-step]", event => {
                RegisteredServiceSections.showStep($(event.currentTarget).data("review-step"));
            });

        $("#previousServiceWizardStep").on("click", () => RegisteredServiceSections.move(-1));
        $("#nextServiceWizardStep").on("click", () => RegisteredServiceSections.move(1));

        $(RegisteredServiceSections.PANELS_SELECTOR).on("input change", () => RegisteredServiceSections.scheduleRender());
        RegisteredServiceSections.followDefinition();

        RegisteredServiceSections.render();
    }

}
