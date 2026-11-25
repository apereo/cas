/* Memory, memory pools and heap dump analysis, as the System page shows them. */

function formatMemoryBytes(value) {
    if (!Number.isFinite(value)) {
        return "N/A";
    }
    if (value < 0) {
        return "Unlimited";
    }
    const units = ["B", "KB", "MB", "GB", "TB"];
    let unitIndex = 0;
    let result = value;
    while (result >= 1024 && unitIndex < units.length - 1) {
        result /= 1024;
        unitIndex++;
    }
    const precision = unitIndex === 0 ? 0 : 1;
    return `${result.toFixed(precision)} ${units[unitIndex]}`;
}

function formatMemoryPercent(value) {
    if (!Number.isFinite(value)) {
        return "N/A";
    }
    return `${Math.round(value)}%`;
}

function memoryUsagePercent(used, max) {
    if (!Number.isFinite(used) || !Number.isFinite(max) || max <= 0) {
        return null;
    }
    return Math.min(100, Math.max(0, (used / max) * 100));
}

function memoryMetricUrl(metricName, tags = []) {
    const params = new URLSearchParams();
    tags.forEach(tag => params.append("tag", tag));
    const query = params.toString();
    return `${CasActuatorEndpoints.metrics()}/${encodeURIComponent(metricName)}${query ? `?${query}` : ""}`;
}

async function getMemoryMetricValue(metricName, tags = []) {
    return new Promise(resolve => {
        $.get(memoryMetricUrl(metricName, tags), response => {
            const measurement = response.measurements?.find(entry => entry.statistic === "VALUE")
                ?? response.measurements?.[0];
            const value = Number(measurement?.value);
            resolve(Number.isFinite(value) ? value : null);
        }).fail((xhr, status, error) => {
            console.debug(`Unable to fetch ${metricName}`, error);
            resolve(null);
        });
    });
}

/**
 * Read the untagged jvm.memory.used metric, which carries both the aggregate measurement and
 * the list of memory pool ids in its availableTags. One request answers both questions.
 *
 * @returns {Promise<{names: string[]}>} the memory pool ids, empty when unavailable
 */
async function getMemoryPoolNames() {
    return new Promise(resolve => {
        $.get(memoryMetricUrl("jvm.memory.used"), response => {
            const idTag = response.availableTags?.find(entry => entry.tag === "id");
            resolve(idTag?.values ?? []);
        }).fail((xhr, status, error) => {
            console.debug("Unable to fetch memory pool names", error);
            resolve([]);
        });
    });
}

function getHeapDumpAnalysisEntries(response) {
    const entries = response?.classesByRetainedSize
        ?? response?.classesByShallowSize
        ?? response?.classes
        ?? response?.topClasses
        ?? response?.histogram
        ?? response?.entries
        ?? response?.data
        ?? [];
    return Array.isArray(entries) ? entries : [];
}

function normalizeHeapDumpEntry(entry) {
    const retainedSize = Number(entry.retainedSizeBytes ?? entry.retainedSize ?? entry.retainedBytes);
    const shallowSize = Number(entry.shallowSizeBytes ?? entry.shallowSize ?? entry.bytes ?? entry.size);
    const instances = Number(entry.instanceCount ?? entry.instances ?? entry.objects ?? entry.count ?? 0);
    const averageShallowSize = Number(entry.averageShallowSizeBytes ?? entry.averageShallowSize ?? entry.avgShallowSize);
    const averageRetainedSize = Number(entry.averageRetainedSizeBytes ?? entry.averageRetainedSize ?? entry.avgRetainedSize);
    const retainedToShallowRatio = Number(entry.retainedToShallowRatio ?? entry.retainedShallowRatio ?? entry.retainedRatio);
    return {
        className: entry.className ?? entry.class ?? entry.name ?? "",
        instances: Number.isFinite(instances) ? instances : 0,
        retained: Number.isFinite(retainedSize) && retainedSize > 0
            ? retainedSize
            : (Number.isFinite(shallowSize) ? shallowSize : 0),
        averageShallowSize: Number.isFinite(averageShallowSize) && averageShallowSize >= 0 ? averageShallowSize : null,
        averageRetainedSize: Number.isFinite(averageRetainedSize) && averageRetainedSize >= 0 ? averageRetainedSize : null,
        retainedToShallowRatio: Number.isFinite(retainedToShallowRatio) && retainedToShallowRatio >= 0 ? retainedToShallowRatio : null
    };
}

function formatNumber(value) {
    return Number(value || 0).toLocaleString();
}

function formatHeapDumpRatio(value) {
    return Number.isFinite(value) ? `${value.toFixed(2)}x` : "N/A";
}

function getHeapDumpTotalObjects(response, entries) {
    const total = Number(response?.totalObjects ?? response?.objects ?? response?.objectCount);
    return Number.isFinite(total)
        ? total
        : entries.reduce((sum, entry) => sum + entry.instances, 0);
}

