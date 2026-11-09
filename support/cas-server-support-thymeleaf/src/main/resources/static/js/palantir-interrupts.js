let interruptInquiryDataTable = null;

let interruptInquirersTable = null;

/**
 * Escape a table cell for display while keeping its raw value for sorting and searching.
 *
 * @param data the cell value
 * @param type the DataTables rendering type
 * @returns {string} the cell, escaped for display
 */
function renderInterruptInquiryCell(data, type) {
    return type === "display" ? $("<div>").text(data ?? "").html() : data;
}

/**
 * Show a message under the inquiry form.
 *
 * @param type info, warning or danger
 * @param message the message
 */
function showInterruptInquiryMessage(type, message) {
    const icons = {
        info: "mdi-information-outline",
        warning: "mdi-account-question-outline",
        danger: "mdi-alert-circle-outline"
    };
    const $message = $("#interruptInquiryMessage");
    $message
        .removeClass("banner-info banner-warning banner-danger")
        .addClass(`banner-${type}`);
    $message.find(".mdi").attr("class", `mdi ${icons[type]}`);
    $message.find(".passwordless-account-message-text").text(message);
    showElements($message);
}

/**
 * Describe what one interrupt source answered.
 *
 * @param inquirer the outcome of the source
 * @returns {{outcome: string, details: string}} the outcome and its details
 */
function describeInterruptInquirer(inquirer) {
    if (inquirer.error) {
        return {outcome: "Failed", details: inquirer.error};
    }
    const response = inquirer.response;
    if (!response) {
        return {outcome: "No answer", details: ""};
    }
    return {
        outcome: response.interrupt ? (response.block ? "Blocks the login" : "Interrupts the login") : "No interrupt",
        details: response.message ? palantirHtmlText(response.message) : passwordlessAccountDisplayValue(response.messageCode, "")
    };
}

/**
 * Draw an interrupt inquiry: the resolved person and application, the interrupt the login flow would show,
 * if any, and the answer of every interrupt source.
 *
 * @param report the inquiry report
 */
function renderInterruptInquiry(report) {
    const interrupt = report.interrupt;
    $("#interruptInquiryPrincipal").text(passwordlessAccountDisplayValue(report.principal, "Unknown user"));
    let application = "No application";
    if (report.service) {
        application = report.registeredService
            ? `${report.registeredService} · ${report.service}`
            : `${report.service} (no registered application matches)`;
    }
    $("#interruptInquiryService").text(application);
    $("#interruptInquiryIcon").attr("class", `mdi ${interrupt ? "mdi-alert-octagon-outline" : "mdi-check-circle-outline"}`);
    let status = "No interrupt";
    if (interrupt) {
        status = interrupt.block ? "Login is blocked" : "Login is interrupted";
    }
    $("#interruptInquiryStatus")
        .toggleClass("passwordless-account-contact-missing", Boolean(interrupt))
        .text(status);

    if (interrupt) {
        const $message = $("#interruptInquiryMessageText")
            .toggleClass("passwordless-account-empty-value", !interrupt.message && !interrupt.messageCode);
        if (interrupt.message) {
            $message.html(sanitizePalantirHtml(interrupt.message));
        } else {
            $message.text(passwordlessAccountDisplayValue(interrupt.messageCode, "No message"));
        }
        updatePasswordlessAccountState("#interruptInquiryBlock", interrupt.block, {enabled: "Yes", disabled: "No", unknown: "Unspecified"});
        updatePasswordlessAccountState("#interruptInquirySso", interrupt.ssoEnabled, {enabled: "Allowed", disabled: "Not allowed", unknown: "Unspecified"});
        const after = Number(interrupt.autoRedirectAfterSeconds);
        updatePasswordlessAccountState("#interruptInquiryRedirect", interrupt.autoRedirect, {
            enabled: after > 0 ? `After ${after} seconds` : "Immediately",
            disabled: "No",
            unknown: "Unspecified"
        });

        const $links = $("#interruptInquiryLinks").empty();
        const links = Object.entries(interrupt.links ?? {});
        if (links.length > 0) {
            for (const [name, url] of links) {
                const chip = $("<a>", {class: "passwordless-account-chip", href: url, target: "_blank", rel: "noopener noreferrer"});
                chip.append($("<i>", {class: "mdi mdi-open-in-new", "aria-hidden": "true"}));
                chip.append($("<span>").text(name));
                $links.append(chip);
            }
        } else {
            $links.append($("<span>", {class: "passwordless-account-empty-value"}).text("The interrupt offers no links."));
        }

        interruptInquiryDataTable.clear();
        for (const [name, values] of Object.entries(interrupt.data ?? {})) {
            interruptInquiryDataTable.row.add([name, passwordlessAccountDisplayValue(values, "")]);
        }
        interruptInquiryDataTable.draw();
        showElements($("#interruptInquiryInterrupt"));
    } else {
        hideElements($("#interruptInquiryInterrupt"));
    }

    interruptInquirersTable.clear();
    for (const inquirer of report.inquirers ?? []) {
        const {outcome, details} = describeInterruptInquirer(inquirer);
        interruptInquirersTable.row.add([inquirer.name, outcome, details]);
    }
    interruptInquirersTable.draw();

    showElements($("#interruptInquiryResult"));
    interruptInquiryDataTable.columns.adjust();
    interruptInquirersTable.columns.adjust();
}

