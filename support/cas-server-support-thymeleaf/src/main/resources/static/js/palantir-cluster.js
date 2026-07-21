const clusterTopologyState = {
    endpointPath: "topology",
    tabIndex: 0,
    prefix: "clusterTopology",
    loadingMessage: "Loading cluster topology...",
    emptyMessage: "No cluster members were discovered.",
    members: [],
    refreshInProgress: false
};

const clusterDiscoveryState = {
    endpointPath: "discovery",
    tabIndex: 1,
    prefix: "clusterDiscovery",
    loadingMessage: "Loading cluster discovery...",
    emptyMessage: "No cluster members were found by the discovery client.",
    members: [],
    refreshInProgress: false
};

let clusterLoadingDialogSequence = 0;
let activeClusterLoadingDialog = null;

function clusterTopologyText(value, fallback = "Not available") {
    const text = value === null || value === undefined ? "" : String(value).trim();
    return text || fallback;
}

function clusterTopologyAttributeValue(value) {
    if (value === null || value === undefined) {
        return "";
    }
    if (typeof value === "object") {
        try {
            return JSON.stringify(value);
        } catch (error) {
            return String(value);
        }
    }
    return String(value);
}

function clusterTopologyResponseTime(value) {
    const responseTime = Number(value);
    return Number.isFinite(responseTime) ? `${(responseTime * 1_000_000).toLocaleString()} ns` : "Not available";
}

function clusterTabIsActive(tabIndex) {
    if (currentActiveTab !== Tabs.CLUSTER.index) {
        return false;
    }
    const clusterTabs = $("#cluster-tabs");
    return clusterTabs.data("ui-tabs")
        ? clusterTabs.tabs("option", "active") === tabIndex
        : tabIndex === clusterTopologyState.tabIndex;
}

function clusterTopologyMetadata(icon, label, value) {
    const row = $("<div>", {class: "cluster-member-metadata-row"});
    row.append($("<i>", {class: `mdi ${icon}`, "aria-hidden": "true"}));
    row.append($("<span>", {class: "cluster-member-metadata-label"}).text(label));
    row.append($("<span>", {class: "cluster-member-metadata-value"}).text(value));
    return row;
}

function showClusterMemberDetails(member) {
    const healthy = member.status === true;
    const dialog = $("<div>", {class: "cluster-member-details-dialog"});
    const details = $(
        `<table class="mdc-data-table__table table table-striped noborder cluster-member-details-table">
            <thead>
                <tr class="mdc-data-table__header-row">
                    <th class="mdc-data-table__header-cell" role="columnheader" scope="col">Field</th>
                    <th class="mdc-data-table__header-cell" role="columnheader" scope="col">Value</th>
                </tr>
            </thead>
            <tbody class="mdc-data-table__content"></tbody>
        </table>`
    );
    const detailsBody = details.find("tbody");
    const addDetail = (label, value) => {
        const row = $("<tr>", {class: "mdc-data-table__row"});
        row.append($("<td>", {class: "mdc-data-table__cell cluster-member-detail-label"}).text(label));
        row.append($("<td>", {class: "mdc-data-table__cell cluster-member-detail-value"}).text(value));
        detailsBody.append(row);
    };

    addDetail("Owner", clusterTopologyText(member.owner, "Unassigned"));
    addDetail("Member ID", clusterTopologyText(member.id));
    addDetail("Address", clusterTopologyText(member.address));
    addDetail("Health", healthy ? "Healthy" : "Unhealthy");
    addDetail("Response time", clusterTopologyResponseTime(member.responseTime));
    if (member.description) {
        addDetail("Description", member.description);
    }

    const attributes = member.attributes && typeof member.attributes === "object" ? member.attributes : {};
    for (const [name, value] of Object.entries(attributes)) {
        addDetail(`Attribute: ${name}`, clusterTopologyAttributeValue(value));
    }
    dialog.append(details);

    $("body").append(dialog);
    let detailsDataTable;
    dialog.dialog({
        title: `Cluster Member: ${clusterTopologyText(member.id, "Unknown")}`,
        modal: true,
        width: Math.min($(window).width() - 40, 760),
        maxHeight: Math.min($(window).height() - 80, 720),
        position: {my: "center top", at: "center top+60", of: window},
        buttons: {
            Close: function () {
                $(this).dialog("close");
            }
        },
        open: function () {
            const dialogShell = dialog.closest(".ui-dialog");
            const dialogClass = healthy ? "cluster-member-dialog-healthy" : "cluster-member-dialog-unhealthy";
            dialogShell.addClass(dialogClass);
            detailsDataTable = details.DataTable({
                paging: false,
                searching: false,
                ordering: false,
                info: false,
                autoWidth: false,
                columns: [
                    {width: "30%"},
                    {width: "70%"}
                ],
                drawCallback: () => {
                    details.find("tr").addClass("mdc-data-table__row");
                    details.find("td").addClass("mdc-data-table__cell");
                }
            });
            cas.init(`.${dialogClass} .ui-dialog-buttonpane`);
        },
        close: function () {
            detailsDataTable?.destroy();
            dialog.dialog("destroy").remove();
        }
    });
}