function getHeapDumpTotalBytes(response, entries) {
    const total = Number(response?.totalRetainedSize ?? response?.totalBytes ?? response?.retainedSize ?? response?.bytes);
    return Number.isFinite(total)
        ? total
        : entries.reduce((sum, entry) => sum + entry.retained, 0);
}

function renderHeapDumpRetainedCell(entry) {
    const width = Math.max(0, Math.min(100, Number(entry.retainedWidth ?? 0)));
    return `
        <div class="heap-dump-retained-cell">
            <span class="heap-dump-retained-bar">
                <span style="width:${width}%"></span>
            </span>
            <span>${formatMemoryBytes(entry.retained)}</span>
        </div>`;
}

async function fetchHeapDumpFile() {
    const response = await fetch(CasActuatorEndpoints.heapDump(), {
        credentials: "include"
    });
    if (!response.ok) {
        throw {status: response.status, message: "Unable to capture heap dump."};
    }
    return response.blob();
}

async function analyzeHeapDumpFile(heapDump) {
    const formData = new FormData();
    formData.append("file", heapDump, "heapdump.hprof");
    const endpoint = new URL(CasActuatorEndpoints.heapDumpAnalysis(), window.location.href);
    const response = await fetch(endpoint.toString(), {
        method: "POST",
        credentials: "include",
        headers: {
            Accept: "application/json"
        },
        body: formData
    });
    if (!response.ok) {
        throw {status: response.status, message: "Unable to analyze heap dump."};
    }
    return response.json();
}

function showHeapDumpAnalysisProgress(message, progress) {
    Swal.fire({
        title: "Heap Dump Analysis",
        html: `
            <div class="heap-dump-swal-progress">
                <div id="heapDumpAnalysisProgressMessage" class="heap-dump-swal-progress-message">${message}</div>
                <div class="heap-dump-swal-progress-track">
                    <span id="heapDumpAnalysisProgressBar" style="width:${progress}%"></span>
                </div>
            </div>`,
        allowOutsideClick: false,
        allowEscapeKey: false,
        showConfirmButton: false,
        didOpen: () => Swal.showLoading()
    });
}

function updateHeapDumpAnalysisProgress(message, progress) {
    const container = Swal.getHtmlContainer();
    if (!container) {
        return;
    }
    const messageElement = container.querySelector("#heapDumpAnalysisProgressMessage");
    const progressElement = container.querySelector("#heapDumpAnalysisProgressBar");
    if (messageElement) {
        messageElement.textContent = message;
    }
    if (progressElement) {
        progressElement.style.width = `${progress}%`;
    }
}

function getHeapDumpAnalysisErrorMessage(error) {
    if (error?.message) {
        return error.message;
    }
    if (error?.status) {
        return `HTTP error: ${error.status}`;
    }
    return "Unable to analyze heap dump.";
}

function renderLiveMemoryCard(title, icon, colorClass, values, caption) {
    if (!Number.isFinite(values.used) && !Number.isFinite(values.committed)) {
        return "";
    }

    const max = Number.isFinite(values.max) && values.max >= 0 ? values.max : null;
    const barLimit = max ?? values.committed;
    const usage = memoryUsagePercent(values.used, barLimit);
    const width = usage === null ? 0 : usage;
    const footer = max !== null && usage !== null
        ? `${formatMemoryPercent(usage)} of max used`
        : caption;

    return `
        <div class="live-memory-card">
            <div class="live-memory-card-title">
                <i class="mdi ${icon} ${colorClass}" aria-hidden="true"></i>
                ${title}
            </div>
            <div class="live-memory-primary">
                <span>Used</span>
                <strong>${formatMemoryBytes(values.used)}</strong>
            </div>
            <div class="live-memory-progress">
                <span class="live-memory-progress-fill ${colorClass}" style="width:${width}%"></span>
            </div>
            <div class="live-memory-stats">
                <div><span>Used</span><strong>${formatMemoryBytes(values.used)}</strong></div>
                <div><span>Committed</span><strong>${formatMemoryBytes(values.committed)}</strong></div>
                <div><span>Max</span><strong>${formatMemoryBytes(values.max)}</strong></div>
            </div>
            <div class="live-memory-caption">${footer}</div>
        </div>`;
}

function renderLiveMemoryPoolRow(pool) {
    const usage = memoryUsagePercent(pool.used, pool.max);
    const usageClass = usage !== null && usage >= 80 ? "warning" : "heap";
    const width = usage === null ? 0 : usage;
    const usageLabel = usage === null ? "N/A" : formatMemoryPercent(usage);

    return {
        0: `${palantirCode(pool.name)}`,
        1: formatMemoryBytes(pool.used),
        2: formatMemoryBytes(pool.committed),
        3: formatMemoryBytes(pool.max),
        4: `
            <div class="live-memory-pool-usage">
                <span class="live-memory-pool-bar">
                    <span class="live-memory-pool-fill ${usageClass}" style="width:${width}%"></span>
                </span>
                <span>${usageLabel}</span>
            </div>`
    };
}
