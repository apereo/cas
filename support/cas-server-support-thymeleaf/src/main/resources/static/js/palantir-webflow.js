async function initializeCasSpringWebflowOperations() {
    function drawFlowStateDiagram() {
        $("#webflowMarkdownContainer").addClass("hide");
        $("#webflowDiagram").addClass("hide");
        hideBanner();
        const flowId = $("#webflowFilter").val();

        Swal.fire({
            icon: "info",
            title: `Fetching Webflow Definition`,
            text: `Please wait while webflow definition for ${flowId} is processed...`,
            allowOutsideClick: false,
            showConfirmButton: false,
            didOpen: () => Swal.showLoading()
        });

        const selectedState = $("#webflowStateFilter").val();
        let url = `${actuatorEndpoints.springWebflow}?flowId=${flowId}`;
        if (selectedState && selectedState !== "all") {
            url += `&stateId=${selectedState}`;
        }
        $.ajax({
            url: url,
            type: "GET",
            headers: {
                "Content-Type": "application/json"
            },
            success: async (response, textStatus, xhr) => {
                const flow = response[flowId];

                if (!selectedState) {
                    const allStates = Object.keys(flow.states).sort();
                    $("#webflowStateFilter").empty().append(
                        $("<option>", {
                            value: "all",
                            text: "All",
                            selected: true
                        })
                    );

                    $.each(allStates, (idx, item) => {
                        $("#webflowStateFilter").append(
                            $("<option>", {
                                value: item,
                                text: item
                            })
                        );
                    });
                    $("#webflowStateFilter").selectmenu("refresh");
                }

                let diagramDefinition = `stateDiagram-v2\ndirection LR\n`;
                if (flow.startActions) {
                    diagramDefinition += `\t[*] --> Initialization: Start\n`;
                    diagramDefinition += `\tstate Initialization {\n`;
                    for (let i = 0; i < flow.startActions.length; i++) {
                        let action = flow.startActions[i];
                        if (action.startsWith("set ")) {
                            action = "Execute";
                        }

                        if (i === 0) {
                            diagramDefinition += `\t\t[*] --> ${action}: Then\n`;
                        } else {
                            let previousAction = flow.startActions[i - 1];
                            if (previousAction.startsWith("set ")) {
                                previousAction = "Execute";
                            }
                            diagramDefinition += `\t\t${previousAction} --> ${action}: Then\n`;
                        }
                        if (i === flow.startActions.length - 1) {
                            diagramDefinition += `\t\t${action} --> [*]: End\n`;
                        }
                    }
                    diagramDefinition += `\t}\n`;
                    diagramDefinition += `\tInitialization --> ${flow.startState}: Then\n`;
                } else {
                    diagramDefinition += `\t[*] --> ${flow.startState}: Start\n`;
                }

                for (let entry of Object.keys(flow.states)) {
                    const state = flow.states[entry];
                    entry = entry.trim().replace(/-/g, "_");
                    if (state.isEndState === true) {
                        diagramDefinition += `\t${entry} --> [*]: End\n`;
                    } else {

                        if (state.isViewState) {
                            if (!state.transitions) {
                                diagramDefinition += `\t${entry} --> [*]: End\n`;
                            }
                        }
                        if (state.transitions) {
                            for (const transition of state.transitions) {
                                let event = transition.substring(0, transition.indexOf("->")).trim().replace(/-/g, "_");
                                if (event === "*") {
                                    if (state.isDecisionState) {
                                        event = "Otherwise";
                                    } else {
                                        event = "Always";
                                    }
                                }
                                let target = transition.substring(transition.indexOf("->") + 2).trim().replace(/-/g, "_");
                                diagramDefinition += `\t${entry} --> ${target}: ${event}\n`;
                            }
                        }
                        if (state.actionList || state.entryActions) {
                            diagramDefinition += `\tstate ${entry} {\n`;

                            if (state.entryActions && state.entryActions.length > 0) {
                                for (let i = 0; i < state.entryActions.length; i++) {
                                    let action = state.entryActions[i].replace(/-/g, "_").trim();
                                    if (action.startsWith("set ")) {
                                        action = "Execute";
                                    }
                                    if (i === 0) {
                                        diagramDefinition += `\t\t[*] --> ${action}: Start\n`;
                                    } else {
                                        let previousAction = state.entryActions[i - 1].replace(/-/g, "_").trim();
                                        if (previousAction.startsWith("set ")) {
                                            previousAction = "Execute";
                                        }
                                        diagramDefinition += `\t\t${previousAction} --> ${action}: Then\n`;
                                    }
                                }
                            }

                            if (state.actionList && state.actionList.length > 0) {
                                let startActionState = state.entryActions
                                    ? state.entryActions[state.entryActions.length - 1].replace(/-/g, "_").trim()
                                    : "[*]";
                                if (startActionState.startsWith("set ")) {
                                    startActionState = "Execute";
                                }

                                for (let i = 0; i < state.actionList.length; i++) {
                                    let action = state.actionList[i];
                                    if (action.startsWith("set ")) {
                                        action = "Execute";
                                    }
                                    const label = startActionState === "[*]" ? "Start" : "Then";
                                    if (i === 0) {
                                        diagramDefinition += `\t\t${startActionState} --> ${action}: ${label}\n`;
                                    }
                                    if (i === state.actionList.length - 1) {
                                        diagramDefinition += `\t\t${action} --> [*]: End\n`;
                                    }
                                }
                            } else if (state.entryActions && state.entryActions.length > 0) {
                                let lastEntryAction = state.entryActions[state.entryActions.length - 1];
                                if (lastEntryAction.startsWith("set ")) {
                                    lastEntryAction = "Execute";
                                }
                                diagramDefinition += `\t\t${lastEntryAction} --> [*]: End\n`;
                            }
                            diagramDefinition += `\t}\n`;
                        }


                        if (state.viewState) {
                            diagramDefinition += `\tnote left of ${entry}\n\t\tView: ${state.viewId}\n\tend note\n`;
                        }
                    }
                }

                $("#webflowMarkdownContainer").removeClass("hide");
                $("#webflowMarkdown").empty().text(diagramDefinition);
                const {svg, bindFunctions} = await mermaid.render("webflowDiagram", diagramDefinition);
                const container = document.getElementById("webflowContainer");
                container.innerHTML = svg;
                $("#webflowDiagram").removeClass("hide");
                Swal.close();
                bindFunctions?.(container);
                updateNavigationSidebar();
            },
            error: (xhr, textStatus, errorThrown) => {
                console.error("Error fetching data:", errorThrown);
                Swal.close();
            }
        });
    }

    if (actuatorEndpoints.springWebflow) {
        mermaid.initialize({
            startOnLoad: false,
            securityLevel: "loose",
            theme: "base",
            logLevel: 4,
            themeVariables: {
                primaryColor: "deepskyblue",
                secondaryColor: "#73e600",
                lineColor: "deepskyblue"
            }
        });

        $("#webflowFilter").empty().selectmenu({
            change: (event, data) => {
                $("#webflowStateFilter").empty();
                drawFlowStateDiagram();
            }
        });
        $("#webflowStateFilter").empty().selectmenu({
            change: (event, data) => drawFlowStateDiagram()
        });

        $.ajax({
            url: `${actuatorEndpoints.springWebflow}`,
            type: "GET",
            headers: {
                "Content-Type": "application/json"
            },
            success: async (response, textStatus, xhr) => {
                const availableFlows = Object.keys(response);
                $.each(availableFlows, (idx, item) => {
                    $("#webflowFilter").append(
                        $("<option>", {
                            value: item,
                            text: item.toUpperCase(),
                            selected: idx === 0
                        })
                    );
                });
                $("#webflowFilter").selectmenu("refresh");
                drawFlowStateDiagram();
            },
            error: (xhr, textStatus, errorThrown) => console.error("Error fetching data:", errorThrown)
        });
    }
}