/**
 * Ask the interrupts endpoint what a user would see at login.
 *
 * @param username the username
 * @param service the application URL, if any
 */
function inquireInterrupt(username, service) {
    const $button = $("#inquireInterruptButton");
    const $buttonLabel = $button.find(".mdc-button__label");
    $button.prop("disabled", true);
    $buttonLabel.html("<i class=\"mdc-tab__icon mdi mdi-loading mdi-spin\" aria-hidden=\"true\"></i>Inquiring...");
    hideElements($("#interruptInquiryResult"));
    hideElements($("#interruptInquiryMessage"));

    $.ajax({
        url: `${CasActuatorEndpoints.interrupts()}/${encodeURIComponent(username)}`,
        method: "GET",
        data: service ? {service: service} : {},
        dataType: "json",
        headers: {"Accept": "application/json"},
        success: report => renderInterruptInquiry(report),
        error: xhr => {
            if (xhr.status === 404) {
                showInterruptInquiryMessage("warning", `CAS could not resolve the user ${username}.`);
            } else if (xhr.status === 400) {
                showInterruptInquiryMessage("warning", `CAS does not accept ${service} as an application URL.`);
            } else {
                console.error("Error inquiring for interrupts:", xhr.responseText);
                showInterruptInquiryMessage("danger", `Unable to find out which interrupt ${username} would see.`);
            }
        },
        complete: () => {
            $button.prop("disabled", false);
            $buttonLabel.html("<i class=\"mdc-tab__icon mdi mdi-magnify\" aria-hidden=\"true\"></i>Inquire");
        }
    });
}

/**
 * Show the Interrupts section of the Authentication page when the interrupts endpoint is available,
 * and wire its form.
 */
function initializeInterruptInquiry() {
    if (!CasActuatorEndpoints.interrupts()) {
        hideElements($("#interrupts").parent());
        hideElements($("#interrupts-tab"));
        return;
    }
    showElements($("#interrupts").parent());
    const tableOptions = (id, emptyTable, columns, widths = []) => ({
        pageLength: 5,
        lengthMenu: [5, 10, 25, 50],
        autoWidth: false,
        order: [],
        language: {emptyTable: emptyTable},
        columnDefs: [
            {targets: columns, render: renderInterruptInquiryCell},
            ...widths.map((width, target) => ({targets: target, width: width}))
        ],
        drawCallback: () => {
            $(`#${id} tr`).addClass("mdc-data-table__row");
            $(`#${id} td`).addClass("mdc-data-table__cell");
        }
    });
    interruptInquiryDataTable = $("#interruptInquiryDataTable").DataTable(
        tableOptions("interruptInquiryDataTable", "The interrupt carries no data.", [0, 1]));
    interruptInquirersTable = $("#interruptInquirersTable").DataTable(
        tableOptions("interruptInquirersTable", "No interrupt sources are configured.", [0, 1, 2], ["25%", "18%"]));

    $("#inquireInterruptButton").off("click.interrupts").on("click.interrupts", () => {
        const form = document.getElementById("fmInterruptInquiry");
        if (form.reportValidity()) {
            inquireInterrupt($("#interruptUsername").val().trim(), $("#interruptService").val().trim());
        }
    });
}

/**
 * Open the Interrupts section of the Authentication page and inquire for a user, as the Users page does.
 *
 * @param username the user
 * @param service the application URL, if any
 * @returns {Promise<void>} resolved once the inquiry has been sent
 */
async function openInterruptInquiry(username, service = "") {
    $(`nav.sidebar-navigation ul li[data-tab-index=${Tabs.AUTHENTICATION.index}]`).trigger("click");
    await initializeDashboardTab(Tabs.AUTHENTICATION);
    const tabs = $("#authentication-tabs");
    if (tabs.data("ui-tabs")) {
        tabs.tabs("option", "active", tabs.find("> ul > li").index($("#interrupts").parent()));
    }
    $("#interruptUsername").val(username).trigger("input");
    $("#interruptService").val(service).trigger("input");
    inquireInterrupt(username, service);
}
