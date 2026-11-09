async function initializeHotKeyOperations() {
    let shortcuts = Tabs.values()
        .filter(tab => tab.shortcut)
        .map(tab => tab.shortcut)
        .join(",");
    shortcuts += ",w,n,esc";

    hotkeys(shortcuts, function (event, handler) {
        const key = handler.key.toLowerCase();
        switch (key) {
        case "esc":
            event.preventDefault();
            closeAllDialogs();
            break;
        case "w":
            event.preventDefault();
            $(`nav.sidebar-navigation ul li[data-tab-index=${Tabs.APPLICATIONS.index}]:visible`).click();
            openRegisteredServiceWizardDialog();
            break;
        case "n":
            if (currentActiveTab === Tabs.CONFIGURATION.index && PalantirDashboardConfiguration.mutablePropertySourcesAvailable()) {
                event.preventDefault();
                $("#newConfigPropertyButton:visible").click();
            }
            if (currentActiveTab === Tabs.LOGGING.index) {
                event.preventDefault();
                $("#newLoggerButton:visible").click();
            }
            if (currentActiveTab === Tabs.AUTHENTICATION.index && $("#delegatedclients").is(":visible")) {
                event.preventDefault();
                $("#newExternalIdentityProvider:visible").click();
            }

            if (currentActiveTab === Tabs.AUTHENTICATION.index && $("#authenticationhandlers").is(":visible")) {
                event.preventDefault();
                $("#newAuthenticationHandlerButton:visible").click();
            }
            if (currentActiveTab === Tabs.PERSON_DIRECTORY.index && $("#attributedefinitions-tab").is(":visible")) {
                event.preventDefault();
                $("#newAttributeDefinitionButton:visible").click();
            }
            if (currentActiveTab === Tabs.PERSON_DIRECTORY.index && $("#attributerepositories-tab").is(":visible")) {
                const newAttributeRepositoryButton = $("#newAttributeRepositoryButton:visible");
                if (newAttributeRepositoryButton.length > 0) {
                    event.preventDefault();
                    newAttributeRepositoryButton.click();
                }
            }
            if (currentActiveTab === Tabs.MULTITENANCY.index && $("#registeredTenantsTab").is(":visible")) {
                event.preventDefault();
                $("#newTenantButton:visible").click();
            }
            if (currentActiveTab === Tabs.ACCESS_STRATEGY.index
                && $("#heimdall-tab").is(":visible")
                && $("#newHeimdallResourceDialog").length === 0) {
                const newResourceButton = $("#newHeimdallResourceButton:visible");
                if (newResourceButton.length > 0) {
                    event.preventDefault();
                    newResourceButton.click();
                }
            }
            if (currentActiveTab === Tabs.APPLICATIONS.index) {
                event.preventDefault();
                openRegisteredServiceWizardDialog();
            }
            
            break;
        default:
            const tab = Object.values(Tabs).find(
                t => t instanceof PalantirDashboardTab && t.shortcut === handler.key
            );
            if (tab) {
                event.preventDefault();
                $(`nav.sidebar-navigation ul li[data-tab-index=${tab.index}]:visible`).click();
            }
        }
    });

    $("#palantirShortcutsPanel").html(renderPalantirShortcuts());
}

/**
 * The keyboard shortcuts of Palantir, grouped: the pages the navigation shows, the actions, the search palette,
 * and forms and dialogs. Built from the navigation each time, so pages hidden for lack of endpoints are left out.
 *
 * @returns {Array<{title: string, items: Array<{keys: Array<Array<string>>, label: string}>}>} the groups; each item
 *          lists one or more alternative key combinations
 */
function palantirShortcutGroups() {
    const mac = /Mac|iPhone|iPad/.test(navigator.platform || navigator.userAgent);
    const pages = $("nav.sidebar-navigation ul li[data-tab-index]:visible").toArray()
        .map(item => {
            const index = Number($(item).data("tab-index"));
            const tab = Object.values(Tabs).find(entry => entry instanceof PalantirDashboardTab && entry.index === index);
            return tab?.shortcut
                ? {keys: [[tab.shortcut]], label: $(item).find(".sidebar-navigation-label").first().text().trim() || tab.name}
                : null;
        })
        .filter(Boolean);
    return [
        {
            title: "Anywhere",
            items: [
                {keys: [[mac ? "⌘" : "Ctrl", "K"], ["/"]], label: "Search pages, sections, applications and actions"},
                {keys: [["?"]], label: "Show these keyboard shortcuts"},
                {keys: [["w"]], label: "Create an application in the wizard"},
                {keys: [["n"]], label: "Create an item on the current page: an application, property, logger, handler, attribute definition or repository, tenant or access resource"},
                {keys: [["Esc"]], label: "Close open dialogs; Palantir asks first when an application has unsaved changes"}
            ]
        },
        {
            title: "Search palette",
            items: [
                {keys: [["↑"], ["↓"]], label: "Move between results"},
                {keys: [["Enter"]], label: "Open the selected result"},
                {keys: [["Esc"]], label: "Close the palette"}
            ]
        },
        {
            title: "Forms, tables and dialogs",
            items: [
                {keys: [["Enter"]], label: "Submit the form you are typing in"},
                {keys: [["Tab"], ["Shift", "Tab"]], label: "Move to the next or previous control; focus stays inside an open dialog"},
                {keys: [["Enter"]], label: "Sort by the focused column header, or choose the focused row"},
                {keys: [["Shift", "F10"]], label: "Open the actions of the focused row, like a right click"}
            ]
        },
        {
            title: "Pages",
            items: pages
        }
    ].filter(group => group.items.length > 0);
}

/**
 * Write the keyboard shortcuts as a list of groups.
 *
 * @returns {string} the markup
 */
function renderPalantirShortcuts() {
    const key = combination => combination.map(name => `<kbd class="key">${escapePalantirMarkup(name)}</kbd>`).join("<span aria-hidden=\"true\">+</span>");
    return palantirShortcutGroups().map(group => `
        <section class="palantir-shortcuts__group${group.title === "Pages" ? " is-pages" : ""}" aria-label="${escapePalantirMarkup(group.title)}">
            <h4>${escapePalantirMarkup(group.title)}</h4>
            <dl class="palantir-shortcuts__list">${group.items.map(item => `
                <div class="palantir-shortcuts__item">
                    <dt>${item.keys.map(key).join("<span class=\"palantir-shortcuts__or\">or</span>")}</dt>
                    <dd>${escapePalantirMarkup(item.label)}</dd>
                </div>`).join("")}
            </dl>
        </section>`).join("");
}

/**
 * Open the keyboard shortcuts overlay.
 */
function openPalantirShortcuts() {
    let dialog = $("#palantirShortcutsDialog");
    if (dialog.length === 0) {
        dialog = $("<div>", {id: "palantirShortcutsDialog", "class": "palantir-shortcuts", title: "Keyboard shortcuts"}).appendTo("body");
        dialog.dialog({
            autoOpen: false,
            modal: true,
            classes: {"ui-dialog": "palantir-shortcuts-dialog"},
            buttons: [{text: "Close", click: () => dialog.dialog("close")}]
        });
    }
    dialog.html(renderPalantirShortcuts()).attr({"tabindex": "0", "aria-label": "Keyboard shortcuts"});
    dialog.dialog("option", "width", Math.min(900, $(window).width() - 32));
    dialog.dialog("option", "maxHeight", $(window).height() - 48);
    dialog.dialog("option", "position", {my: "center", at: "center", of: window});
    dialog.dialog("open");
}