function createClusterMemberCard(member) {
    const healthy = member.status === true;
    const card = $("<article>", {
        class: `mdc-card cluster-member-card ${healthy ? "cluster-member-card-healthy" : "cluster-member-card-unhealthy"}`,
        "aria-label": `${clusterTopologyText(member.id, "Unknown member")}: ${healthy ? "healthy" : "unhealthy"}`
    });
    const header = $("<div>", {class: "cluster-member-card-header"});
    const identity = $("<div>", {class: "cluster-member-identity"});
    identity.append($("<i>", {
        class: `mdi ${healthy ? "mdi-server-network" : "mdi-server-network-off"}`,
        "aria-hidden": "true"
    }));
    identity.append($("<code>", {class: "cluster-member-id"}).text(clusterTopologyText(member.id, "Unknown member")));
    header.append(identity);
    header.append($("<span>", {
        class: `cluster-member-health cluster-member-health-${healthy ? "healthy" : "unhealthy"}`
    }).append($("<i>", {
        class: `mdi ${healthy ? "mdi-check-circle" : "mdi-alert-circle"}`,
        "aria-hidden": "true"
    })).append(document.createTextNode(healthy ? "Healthy" : "Unhealthy")));
    card.append(header);

    if (member.description) {
        card.append($("<p>", {class: "cluster-member-description"}).text(member.description));
    }

    const metadata = $("<div>", {class: "cluster-member-metadata"});
    metadata.append(clusterTopologyMetadata("mdi-map-marker", "Address", clusterTopologyText(member.address)));
    metadata.append(clusterTopologyMetadata("mdi-timer-outline", "Response", clusterTopologyResponseTime(member.responseTime)));
    card.append(metadata);

    const attributes = member.attributes && typeof member.attributes === "object" ? member.attributes : {};
    const attributeEntries = Object.entries(attributes);
    if (attributeEntries.length > 0) {
        const attributePreview = $("<div>", {class: "cluster-member-attributes"});
        attributePreview.append($("<div>", {class: "cluster-member-attributes-title"})
            .append($("<i>", {class: "mdi mdi-tag-multiple", "aria-hidden": "true"}))
            .append(document.createTextNode(`Attributes (${attributeEntries.length})`)));
        const previewList = $("<dl>");
        for (const [name, value] of attributeEntries.slice(0, 3)) {
            previewList.append($("<dt>").text(name));
            previewList.append($("<dd>").text(clusterTopologyAttributeValue(value)));
        }
        attributePreview.append(previewList);
        if (attributeEntries.length > 3) {
            attributePreview.append($("<span>", {class: "cluster-member-more-attributes"})
                .text(`+${attributeEntries.length - 3} more`));
        }
        card.append(attributePreview);
    }

    const actions = $("<div>", {class: "mdc-card__actions cluster-member-card-actions"});
    const detailsButton = $(
        `<button type="button" class="mdc-button mdc-button--raised" title="View all member details">
            <span class="mdc-button__label"><i class="mdi mdi-information-outline" aria-hidden="true"></i>Details</span>
        </button>`
    );
    detailsButton.on("click", () => showClusterMemberDetails(member));
    actions.append(detailsButton);
    card.append(actions);
    return card;
}

function renderClusterMembers(state) {
    const ownersContainer = $(`#${state.prefix}Owners`).empty();
    const status = $(`#${state.prefix}Status`);
    const allMembers = Array.isArray(state.members) ? state.members : [];
    const healthyCount = allMembers.filter(member => member.status === true).length;
    const ownerNames = new Set(allMembers.map(member => clusterTopologyText(member.owner, "Unassigned")));

    $(`#${state.prefix}MemberCount`).text(allMembers.length);
    $(`#${state.prefix}HealthyCount`).text(healthyCount);
    $(`#${state.prefix}UnhealthyCount`).text(allMembers.length - healthyCount);
    $(`#${state.prefix}OwnerCount`).text(ownerNames.size);
    showElements($(`#${state.prefix}Summary`));

    if (allMembers.length === 0) {
        status.html("");
        status.append($("<i>", {class: "mdi mdi-server-off", "aria-hidden": "true"}));
        status.append(document.createTextNode(state.emptyMessage));
        showElements(status);
        return;
    }

    hideElements(status);

    const groups = new Map();
    for (const member of allMembers) {
        const owner = clusterTopologyText(member.owner, "Unassigned");
        if (!groups.has(owner)) {
            groups.set(owner, []);
        }
        groups.get(owner).push(member);
    }

    const sortedGroups = [...groups.entries()].sort(([first], [second]) => first.localeCompare(second));
    for (const [owner, ownerMembers] of sortedGroups) {
        ownerMembers.sort((first, second) => {
            if (first.status !== second.status) {
                return first.status === true ? 1 : -1;
            }
            return clusterTopologyText(first.id, "").localeCompare(clusterTopologyText(second.id, ""));
        });
        const healthyOwnerMembers = ownerMembers.filter(member => member.status === true).length;
        const ownerGroup = $("<section>", {class: "cluster-owner-group"});
        const ownerHeader = $("<div>", {class: "cluster-owner-header"});
        const ownerTitle = $("<h4>", {class: "cluster-owner-title"});
        ownerTitle.append($("<i>", {class: "mdi mdi-domain", "aria-hidden": "true"}));
        ownerTitle.append($("<span>").text(owner));
        ownerHeader.append(ownerTitle);
        ownerHeader.append($("<span>", {class: "cluster-owner-count"})
            .text(`${healthyOwnerMembers}/${ownerMembers.length} healthy`));
        ownerGroup.append(ownerHeader);

        const memberGrid = $("<div>", {class: "cluster-member-grid"});
        for (const member of ownerMembers) {
            memberGrid.append(createClusterMemberCard(member));
        }
        ownerGroup.append(memberGrid);
        ownersContainer.append(ownerGroup);
    }
}

