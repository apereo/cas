async function initializeScheduledTasksOperations() {

    const threadDumpStacks = new Map();
    const threadDumpStackDialogElement = document.getElementById("threadDumpStackDialog");
    const threadDumpStackDialog = threadDumpStackDialogElement
        ? window.mdc.dialog.MDCDialog.attachTo(threadDumpStackDialogElement)
        : undefined;

    function escapeHtml(value) {
        return String(value ?? "").replace(/[&<>"']/g, s => ({
            "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;"
        }[s]));
    }

    function toThreadStackKey(thread) {
        return String(thread.threadId ?? thread.id ?? thread.threadName ?? "");
    }

    function threadStackTrace(thread) {
        return Array.isArray(thread.stackTrace) ? thread.stackTrace : [];
    }

    function isVirtualThread(thread) {
        return thread.virtual === true || thread.virtualThread === true || String(thread.threadName ?? "").startsWith("VirtualThread[");
    }

    function threadStateClass(state) {
        switch (state) {
            case "RUNNABLE":
                return "thread-state-runnable";
            case "BLOCKED":
                return "thread-state-blocked";
            case "WAITING":
            case "TIMED_WAITING":
                return "thread-state-waiting";
            case "TERMINATED":
                return "thread-state-terminated";
            default:
                return "thread-state-other";
        }
    }

    function renderThreadName(thread) {
        const name = thread.threadName ?? "N/A";
        const virtualBadge = isVirtualThread(thread)
            ? "<span class=\"thread-dump-virtual-badge\">virtual</span>"
            : "";
        return `<div class="thread-dump-name-cell">
                    <code title="${escapeHtml(name)}">${escapeHtml(name)}</code>
                    ${virtualBadge}
                </div>`;
    }

    function renderThreadState(state) {
        const safeState = escapeHtml(state ?? "UNKNOWN");
        return `<span class="thread-state-badge ${threadStateClass(state)}">${safeState}</span>`;
    }

    function renderBoolean(value) {
        return value === true
            ? "<i class=\"mdc-tab__icon mdi mdi-check\" aria-label=\"true\"></i>"
            : "<span aria-label=\"false\">-</span>";
    }

    function threadCpuTime(thread) {
        const cpuTime = thread.cpuTime ?? thread.cpuTimeNanos ?? thread.threadCpuTime ?? thread.threadCpuTimeNanos ?? thread.cpu;
        if (cpuTime === undefined || cpuTime === null || cpuTime < 0) {
            return null;
        }
        return cpuTime;
    }

    function formatThreadCpuTime(thread) {
        const cpuTime = threadCpuTime(thread);
        if (cpuTime === null) {
            return "-";
        }
        if (typeof cpuTime === "string") {
            return escapeHtml(cpuTime);
        }
        const milliseconds = cpuTime > 1_000_000 ? cpuTime / 1_000_000 : cpuTime;
        return `${Math.round(milliseconds)} ms`;
    }

    function renderThreadStackButton(thread) {
        const stackSize = threadStackTrace(thread).length;
        if (stackSize === 0) {
            return "";
        }
        const stackKey = escapeHtml(toThreadStackKey(thread));
        return `<button type="button"
                        class="mdc-button mdc-button--raised btn btn-link min-width-32x thread-stack-button"
                        data-thread-stack-key="${stackKey}"
                        title="View stack trace for ${escapeHtml(thread.threadName ?? "thread")}"
                        aria-label="View stack trace for ${escapeHtml(thread.threadName ?? "thread")}">
                    <span class="mdc-button__ripple"></span>
                    <span class="mdc-button__label">
                        <i class="mdi mdi-cog-outline min-width-32x" aria-hidden="true"></i>
                    </span>
                </button>`;
    }

    function formatStackFrame(frame) {
        if (typeof frame === "string") {
            return frame;
        }
        const className = frame.className ?? "";
        const methodName = frame.methodName ?? "";
        const fileName = frame.fileName ?? "Unknown Source";
        const lineNumber = Number.isFinite(frame.lineNumber) && frame.lineNumber >= 0 ? `:${frame.lineNumber}` : "";
        const location = frame.nativeMethod === true ? "Native Method" : `${fileName}${lineNumber}`;
        return `at ${className}.${methodName}(${location})`;
    }

    function applyMdcDataTableControls(selector) {
        const wrapper = $(selector).closest(".dataTables_wrapper, .dt-container");
        wrapper.find(".dataTables_filter input, .dt-search input")
            .addClass("mdc-text-field__input form-control palantir-datatable-search-input")
            .attr("aria-label", "Search table");
        wrapper.find(".dataTables_length select, .dt-length select")
            .addClass("mdc-select__native-control form-select");
        wrapper.find(".paginate_button, .dt-paging-button")
            .addClass("mdc-button mdc-button--outlined");
    }

    function showThreadStack(stackKey) {
        const thread = threadDumpStacks.get(stackKey);
        if (!thread) {
            return;
        }
        const stackTrace = threadStackTrace(thread);
        $("#threadDumpStackDialog-title").text(`Thread Stack: ${thread.threadName ?? "N/A"}`);
        $("#threadDumpStackSummary").html(`
            <span><strong>ID:</strong> ${escapeHtml(thread.threadId ?? "N/A")}</span>
            <span><strong>State:</strong> ${escapeHtml(thread.threadState ?? "UNKNOWN")}</span>
            <span><strong>Frames:</strong> ${stackTrace.length}</span>
        `);
        $("#threadDumpStackTrace").text(stackTrace.map(formatStackFrame).join("\n"));
        threadDumpStackDialog?.open();
    }

    const threadDumpTable = $("#threadDumpTable").DataTable({
        pageLength: 10,
        autoWidth: false,
        order: [],
        columns: [
            {
                data: "threadName",
                width: "31%",
                render: (data, type, row) => type === "display" ? renderThreadName(row) : data
            },
            {
                data: "threadId",
                width: "8%",
                render: (data, type) => type === "display" ? `<code>${escapeHtml(data)}</code>` : data
            },
            {
                data: "threadState",
                width: "15%",
                render: (data, type) => type === "display" ? renderThreadState(data) : data
            },
            {
                data: "priority",
                width: "8%",
                render: (data, type) => type === "display" ? `<code>${escapeHtml(data)}</code>` : data
            },
            {
                data: "daemon",
                width: "8%",
                render: (data, type) => type === "display" ? renderBoolean(data) : data
            },
            {
                data: null,
                width: "12%",
                render: (data, type, row) => {
                    const cpuTime = threadCpuTime(row);
                    if (type === "sort" || type === "type") {
                        return cpuTime ?? -1;
                    }
                    return formatThreadCpuTime(row);
                }
            },
            {
                data: null,
                orderable: false,
                searchable: false,
                width: "18%",
                className: "thread-stack-action",
                render: (data, type, row) => type === "display" ? renderThreadStackButton(row) : threadStackTrace(row).length
            }
        ],
        initComplete: () => applyMdcDataTableControls("#threadDumpTable"),
        drawCallback: settings => {
            $("#threadDumpTable tr").addClass("mdc-data-table__row");
            $("#threadDumpTable td").addClass("mdc-data-table__cell");
            applyMdcDataTableControls("#threadDumpTable");
        }
    });

    $("#threadDumpTable").on("click", "button.thread-stack-button", function () {
        showThreadStack($(this).data("thread-stack-key").toString());
    });


    const scheduledtasks = $("#scheduledTasksTable").DataTable({
        pageLength: 25,
        autoWidth: false,
        order: [[0, "asc"], [1, "asc"]],
        columns: [
            {
                data: "type",
                width: "8%",
                render: (data, type) => type === "display" ? renderScheduledTaskType(data) : data
            },
            {
                data: "task",
                width: "14%",
                render: (data, type) => type === "display" ? `<code>${escapeHtml(data)}</code>` : data
            },
            {
                data: "target",
                width: "20%",
                render: (data, type) => type === "display" ? renderScheduledTaskTarget(data) : data
            },
            {
                data: "initialDelay",
                width: "8%",
                render: (data, type) => type === "display" ? formatScheduledTaskDuration(data) : data
            },
            {
                data: "interval",
                width: "8%",
                render: (data, type) => type === "display" ? formatScheduledTaskDuration(data) : data
            },
            {
                data: "lastStatus",
                width: "9%",
                render: (data, type, row) => type === "display" ? renderScheduledTaskStatus(data, row.exception) : data
            },
            {
                data: "lastExecution",
                width: "10%",
                render: (data, type) => type === "display" ? formatScheduledTaskTime(data) : data
            },
            {
                data: "nextExecution",
                width: "10%",
                render: (data, type) => type === "display" ? formatScheduledTaskTime(data) : data
            }
        ],
        drawCallback: settings => {
            $("#scheduledTasksTable tr").addClass("mdc-data-table__row");
            $("#scheduledTasksTable td").addClass("mdc-data-table__cell");
            applyMdcDataTableControls("#scheduledTasksTable");
        }
    });

    function renderScheduledTaskType(value) {
        return `<span class="scheduled-task-type">${escapeHtml(camelcaseToTitleCase(value ?? "Unknown"))}</span>`;
    }

    function renderScheduledTaskTarget(value) {
        if (!value) {
            return "-";
        }
        return `<code class="scheduled-task-target" title="${escapeHtml(value)}">${escapeHtml(value)}</code>`;
    }

    function renderOptionalCode(value) {
        return value === undefined || value === null || value === "" ? "-" : `<code>${escapeHtml(value)}</code>`;
    }

    function formatScheduledTaskDuration(value) {
        if (value === undefined || value === null || value === "") {
            return "-";
        }
        const duration = Number(value);
        if (!Number.isFinite(duration)) {
            return escapeHtml(value);
        }
        if (duration === 0) {
            return "0 ms";
        }
        if (duration % 3_600_000 === 0) {
            return `${duration / 3_600_000} h`;
        }
        if (duration % 60_000 === 0) {
            return `${duration / 60_000} min`;
        }
        if (duration % 1_000 === 0) {
            return `${duration / 1_000} s`;
        }
        return `${duration} ms`;
    }

    function formatScheduledTaskTime(value) {
        if (!value) {
            return "-";
        }
        const date = new Date(value);
        return Number.isNaN(date.getTime()) ? escapeHtml(value) : escapeHtml(date.toLocaleString());
    }

    function scheduledTaskStatusClass(status) {
        switch (status) {
            case "SUCCESS":
                return "scheduled-task-status-success";
            case "ERROR":
            case "FAILURE":
            case "FAILED":
                return "scheduled-task-status-error";
            case "STARTED":
            case "RUNNING":
                return "scheduled-task-status-running";
            default:
                return "scheduled-task-status-none";
        }
    }

    function renderScheduledTaskStatus(status, exception) {
        const value = status ?? "NONE";
        const title = exception?.message ?? exception?.type ?? value;
        return `<span class="scheduled-task-status ${scheduledTaskStatusClass(value)}" title="${escapeHtml(title)}">
                    ${escapeHtml(value)}
                </span>`;
    }

    function renderScheduledTaskException(exception) {
        if (!exception) {
            return "-";
        }
        const message = exception.message ? `: ${exception.message}` : "";
        return `<code class="scheduled-task-exception" title="${escapeHtml(exception.type)}${escapeHtml(message)}">
                    ${escapeHtml(exception.type)}${escapeHtml(message)}
                </code>`;
    }

    function toScheduledTaskRow(groupName, task) {
        const target = task.runnable?.target ?? "";
        const lastExecution = task.lastExecution ?? {};
        const taskName = target ? getLastTwoWords(target) : camelcaseToTitleCase(groupName ?? "Scheduled Task");
        return {
            type: groupName,
            task: taskName,
            target: target,
            expression: task.expression,
            trigger: task.trigger,
            initialDelay: task.initialDelay,
            interval: task.interval,
            lastStatus: lastExecution.status ?? "NONE",
            lastExecution: lastExecution.time,
            nextExecution: task.nextExecution?.time,
            exception: lastExecution.exception
        };
    }

    function addScheduledTaskCategory(groupName, items) {
        if (items !== undefined && Array.isArray(items)) {
            for (const task of items) {
                scheduledtasks.row.add(toScheduledTaskRow(groupName, task));
            }
        }
    }

    if (CasActuatorEndpoints.scheduledTasks()) {
        $.get(CasActuatorEndpoints.scheduledTasks(), response => {
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

    if (CasActuatorEndpoints.metrics()) {
        initializeJvmMetrics();
        setInterval(() => {
            if (currentActiveTab === Tabs.TASKS.index) {
                initializeJvmMetrics();
            }
        }, palantirSettings().refreshInterval);
    }

    function fetchThreadDump() {
        $.get(CasActuatorEndpoints.threadDump(), response => {
            threadDumpTable.clear();
            threadDumpStacks.clear();
            for (const thread of response.threads) {
                threadDumpStacks.set(toThreadStackKey(thread), thread);
                threadDumpTable.row.add(thread);
            }
            threadDumpTable.draw();
        }).fail((xhr, status, error) => {
            console.error("Error fetching data:", error);
            displayBanner(xhr);
        });
    }


    if (CasActuatorEndpoints.threadDump()) {
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
            $.get(`${CasActuatorEndpoints.metrics()}/${metricName}`, response => resolve(response.measurements[0].value)).fail((xhr, status, error) => {
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
            $.get(CasActuatorEndpoints.threadDump(), response => {
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

    if (CasActuatorEndpoints.metrics()) {
        fetchJvmThreadsMetrics()
            .then(payload => {
                jvmThreadsChart.data.datasets[0].data = payload;
                jvmThreadsChart.resize();
                jvmThreadsChart.update();
            });
    }


    if (CasActuatorEndpoints.threadDump()) {
        fetchThreadDump().then(payload => {
            threadDumpChart.data.labels = Object.keys(payload);
            const values = Object.values(payload);
            threadDumpChart.data.datasets[0] = {
                label: `${values.reduce((sum, value) => sum + value, 0)} Threads`,
                data: values
            };
            threadDumpChart.resize();
            threadDumpChart.update();
        });
    }
}
