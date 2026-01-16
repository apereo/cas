let notyf = null;

async function initializePalantir() {
    try {
        setTimeout(() => {
            initializeCasFeatures().then(() => {
                let visibleCount = processNavigationTabs();
                if (visibleCount === 0) {
                    $("#dashboard").hide();
                    Swal.fire({
                        title: "Palantir is unavailable!",
                        text: `Palantir requires a number of actuator endpoints to be enabled and exposed, 
                            and your CAS deployment fails to do so.`,
                        icon: "warning",
                        showConfirmButton: false
                    });
                } else {
                    let selectedTab = window.localStorage.getItem("PalantirSelectedTab");
                    if (!$(`nav.sidebar-navigation ul li[data-tab-index=${selectedTab}]`).is(":visible")) {
                        selectedTab = Tabs.APPLICATIONS.index;
                    }
                    $(`nav.sidebar-navigation ul li[data-tab-index=${selectedTab}]`).click();
                    activateDashboardTab(selectedTab);

                    Promise.all([
                        initializeAllCharts(),
                        initializeScheduledTasksOperations(),
                        initializeServicesOperations(),
                        initializeAccessStrategyOperations(),
                        initializeHeimdallOperations(),
                        initializeTicketsOperations(),
                        initializeSystemOperations(),
                        initializeLoggingOperations(),
                        initializeSsoSessionOperations(),
                        initializeConfigurationOperations(),
                        initializePersonDirectoryOperations(),
                        initializeAuthenticationOperations(),
                        initializeConsentOperations(),
                        initializeCasProtocolOperations(),
                        initializeSAML2ProtocolOperations(),
                        initializeSAML1ProtocolOperations(),
                        initializeOidcProtocolOperations(),
                        initializeThrottlesOperations(),
                        initializeMultifactorOperations(),
                        initializeMultitenancyOperations(),
                        initializeTrustedMultifactorOperations(),
                        initializeAuditEventsOperations(),
                        initializeCasEventsOperations(),
                        initializeCasSpringWebflowOperations(),
                        initializeHotKeyOperations(),
                        restoreActiveTabs()
                    ]);

                    window.addEventListener("resize", updateNavigationSidebar, {passive: true});
                }
            });
        }, 2);
        $("#dashboard").removeClass("d-none");
    } catch (error) {
        console.error("An error occurred:", error);
    }
}

document.addEventListener("DOMContentLoaded", () => {
    initializeTabs();
    initializeMenus();
    initializeDropDowns();
    initializeDatePickers();
    initializeTooltips();

    $("nav.sidebar-navigation ul li").off().on("click", function () {
        hideBanner();
        const index = selectSidebarMenuButton(this);
        activateDashboardTab(index);
    });
    Swal.fire({
        icon: "info",
        title: "Initializing Palantir",
        text: "Please wait while Palantir is initializing...",
        allowOutsideClick: false,
        showConfirmButton: false
    });

    notyf = new Notyf({
        duration: 3000,
        ripple: true,
        dismissable: true,
        position: {
            x: "center",
            y: "bottom"
        }
    });

    initializePalantir().then(r =>
        Swal.fire({
            title: "Palantir is ready!",
            text: "Palantir is successfully initialized and is ready for use.",
            showConfirmButton: false,
            icon: "success",
            timer: 800
        }));
});
                                                                         