function clearClusterMembers(state) {
    state.members = [];
    $(`#${state.prefix}Owners`).empty();
    $(`#${state.prefix}MemberCount`).text("0");
    $(`#${state.prefix}HealthyCount`).text("0");
    $(`#${state.prefix}UnhealthyCount`).text("0");
    $(`#${state.prefix}OwnerCount`).text("0");
    hideElements($(`#${state.prefix}Summary`));
    const status = $(`#${state.prefix}Status`).empty();
    hideElements(status);
}

function showClusterLoadingDialog(state) {
    const dialogId = String(++clusterLoadingDialogSequence);
    activeClusterLoadingDialog = dialogId;
    Swal.fire({
        title: state.loadingMessage,
        html: `
            <div role="progressbar" class="mdc-linear-progress mdc-linear-progress--indeterminate"
                 aria-label="${state.loadingMessage}">
                <div class="mdc-linear-progress__buffer">
                    <div class="mdc-linear-progress__buffer-bar"></div>
                    <div class="mdc-linear-progress__buffer-dots"></div>
                </div>
                <div class="mdc-linear-progress__bar mdc-linear-progress__primary-bar">
                    <span class="mdc-linear-progress__bar-inner"></span>
                </div>
                <div class="mdc-linear-progress__bar mdc-linear-progress__secondary-bar">
                    <span class="mdc-linear-progress__bar-inner"></span>
                </div>
            </div>`,
        allowOutsideClick: false,
        allowEscapeKey: false,
        showConfirmButton: false,
        customClass: {popup: "cluster-members-loading-dialog"},
        didOpen: popup => {
            popup.dataset.clusterLoadingDialog = dialogId;
            cas.init(".cluster-members-loading-dialog");
        }
    });
    return dialogId;
}

function closeClusterLoadingDialog(dialogId) {
    if (activeClusterLoadingDialog !== dialogId) {
        return;
    }
    const popup = Swal.getPopup();
    if (popup?.dataset.clusterLoadingDialog === dialogId) {
        Swal.close();
    }
    activeClusterLoadingDialog = null;
}

function refreshClusterMembers(state) {
    const endpointBase = CasActuatorEndpoints.clusterTopology();
    if (!endpointBase || !clusterTabIsActive(state.tabIndex) || state.refreshInProgress) {
        return Promise.resolve();
    }
    state.refreshInProgress = true;
    const endpoint = `${String(endpointBase).replace(/\/$/, "")}/${state.endpointPath}`;
    const loadingDialogId = showClusterLoadingDialog(state);

    return $.get(endpoint)
        .done(response => {
            state.members = Array.isArray(response) ? response : [];
            renderClusterMembers(state);
        })
        .fail((xhr, requestStatus, error) => {
            console.error(`Error fetching cluster ${state.endpointPath}:`, error);
            clearClusterMembers(state);
            displayBanner(xhr);
        })
        .always(() => {
            state.refreshInProgress = false;
            closeClusterLoadingDialog(loadingDialogId);
        });
}

function refreshClusterTopology() {
    return refreshClusterMembers(clusterTopologyState);
}

function refreshClusterDiscovery() {
    return refreshClusterMembers(clusterDiscoveryState);
}

function refreshActiveClusterTab() {
    if (clusterTabIsActive(clusterTopologyState.tabIndex)) {
        return refreshClusterTopology();
    }
    if (clusterTabIsActive(clusterDiscoveryState.tabIndex)) {
        return refreshClusterDiscovery();
    }
    return Promise.resolve();
}

async function initializeClusterTopologyOperations() {
    if (!CasActuatorEndpoints.clusterTopology() || $("#cluster-tabs").length === 0) {
        return;
    }
    $("#cluster-tabs").off("tabsactivate.clusterTopology").on("tabsactivate.clusterTopology", (event, ui) => {
        if (ui.newPanel.is("#cluster-topology-tab")) {
            refreshClusterTopology();
        } else if (ui.newPanel.is("#cluster-discovery-tab")) {
            refreshClusterDiscovery();
        }
    });
    refreshActiveClusterTab();
    setInterval(() => {
        if (currentActiveTab === Tabs.CLUSTER.index) {
            refreshActiveClusterTab();
        }
    }, palantirSettings().refreshInterval);
}
