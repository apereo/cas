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

    function ticketCardText(value, fallback = "Not available") {
        const text = value === null || value === undefined ? "" : String(value).trim();
        return text || fallback;
    }

    function ticketCardFieldValue(value) {
        if (value === null) {
            return "null";
        }
        if (value === undefined) {
            return "Not available";
        }
        if (typeof value === "object") {
            try {
                return JSON.stringify(value);
            } catch (e) {
                return String(value);
            }
        }
        return String(value);
    }

    function setTicketCardsStatus(selector, icon, message) {
        const status = $(selector).empty();
        if (!message) {
            status.addClass("d-none");
            return;
        }
        status.append($("<i>", {class: `mdi ${icon}`, "aria-hidden": "true"}));
        status.append(document.createTextNode(message));
        status.removeClass("d-none");
    }

    function createTicketFields(data) {
        const flattened = flattenJSON(data ?? {});
        const entries = Object.entries(flattened).filter(([, value]) => value !== null);
        const fields = $("<section>", {class: "ticket-card-fields"});
        fields.append($("<div>", {class: "ticket-card-fields-title"})
            .append($("<i>", {class: "mdi mdi-tag-multiple", "aria-hidden": "true"}))
            .append(document.createTextNode(`${entries.length} ${entries.length === 1 ? "field" : "fields"}`)));

        if (entries.length === 0) {
            fields.append($("<p>", {class: "ticket-card-no-fields"}).text("No additional fields"));
            return fields;
        }

        const fieldList = $("<dl>", {class: "ticket-card-field-list"});
        for (const [name, value] of entries) {
            const field = $("<div>", {class: "ticket-card-field"});
            field.append($("<dt>").text(name));
            field.append($("<dd>").append($("<code>").text(ticketCardFieldValue(value))));
            fieldList.append(field);
        }
        fields.append(fieldList);
        return fields;
    }

    function createTicketCard({title, subtitle, badge, icon, variant, data}) {
        const card = $("<article>", {
            class: `mdc-card ticket-item-card ticket-item-card-${variant}`,
            "aria-label": `${badge}: ${ticketCardText(title)}`
        });
        const header = $("<div>", {class: "ticket-item-card-header"});
        const identity = $("<div>", {class: "ticket-item-identity"});
        identity.append($("<i>", {class: `mdi ${icon}`, "aria-hidden": "true"}));
        const titleGroup = $("<div>", {class: "ticket-item-title-group"});
        titleGroup.append($("<code>", {class: "ticket-item-title"}).text(ticketCardText(title)));
        if (subtitle) {
            titleGroup.append($("<p>", {class: "ticket-item-subtitle"}).text(subtitle));
        }
        identity.append(titleGroup);
        header.append(identity);
        header.append($("<span>", {class: `ticket-item-badge ticket-item-badge-${variant}`})
            .append($("<i>", {
                class: `mdi ${variant === "catalog" ? "mdi-book-open-page-variant" : "mdi-clock-outline"}`,
                "aria-hidden": "true"
            }))
            .append(document.createTextNode(badge)));
        card.append(header);
        card.append(createTicketFields(data));
        return card;
    }

    function renderTicketCatalog(response) {
        const entries = Array.isArray(response)
            ? [...response].sort((first, second) => ticketCardText(first?.prefix, "")
                .localeCompare(ticketCardText(second?.prefix, "")))
            : [];
        const cards = $("#ticketCatalogCards").empty();
        $("#ticketCatalogCount").text(entries.length);
        $("#ticketCatalogSummary").removeClass("d-none");
        $("#ticketDefinitions").empty();

        if (entries.length === 0) {
            setTicketCardsStatus("#ticketCatalogStatus", "mdi-ticket-outline", "No ticket definitions are available.");
            return;
        }

        setTicketCardsStatus("#ticketCatalogStatus", "", "");
        for (const entry of entries) {
            const item = $("<li>", {
                class: "mdc-list-item",
                "data-value": ticketCardText(entry?.prefix, ""),
                role: "option"
            });
            item.append($("<span>", {class: "mdc-list-item__ripple"}));
            item.append($("<span>", {class: "mdc-list-item__text"}).text(ticketCardText(entry?.apiClass)));
            $("#ticketDefinitions").append(item);

            cards.append(createTicketCard({
                title: entry?.prefix,
                subtitle: ticketCardText(entry?.apiClass),
                badge: "Definition",
                icon: "mdi-ticket-confirmation",
                variant: "catalog",
                data: entry
            }));
        }

        const ticketDefinitions = new mdc.select.MDCSelect(document.getElementById("ticketDefinitionsSelect"));
        ticketDefinitions.selectedIndex = 0;
    }

    const ticketRegistryEndpoint = CasActuatorEndpoints.ticketRegistry();
    if (ticketRegistryEndpoint) {
        $.get(`${ticketRegistryEndpoint}/ticketCatalog`, renderTicketCatalog)
            .fail((xhr, status, error) => {
                console.error("Error fetching data:", error);
                $("#ticketCatalogCards").empty();
                $("#ticketCatalogSummary").addClass("d-none");
                setTicketCardsStatus("#ticketCatalogStatus", "mdi-alert-circle", "Unable to load the ticket catalog.");
                displayBanner(xhr);
            });
    } else {
        setTicketCardsStatus("#ticketCatalogStatus", "mdi-ticket-off-outline", "The ticket catalog endpoint is unavailable.");
    }

    function renderTicketExpirationPolicies(response) {
        const policies = response && typeof response === "object" && !Array.isArray(response)
            ? Object.entries(response).sort(([first], [second]) => first.localeCompare(second))
            : [];
        const cards = $("#ticketExpirationPoliciesCards").empty();
        $("#ticketExpirationPoliciesCount").text(policies.length);
        $("#ticketExpirationPoliciesSummary").removeClass("d-none");

        if (policies.length === 0) {
            setTicketCardsStatus("#ticketExpirationPoliciesStatus", "mdi-timer-off-outline",
                "No ticket expiration policies are available.");
            return;
        }

        setTicketCardsStatus("#ticketExpirationPoliciesStatus", "", "");
        for (const [name, policy] of policies) {
            const policyData = policy && typeof policy === "object" ? policy : {value: policy};
            cards.append(createTicketCard({
                title: name,
                subtitle: ticketCardText(policyData.name, "Ticket expiration policy"),
                badge: "Expiration",
                icon: "mdi-timer-sand",
                variant: "expiration",
                data: policyData
            }));
        }
    }

    const ticketExpirationPoliciesEndpoint = CasActuatorEndpoints.ticketExpirationPolicies();
    if (ticketExpirationPoliciesEndpoint) {
        $.get(ticketExpirationPoliciesEndpoint, renderTicketExpirationPolicies)
            .fail((xhr, status, error) => {
                console.error("Error fetching data:", error);
                $("#ticketExpirationPoliciesCards").empty();
                $("#ticketExpirationPoliciesSummary").addClass("d-none");
                setTicketCardsStatus("#ticketExpirationPoliciesStatus", "mdi-alert-circle",
                    "Unable to load ticket expiration policies.");
                displayBanner(xhr);
            });
    } else {
        setTicketCardsStatus("#ticketExpirationPoliciesStatus", "mdi-timer-off-outline",
            "The ticket expiration policies endpoint is unavailable.");
    }
}
