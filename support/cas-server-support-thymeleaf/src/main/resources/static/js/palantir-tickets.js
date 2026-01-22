async function initializeTicketsOperations() {
    const ticketEditor = initializeAceEditor("ticketEditor");
    ticketEditor.setReadOnly(true);

    $("button#searchTicketButton").off().on("click", () => {
        const ticket = document.getElementById("ticket");
        if (!ticket.checkValidity()) {
            ticket.reportValidity();
            return false;
        }
        const ticketId = $(ticket).val();
        const type = $("#ticketDefinitions .mdc-list-item--selected").attr("data-value").trim();
        if (ticket && type) {
            const decode = new mdc.switchControl.MDCSwitch(document.getElementById("decodeTicketButton")).selected;
            ticketEditor.setValue("");
            if (CasActuatorEndpoints.ticketRegistry()) {
                $.get(`${CasActuatorEndpoints.ticketRegistry()}/query?type=${type}&id=${ticketId}&decode=${decode}`,
                    response => {
                        ticketEditor.setValue(JSON.stringify(response, null, 2));
                        ticketEditor.gotoLine(1);
                    })
                    .fail((xhr, status, error) => {
                        console.error("Error fetching data:", error);
                        displayBanner(xhr);
                    });
            }
        }
    });

    $("button#cleanTicketsButton").off().on("click", () => {
        hideBanner();
        if (CasActuatorEndpoints.ticketRegistry()) {
            $.ajax({
                url: `${CasActuatorEndpoints.ticketRegistry()}/clean`,
                type: "DELETE",
                success: response => {
                    ticketEditor.setValue(JSON.stringify(response, null, 2));
                    ticketEditor.gotoLine(1);
                },
                error: (xhr, status, error) => {
                    console.error(`Error: ${status} / ${error} / ${xhr.responseText}`);
                    displayBanner(xhr);
                }
            });
        }

    });

    const ticketCatalogTable = $("#ticketCatalogTable").DataTable({
        pageLength: 10,
        autoWidth: false,
        columnDefs: [
            {visible: false, targets: 0}
        ],
        order: [0, "desc"],
        drawCallback: settings => {
            $("#ticketCatalogTable tr").addClass("mdc-data-table__row");
            $("#ticketCatalogTable td").addClass("mdc-data-table__cell");

            const api = settings.api;
            const rows = api.rows({page: "current"}).nodes();
            let last = null;
            api.column(0, {page: "current"})
                .data()
                .each((group, i) => {
                    if (last !== group) {
                        $(rows).eq(i).before(
                            `<tr style='font-weight: bold; background-color:var(--cas-theme-primary); color:var(--mdc-text-button-label-text-color);'>
                                            <td colspan="2">${group}</td></tr>`.trim());
                        last = group;
                    }
                });
        }
    });

    if (CasActuatorEndpoints.ticketRegistry()) {
        $.get(`${CasActuatorEndpoints.ticketRegistry()}/ticketCatalog`, response => {
            response.forEach(entry => {
                let item = `
                            <li class="mdc-list-item" data-value='${entry.prefix}' role="option">
                                <span class="mdc-list-item__ripple"></span>
                                <span class="mdc-list-item__text">${entry.apiClass}</span>
                            </li>
                        `;
                $("#ticketDefinitions").append($(item.trim()));

                const flattened = flattenJSON(entry);
                for (const [key, value] of Object.entries(flattened)) {
                    ticketCatalogTable.row.add({
                        0: `<code>${entry.prefix}</code>`,
                        1: `<code>${key}</code>`,
                        2: `<code>${value}</code>`
                    });
                }
                ticketCatalogTable.draw();
            });
            const ticketDefinitions = new mdc.select.MDCSelect(document.getElementById("ticketDefinitionsSelect"));
            ticketDefinitions.selectedIndex = 0;
        }).fail((xhr, status, error) => {
            console.error("Error fetching data:", error);
            displayBanner(xhr);
        });
    }

    const ticketExpirationPoliciesTable = $("#ticketExpirationPoliciesTable").DataTable({
        pageLength: 10,
        autoWidth: false,
        columnDefs: [
            {visible: false, targets: 0}
        ],

        order: [0, "desc"],
        drawCallback: settings => {
            $("#ticketExpirationPoliciesTable tr").addClass("mdc-data-table__row");
            $("#ticketExpirationPoliciesTable td").addClass("mdc-data-table__cell");

            const api = settings.api;
            const rows = api.rows({page: "current"}).nodes();
            let last = null;
            api.column(0, {page: "current"})
                .data()
                .each((group, i) => {
                    if (last !== group) {
                        $(rows).eq(i).before(
                            `<tr style='font-weight: bold; background-color:var(--cas-theme-primary); color:var(--mdc-text-button-label-text-color);'>
                                            <td colspan="2">${group}</td></tr>`.trim());
                        last = group;
                    }
                });
        }
    });

    if (CasActuatorEndpoints.ticketExpirationPolicies()) {
        $.get(CasActuatorEndpoints.ticketExpirationPolicies(), response => {
            ticketExpirationPoliciesTable.clear();
            for (const key of Object.keys(response)) {
                const policy = response[key];
                delete policy.name;

                const flattened = flattenJSON(policy);
                for (const [k, v] of Object.entries(flattened)) {
                    ticketExpirationPoliciesTable.row.add({
                        0: `<code>${key}</code>`,
                        1: `<code>${k}</code>`,
                        2: `<code>${v}</code>`
                    });
                }
            }
            ticketExpirationPoliciesTable.draw();
        }).fail((xhr, status, error) => {
            console.error("Error fetching data:", error);
            displayBanner(xhr);
        });
    }
}
