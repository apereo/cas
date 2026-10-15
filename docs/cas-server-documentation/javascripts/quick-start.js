/* Progressive enhancement: the source guide remains readable without JavaScript. */
(() => {
    const wizard = document.getElementById("cas-quick-start");
    if (!wizard) {
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
    const choices = Array.from(document.querySelectorAll(".quick-start-features input"));
    const selectionControls = document.createElement("div");
    selectionControls.className = "quick-start-selection";
    selectionControls.innerHTML = "<button type=\"button\">Check All</button><button type=\"button\">Uncheck All</button><span role=\"status\" aria-live=\"polite\"></span>";
    document.querySelector(".quick-start-features").before(selectionControls);
    const [checkAll, uncheckAll] = selectionControls.querySelectorAll("button");
    const features = Array.from(document.querySelectorAll("[data-quick-feature]"));
    let current = 0;
    let showAll = false;
    const updateFeatures = () => {
        const selected = choices.filter((input) => input.checked).map((input) => input.value);
        checkAll.disabled = selected.length === choices.length;
        uncheckAll.disabled = selected.length === 0;
        selectionControls.querySelector("span").textContent = `${selected.length} of ${choices.length} features selected`;
        features.forEach((block) => { block.hidden = !selected.includes(block.dataset.quickFeature); });
        const summary = document.querySelector(".quick-start-summary");
        summary.hidden = false;
        summary.textContent = `Your overlay: CAS + JSON service registry${choices.filter((input) => input.checked)
            .map((input) => ` + ${input.closest("label").querySelector("strong").textContent}`).join("")}.`;
        const url = new URL(location.href);
        if (selected.length) {
            url.searchParams.set("features", selected.join(","));
        } else {
            url.searchParams.delete("features");
        }
        history.replaceState(history.state, "", url);
    };
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
        wizard.querySelector("[role=\"status\"]").textContent = `Step ${current + 1} of 8 · ${headings[current].textContent.replace(/^\d+\. /, "")}`;
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
    const restore = () => {
        const selected = (new URL(location.href).searchParams.get("features") || "").split(",");
        choices.forEach((input) => { input.checked = selected.includes(input.value); });
        let target;
        try {
            target = document.getElementById(decodeURIComponent(location.hash.slice(1)));
        } catch {
            target = null;
        }
        const feature = target?.closest("[data-quick-feature]");
        if (feature) {
            choices.find((input) => input.value === feature.dataset.quickFeature).checked = true;
        }
        updateFeatures();
        const index = steps.findIndex((step) => step.contains(target));
        show(index < 0 ? 0 : index);
        target?.scrollIntoView({block: "start"});
    };
    choices.forEach((input) => input.addEventListener("change", updateFeatures));
    [checkAll, uncheckAll].forEach((button) => button.addEventListener("click", () => {
        choices.forEach((input) => { input.checked = button === checkAll; });
        updateFeatures();
    }));
    back.addEventListener("click", () => show(Math.max(0, current - 1), true));
    next.addEventListener("click", () => show(Math.min(7, current + 1), true));
    wizard.querySelector(".quick-start-view").addEventListener("click", (event) => {
        showAll = !showAll;
        event.currentTarget.textContent = showAll ? "Show one step" : "Show all steps";
        event.currentTarget.setAttribute("aria-pressed", String(showAll));
        show(current);
    });
    window.addEventListener("hashchange", restore);
    window.addEventListener("popstate", restore);
    restore();
})();
