function hideElements(elements) {
    $(elements)
        .hide()
        .addClass("hide")
        .addClass("d-none");
}

function showElements(elements) {
    $(elements)
        .show()
        .removeClass("hide")
        .removeClass("d-none");
}

function hideBanner() {
    notyf.dismissAll();
}

function closeAllDialogs() {
    $(".ui-dialog-content:visible").dialog("close");
}

function displayBanner(error) {
    let message = "";
    if (error.hasOwnProperty("status")) {
        switch (error.status) {
        case 401:
            message = "You are not authorized to access this resource. Are you sure you are authenticated?";
            break;
        case 403:
            message = "You are forbidden from accessing this resource. Are you sure you have the necessary permissions and the entry is correctly registered with CAS?";
            break;
        case 400:
        case 500:
        case 503:
            message = "Unable to process or accept the request. Check CAS server logs for details.";
            break;
        case 0:
            message = "Unable to contact the CAS server. Are you sure the server is reachable?";
            break;
        default:
            message = `HTTP error: ${error.status}. `;
            break;
        }
    }
    if (error.hasOwnProperty("path")) {
        message += `Unable to make an API call to ${error.path}. Is the endpoint enabled and available?`;
    }
    if (message.length === 0) {
        if (typeof error === "string") {
            message = error;
        } else {
            message = error.message;
        }
    }
    notyf.dismissAll();
    notyf.error(message);
}

function waitForActuator(endpoint, intervalMs = 2000) {
    return new Promise((resolve) => {
        function poll() {
            $.ajax({
                url: endpoint,
                method: "GET",
                dataType: "json",
                timeout: 3000
            })
                .done(function (data) {
                    resolve(data);
                })
                .fail(function () {
                    setTimeout(poll, intervalMs);
                });
        }

        poll();
    });
}

function highlightElements() {
    document
        .querySelectorAll("pre code[data-highlighted]")
        .forEach(el => delete el.dataset.highlighted);
    hljs.highlightAll();
}


function initializeTabs() {
    $(".jqueryui-tabs").tabs({
        activate: function () {
            const tabId = $(this).attr("id");
            if (tabId) {
                const active = $(this).tabs("option", "active");
                const storedTabs = localStorage.getItem("ActiveTabs");
                const activeTabs = storedTabs ? JSON.parse(storedTabs) : {};
                activeTabs[tabId] = active;
                localStorage.setItem("ActiveTabs", JSON.stringify(activeTabs));
            }
        }
    }).off().on("click", () => updateNavigationSidebar());
}

function initializeMenus() {
    $(".jqueryui-menu").menu();
}

function initializeDropDowns() {
    $(".jqueryui-selectmenu").selectmenu({
        width: "360px",
        change: function (event, ui) {
            const $select = $(this);
            const handlerNames = $select.data("change-handler").split(",");
            for (const handlerName of handlerNames) {
                if (handlerName && handlerName.length > 0 && typeof window[handlerName] === "function") {
                    const result = window[handlerName]($select, ui);
                    if (result !== undefined && result === false) {
                        break;
                    }
                }
            }
        }
    });
}

function initializeDatePickers() {
    $("input.jquery-datepicker").datepicker({
        showAnim: "slideDown",
        onSelect: function (date, ins) {
            $(ins).val(date);
            generateServiceDefinition();
            $(`#${$(ins).prop("id")}`).prev().find(".mdc-notched-outline__notch").hide();
        }
    });
}

function initializeTooltips() {
    $( function() {
        $( document ).tooltip({
            show: {
                effect: "fade",
                delay: 800,
                duration: 500
            },
            hide: {
                effect: "fade",
                delay: 100
            },
            position: {
                my: "center bottom-20",
                at: "center top",
                using: function( position, feedback ) {
                    $( this ).css( position );
                    $( "<div>" )
                        .addClass( "arrow" )
                        .addClass( feedback.vertical )
                        .addClass( feedback.horizontal )
                        .appendTo( this );
                }
            }
        });
    });
}
