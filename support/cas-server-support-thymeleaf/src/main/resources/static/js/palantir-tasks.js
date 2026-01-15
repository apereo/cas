async function initializeScheduledTasksOperations() {

    const threadDumpTable = $("#threadDumpTable").DataTable({
        pageLength: 10,
        autoWidth: false,
        columnDefs: [
            {width: "5%", targets: 0},
            {width: "35%", targets: 1},
            {width: "30%", targets: 2},
            {width: "10%", targets: 3},
            {width: "10%", targets: 4},
            {width: "10%", targets: 5}
        ],
        drawCallback: settings => {
            $("#threadDumpTable tr").addClass("mdc-data-table__row");
            $("#threadDumpTable td").addClass("mdc-data-table__cell");
        }
    });


    const groupColumn = 0;
    const scheduledtasks = $("#scheduledTasksTable").DataTable({
        pageLength: 25,
        columnDefs: [{visible: false, targets: groupColumn}],
        order: [groupColumn, "asc"],
        drawCallback: settings => {
            $("#scheduledTasksTable tr").addClass("mdc-data-table__row");
            $("#scheduledTasksTable td").addClass("mdc-data-table__cell");

            const api = settings.api;
            const rows = api.rows({page: "current"}).nodes();
            let last = null;
            api.column(groupColumn, {page: "current"})
                .data()
                .each((group, i) => {
                    if (last !== group) {
                        $(rows).eq(i).before(
                            `<tr style='font-weight: bold; background-color:var(--cas-theme-primary); color:var(--mdc-text-button-label-text-color);'>
                                            <td colspan="2">${group}</td>
                                        </tr>`.trim());
                        last = group;
                    }
                });
        }
    });

    function addScheduledTaskCategory(groupName, items) {
        if (items !== undefined && Array.isArray(items)) {
            for (const group of items) {
                const flattened = flattenJSON(group);
                for (const [key, value] of Object.entries(flattened)) {
                    const target = flattened["runnable.target"];
                    if (target !== value) {
                        scheduledtasks.row.add({
                            0: `<code>${camelcaseToTitleCase(groupName)} / ${getLastTwoWords(target)}</code>`,
                            1: `<code>${key}</code>`,
                            2: `<code>${value}</code>`
                        });
                    }
                }
            }
        }
    }

    if (actuatorEndpoints.scheduledtasks) {
        $.get(actuatorEndpoints.scheduledtasks, response => {
            scheduledtasks.clear();
            for (const group of Object.keys(response)) {
                addScheduledTaskCategory(group, response[group]);
            }
            scheduledtasks.draw();
        }).fail((xhr, status, error) => {
            console.error("Error fetching data:", error);
            displayBanner(xhr);
        });
    }

    if (actuatorEndpoints.metrics) {
        initializeJvmMetrics();
        setInterval(() => {
            if (currentActiveTab === Tabs.TASKS.index) {
                initializeJvmMetrics();
            }
        }, palantirSettings().refreshInterval);
    }

    function fetchThreadDump() {
        $.get(actuatorEndpoints.threaddump, response => {
            threadDumpTable.clear();
            for (const thread of response.threads) {
                threadDumpTable.row.add({
                    0: `<code>${thread.threadId}</code>`,
                    1: `<code>${thread.threadName}</code>`,
                    2: `<code>${thread.threadState}</code>`,
                    3: `<code>${thread.priority}</code>`,
                    4: `<code>${thread.daemon}</code>`,
                    5: `<code>${thread.suspended}</code>`
                });
            }
            threadDumpTable.draw();
        }).fail((xhr, status, error) => {
            console.error("Error fetching data:", error);
            displayBanner(xhr);
        });
    }


    if (actuatorEndpoints.threaddump) {
        fetchThreadDump();
        setInterval(() => {
            if (currentActiveTab === Tabs.TASKS.index) {
                fetchThreadDump();
            }
        }, palantirSettings().refreshInterval);
    }
}

function initializeJvmMetrics() {
    function fetchJvmThreadMetric(metricName) {
        return new Promise((resolve, reject) =>
            $.get(`${actuatorEndpoints.metrics}/${metricName}`, response => resolve(response.measurements[0].value)).fail((xhr, status, error) => {
                console.error("Error fetching data:", error);
                displayBanner(xhr);
                reject(error);
            })
        );
    }

    async function fetchJvmThreadsMetrics() {
        const promises = [
            "jvm.threads.daemon",
            "jvm.threads.live",
            "jvm.threads.peak",
            "jvm.threads.started",
            "jvm.threads.states"
        ].map(metric => fetchJvmThreadMetric(metric));
        const results = await Promise.all(promises);
        return results.map(result => Number(result));
    }

    async function fetchThreadDump() {
        return new Promise((resolve, reject) =>
            $.get(actuatorEndpoints.threaddump, response => {
                let threadData = {};
                for (const thread of response.threads) {
                    if (!threadData[thread.threadState]) {
                        threadData[thread.threadState] = 0;
                    }
                    threadData[thread.threadState] += 1;
                }
                resolve(threadData);
            }).fail((xhr, status, error) => {
                console.error("Error thread dump:", error);
                displayBanner(xhr);
                reject(error);
            }));

    }

    if (actuatorEndpoints.metrics) {
        fetchJvmThreadsMetrics()
            .then(payload => {
                jvmThreadsChart.data.datasets[0].data = payload;
                jvmThreadsChart.update();
            });
    }


    if (actuatorEndpoints.threaddump) {
        fetchThreadDump().then(payload => {
            threadDumpChart.data.labels = Object.keys(payload);
            const values = Object.values(payload);
            threadDumpChart.data.datasets[0] = {
                label: `${values.reduce((sum, value) => sum + value, 0)} Threads`,
                data: values
            };
            threadDumpChart.update();
        });
    }
}

