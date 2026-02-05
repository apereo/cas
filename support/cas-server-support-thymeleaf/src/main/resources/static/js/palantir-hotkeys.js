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
            if (currentActiveTab === Tabs.CONFIGURATION.index && mutablePropertySourcesAvailable) {
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

    $(`nav.sidebar-navigation ul li:visible`).each(function () {
        const index = $(this).data("tab-index");
        const tab = Object.values(Tabs).find(
            t => t instanceof PalantirDashboardTab && t.index === index
        );
        if (tab && tab.shortcut) {
            $("#palantirShortcutsPanel").append(`
                <div class="shortcut-item">
                  <kbd class="key">${tab.shortcut}</kbd>
                  <span>${tab.name}</span>
                </div>
              `);
        }
    });
    $("#palantirShortcutsPanel").append(`
        <div class="shortcut-item">
          <kbd class="key">w</kbd>
          <span>Application Wizard Dialog</span>
        </div>
      `);
}
