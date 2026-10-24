/*
 * Progressive enhancement: the source guide remains readable without JavaScript.
 * The feature selection is page state kept in the "features" query parameter of the current history entry;
 * moving through history changes the step, never the selection.
 * A section marked data-quick-feature="a b" is shown when any of the named features is selected.
 * A checkbox may carry data-requires and data-excludes, each a space-separated list of feature values: checking it
 * selects what it requires and clears what it excludes, and clearing a feature clears whatever requires it.
 */
(() => {
    const wizard = document.getElementById("cas-quick-start");
    const fieldset = document.querySelector(".quick-start-features");
    const output = document.querySelector(".quick-start-output");
    if (!wizard || !fieldset || !output) {
        return;
    }
    const headings = Array.from(wizard.parentElement.children).filter((element) =>
        element.matches("h2") && /^[1-8]\. /.test(element.textContent));
    if (headings.length !== 8) {
        return;
    }
    const steps = headings.map((heading) => {
        const section = document.createElement("section");
        section.className = "quick-start-step";
        section.setAttribute("aria-labelledby", heading.id);
        heading.before(section);
        section.append(heading);
        while (section.nextSibling && !(section.nextSibling.nodeType === 1 && section.nextSibling.matches("h2"))) {
            section.append(section.nextSibling);
        }
        heading.tabIndex = -1;
        return section;
    });
    wizard.innerHTML = `<div class="quick-start-top"><p role="status" aria-live="polite"></p>
        <button type="button" class="quick-start-view" aria-pressed="false">Show all steps</button></div>
        <div class="quick-start-notice" hidden><p role="status" aria-live="polite"></p>
        <button type="button">Change features</button></div>
        <progress max="8" value="1" aria-label="Current step"></progress>
        <nav aria-label="Quick-start steps"><ol></ol></nav>`;
    const list = wizard.querySelector("ol");
    const buttons = headings.map((heading, index) => {
        const item = document.createElement("li");
        const button = document.createElement("button");
        button.type = "button";
        button.textContent = heading.textContent;
        button.addEventListener("click", () => show(index, true));
        item.append(button);
        list.append(item);
        return button;
    });
    const footer = document.createElement("div");
    footer.className = "quick-start-controls";
    footer.innerHTML = "<button type=\"button\">Back</button><span></span><button type=\"button\">Next step →</button>";
    steps.at(-1).after(footer);
    const [back, next] = footer.querySelectorAll("button");
    const choices = Array.from(fieldset.querySelectorAll("input"));
    const selectionControls = document.createElement("div");
    selectionControls.className = "quick-start-selection";
    selectionControls.innerHTML = "<button type=\"button\">Check All</button><button type=\"button\">Uncheck All</button><span role=\"status\" aria-live=\"polite\"></span>";
    fieldset.before(selectionControls);
    const [checkAll, uncheckAll] = selectionControls.querySelectorAll("button");
    const features = Array.from(document.querySelectorAll("[data-quick-feature]"));
    const featuresStep = steps.findIndex((step) => step.contains(fieldset));
    const notice = wizard.querySelector(".quick-start-notice");
    const summary = output.querySelector(".quick-start-summary");
    const change = document.createElement("button");
    change.type = "button";
    change.textContent = "Change features";
    summary.append(change);
    output.hidden = false;
    const sectionHeadings = steps.flatMap((step) => Array.from(step.querySelectorAll("h2, h3")));
    const propertyBlocks = steps.slice(2, 5).flatMap((step) => Array.from(step.querySelectorAll(".cas-settings-linked")));
    const baseModules = (fieldset.dataset.modules || "").split(/\s+/).filter(Boolean);
    let current = 0;
    let showAll = false;

    /**
     * The checkbox of a feature.
     *
     * @param value the feature value
     * @returns the checkbox, or undefined
     */
    const choiceOf = (value) => choices.find((input) => input.value === value);

    /**
     * The checkboxes a checkbox names in one of its rule attributes.
     *
     * @param input the checkbox
     * @param rule the dataset key, requires or excludes
     * @returns the named checkboxes that exist
     */
    const ruleOf = (input, rule) => (input.dataset[rule] || "").split(/\s+/).map(choiceOf).filter(Boolean);

    /**
     * The display name of a feature.
     *
     * @param input the checkbox
     * @returns the label's title
     */
    const labelOf = (input) => input.closest("label").querySelector("strong").textContent;

    /**
     * Applies the selection rules after a checkbox changed, following every change it causes.
     *
     * @param input the checkbox that changed
     * @returns one sentence per automatic change
     */
    const enforce = (input) => {
        const messages = [];
        const pending = [input];
        let budget = choices.length * choices.length;
        while (pending.length && budget-- > 0) {
            const changed = pending.shift();
            if (changed.checked) {
                ruleOf(changed, "requires").filter((required) => !required.checked).forEach((required) => {
                    required.checked = true;
                    messages.push(`Also selected ${labelOf(required)}, which ${labelOf(changed)} uses.`);
                    pending.push(required);
                });
                const conflicts = new Set([...ruleOf(changed, "excludes"),
                    ...choices.filter((other) => ruleOf(other, "excludes").includes(changed))]);
                [...conflicts].filter((excluded) => excluded.checked).forEach((excluded) => {
                    excluded.checked = false;
                    messages.push(`Removed ${labelOf(excluded)}, which cannot be combined with ${labelOf(changed)}.`);
                    pending.push(excluded);
                });
            } else {
                choices.filter((dependent) => dependent.checked && ruleOf(dependent, "requires").includes(changed)).forEach((dependent) => {
                    dependent.checked = false;
                    messages.push(`Removed ${labelOf(dependent)}, which needs ${labelOf(changed)}.`);
                    pending.push(dependent);
                });
            }
        }
        return messages;
    };

    /**
     * Makes a selection read from elsewhere, such as the address, consistent; earlier features win conflicts.
     */
    const normalize = () => choices.forEach((input) => {
        if (input.checked) {
            enforce(input);
        }
    });

    /**
     * The unchecked features that can be added without removing a selected one.
     *
     * @returns the addable checkboxes, in page order
     */
    const addable = () => choices.filter((input) => !input.checked
        && !ruleOf(input, "excludes").some((excluded) => excluded.checked)
        && !choices.some((other) => other.checked && ruleOf(other, "excludes").includes(input)));

    /**
     * The code element of a rendered fenced block, with or without Rouge line numbers.
     *
     * @param block the element that contains the rendered block
     * @returns the element holding the code text, or null
     */
    const codeOf = (block) => block?.querySelector(".rouge-code pre") || block?.querySelector("pre code") || null;
    const overlayCommand = codeOf(document.querySelector("[data-quick-output=\"overlay\"]"))?.textContent || "";
    const overlayBase = (overlayCommand.match(/dependencies=(\S+)/)?.[1] || "").split(",").filter(Boolean);

    /**
     * Replaces the text of a generated block, renumbers its gutter and links its settings again.
     *
     * @param name the data-quick-output name of the block
     * @param text the new code text, ending with a newline
     */
    const setOutput = (name, text) => {
        const block = document.querySelector(`[data-quick-output="${name}"]`);
        const code = codeOf(block);
        if (!code) {
            return;
        }
        code.textContent = text;
        const gutter = block.querySelector(".rouge-gutter pre");
        if (gutter) {
            gutter.textContent = text.trimEnd().split("\n").map((line, index) => `${index + 1}\n`).join("");
        }
        const linked = block.querySelector(".cas-settings-linked");
        if (linked && typeof linkCasSettingsBlock === "function") {
            linkCasSettingsBlock(linked);
        }
    };

    /**
     * The features a conditional section belongs to.
     *
     * @param block an element carrying data-quick-feature
     * @returns the feature values it names
     */
    const featuresOf = (block) => block.dataset.quickFeature.split(/\s+/).filter(Boolean);

    /**
     * The nearest step or recipe heading before an element, without its step number.
     *
     * @param element any element inside the steps
     * @returns the heading text
     */
    const headingBefore = (element) => sectionHeadings
        .filter((heading) => heading.compareDocumentPosition(element) & Node.DOCUMENT_POSITION_FOLLOWING)
        .at(-1)?.textContent.trim().replace(/^\d+\. /, "") || "CAS";

    /**
     * Rebuilds the overlay command, the dependencies block and cas.properties from the visible recipes.
     *
     * @param selected the selected checkboxes
     */
    const updateOutputs = (selected) => {
        const modules = [...baseModules, ...selected.map((input) => input.dataset.module).filter(Boolean)];
        const initializr = [...new Set([...overlayBase, ...modules.map((module) => module.replace(/^cas-server-/, ""))])];
        if (overlayCommand) {
            setOutput("overlay", overlayCommand.replace(/dependencies=\S+/, `dependencies=${initializr.join(",")}`));
        }
        setOutput("dependencies", `dependencies {\n${modules.map((module) =>
            `    implementation "org.apereo.cas:${module}"`).join("\n")}\n}\n`);
        setOutput("properties", `${propertyBlocks
            .filter((block) => !block.closest("[data-quick-feature][hidden]"))
            .map((block) => `# ${headingBefore(block)}\n${codeOf(block)?.textContent.trimEnd() || ""}`)
            .join("\n\n")}\n`);
    };

    /**
     * Applies the checkboxes: shows matching recipes, refreshes the summary and generated blocks,
     * and records the selection in the current history entry.
     *
     * @param messages sentences describing automatic changes to announce with the count
     */
    const updateFeatures = (messages = []) => {
        const selectedInputs = choices.filter((input) => input.checked);
        const selected = selectedInputs.map((input) => input.value);
        checkAll.disabled = addable().length === 0;
        uncheckAll.disabled = selected.length === 0;
        selectionControls.querySelector("span").textContent =
            [`${selected.length} of ${choices.length} features selected.`, ...messages].join(" ");
        features.forEach((block) => {
            block.hidden = !featuresOf(block).some((feature) => selected.includes(feature));
        });
        summary.querySelector("p").textContent = `Your overlay: CAS + JSON service registry${selectedInputs
            .map((input) => ` + ${labelOf(input)}`).join("")}.`;
        updateOutputs(selectedInputs);
        const url = new URL(location.href);
        if (selected.length) {
            url.searchParams.set("features", selected.join(","));
        } else {
            url.searchParams.delete("features");
        }
        url.search = url.search.replace(/%2C/gi, ",");
        history.replaceState(history.state, "", url);
    };

    /**
     * Displays one step, or all of them, and optionally records the step in history.
     *
     * @param index the zero-based step
     * @param navigate whether to push a history entry and move focus to the step
     */
    const show = (index, navigate = false) => {
        current = index;
        steps.forEach((step, i) => { step.hidden = !showAll && i !== current; });
        buttons.forEach((button, i) => {
            if (i === current) {
                button.setAttribute("aria-current", "step");
            } else {
                button.removeAttribute("aria-current");
            }
        });
        wizard.querySelector(".quick-start-top [role=\"status\"]").textContent =
            `Step ${current + 1} of 8 · ${headings[current].textContent.replace(/^\d+\. /, "")}`;
        wizard.querySelector("progress").value = current + 1;
        back.disabled = current === 0;
        next.disabled = current === steps.length - 1;
        footer.querySelector("span").textContent = current === 7 ? "Ready to plan your deployment" : `${current + 1} / 8`;
        if (navigate) {
            history.pushState(history.state, "", `#${headings[current].id}`);
            headings[current].focus({preventScroll: true});
            wizard.scrollIntoView({block: "start"});
        }
    };

    /**
     * Opens the feature choices in step 2.
     */
    const changeFeatures = () => {
        notice.hidden = true;
        show(featuresStep, true);
        fieldset.scrollIntoView({block: "start"});
        choices[0]?.focus({preventScroll: true});
    };

    /**
     * Shows the step holding the location hash. A link into a recipe whose features are all unselected
     * selects the first of them and says so.
     *
     * @param initial whether the selection should be read from the address, which only the first load does
     */
    const restore = (initial) => {
        if (initial) {
            const selected = (new URL(location.href).searchParams.get("features") || "").split(",");
            choices.forEach((input) => { input.checked = selected.includes(input.value); });
            normalize();
        }
        let target;
        try {
            target = document.getElementById(decodeURIComponent(location.hash.slice(1)));
        } catch {
            target = null;
        }
        const recipe = target?.closest("[data-quick-feature]");
        const candidates = recipe ? featuresOf(recipe).map((feature) => choices.find((input) => input.value === feature)).filter(Boolean) : [];
        if (candidates.length && !candidates.some((input) => input.checked)) {
            candidates[0].checked = true;
            notice.querySelector("p").textContent = [
                `Added ${labelOf(candidates[0])} to your features because this link points to its instructions.`,
                ...enforce(candidates[0])
            ].join(" ");
            notice.hidden = false;
        }
        updateFeatures();
        const index = steps.findIndex((step) => step.contains(target));
        show(index < 0 ? 0 : index);
        target?.scrollIntoView({block: "start"});
    };

    choices.forEach((input) => input.addEventListener("change", () => {
        notice.hidden = true;
        updateFeatures(enforce(input));
    }));
    checkAll.addEventListener("click", () => {
        const messages = [];
        for (let next = addable()[0]; next; next = addable()[0]) {
            next.checked = true;
            messages.push(...enforce(next));
        }
        notice.hidden = true;
        updateFeatures(messages);
    });
    uncheckAll.addEventListener("click", () => {
        choices.forEach((input) => { input.checked = false; });
        notice.hidden = true;
        updateFeatures();
    });
    back.addEventListener("click", () => show(Math.max(0, current - 1), true));
    next.addEventListener("click", () => show(Math.min(7, current + 1), true));
    notice.querySelector("button").addEventListener("click", changeFeatures);
    change.addEventListener("click", changeFeatures);
    wizard.querySelector(".quick-start-view").addEventListener("click", (event) => {
        showAll = !showAll;
        event.currentTarget.textContent = showAll ? "Show one step" : "Show all steps";
        event.currentTarget.setAttribute("aria-pressed", String(showAll));
        show(current);
    });
    window.addEventListener("popstate", () => restore(false));
    restore(true);
})();
