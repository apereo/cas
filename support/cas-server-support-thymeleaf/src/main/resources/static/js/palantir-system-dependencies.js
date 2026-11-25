/* The dependencies of CAS and their known vulnerabilities, with CVSS scores, as the System page shows them. */

function normalizeDependency(dependency) {
    const entry = dependency ?? {};
    return {
        groupId: entry.groupId ?? "",
        artifactId: entry.artifactId ?? "",
        version: entry.version ?? "",
        source: entry.source ?? ""
    };
}

function renderDependencyVersion(version) {
    return `<span class="cas-dependency-version">${escapePalantirMarkup(version)}</span>`;
}

function renderVulnerabilityId(id) {
    return `<span class="cas-vulnerability-id">${escapePalantirMarkup(id)}</span>`;
}

function roundUpCvssScore(value) {
    return Math.ceil(value * 10) / 10;
}

function cvssSeverityLabel(score) {
    if (!Number.isFinite(score)) {
        return "Unknown";
    }
    if (score === 0) {
        return "None";
    }
    if (score < 4) {
        return "Low";
    }
    if (score < 7) {
        return "Medium";
    }
    if (score < 9) {
        return "High";
    }
    return "Critical";
}

function parseCvssVector(vector) {
    return Object.fromEntries(String(vector ?? "")
        .split("/")
        .filter(part => part.includes(":"))
        .map(part => {
            const [key, value] = part.split(":");
            return [key, value];
        }));
}

function calculateCvssV3Score(vector) {
    const metrics = parseCvssVector(vector);

    const metricValues = {
        AV: {N: 0.85, A: 0.62, L: 0.55, P: 0.2},
        AC: {L: 0.77, H: 0.44},
        UI: {N: 0.85, R: 0.62},
        S: {U: "U", C: "C"},
        C: {H: 0.56, L: 0.22, N: 0},
        I: {H: 0.56, L: 0.22, N: 0},
        A: {H: 0.56, L: 0.22, N: 0}
    };
    const scope = metricValues.S[metrics.S];
    const privilegeRequired = {
        U: {N: 0.85, L: 0.62, H: 0.27},
        C: {N: 0.85, L: 0.68, H: 0.5}
    }[scope]?.[metrics.PR];
    const values = {
        attackVector: metricValues.AV[metrics.AV],
        attackComplexity: metricValues.AC[metrics.AC],
        privilegeRequired,
        userInteraction: metricValues.UI[metrics.UI],
        confidentiality: metricValues.C[metrics.C],
        integrity: metricValues.I[metrics.I],
        availability: metricValues.A[metrics.A]
    };
    if (!scope || Object.values(values).some(value => !Number.isFinite(value))) {
        return null;
    }

    const impactSubScore = 1 - ((1 - values.confidentiality) * (1 - values.integrity) * (1 - values.availability));
    const impact = scope === "U"
        ? 6.42 * impactSubScore
        : 7.52 * (impactSubScore - 0.029) - 3.25 * Math.pow(impactSubScore - 0.02, 15);
    const exploitability = 8.22 * values.attackVector * values.attackComplexity
        * values.privilegeRequired * values.userInteraction;
    if (impact <= 0) {
        return 0;
    }
    const baseScore = scope === "U"
        ? Math.min(impact + exploitability, 10)
        : Math.min(1.08 * (impact + exploitability), 10);
    return roundUpCvssScore(baseScore);
}

function calculateCvssV4Score(vector) {
    const metrics = parseCvssVector(vector);
    const metricValues = {
        AV: {N: 0.85, A: 0.62, L: 0.55, P: 0.2},
        AC: {L: 0.77, H: 0.44},
        AT: {N: 1, P: 0.7},
        PR: {N: 0.85, L: 0.62, H: 0.27},
        UI: {N: 0.85, P: 0.62, A: 0.45},
        VC: {H: 0.56, L: 0.22, N: 0},
        VI: {H: 0.56, L: 0.22, N: 0},
        VA: {H: 0.56, L: 0.22, N: 0},
        SC: {H: 0.56, L: 0.22, N: 0},
        SI: {S: 0.56, H: 0.56, L: 0.22, N: 0},
        SA: {S: 0.56, H: 0.56, L: 0.22, N: 0}
    };
    const exploitabilityMetrics = {
        attackVector: metricValues.AV[metrics.AV],
        attackComplexity: metricValues.AC[metrics.AC],
        attackRequirements: metricValues.AT[metrics.AT],
        privilegeRequired: metricValues.PR[metrics.PR],
        userInteraction: metricValues.UI[metrics.UI]
    };
    const impactMetrics = {
        vulnerableConfidentiality: metricValues.VC[metrics.VC],
        vulnerableIntegrity: metricValues.VI[metrics.VI],
        vulnerableAvailability: metricValues.VA[metrics.VA],
        subsequentConfidentiality: metricValues.SC[metrics.SC],
        subsequentIntegrity: metricValues.SI[metrics.SI],
        subsequentAvailability: metricValues.SA[metrics.SA]
    };
    if (Object.values(exploitabilityMetrics).some(value => !Number.isFinite(value))
        || Object.values(impactMetrics).some(value => !Number.isFinite(value))) {
        return null;
    }

    const vulnerableImpact = 1 - ((1 - impactMetrics.vulnerableConfidentiality)
        * (1 - impactMetrics.vulnerableIntegrity)
        * (1 - impactMetrics.vulnerableAvailability));
    const subsequentImpact = 1 - ((1 - impactMetrics.subsequentConfidentiality)
        * (1 - impactMetrics.subsequentIntegrity)
        * (1 - impactMetrics.subsequentAvailability));
    const impact = Math.max(vulnerableImpact, subsequentImpact);
    if (impact <= 0) {
        return 0;
    }
    const exploitability = 8.22 * exploitabilityMetrics.attackVector
        * exploitabilityMetrics.attackComplexity
        * exploitabilityMetrics.attackRequirements
        * exploitabilityMetrics.privilegeRequired
        * exploitabilityMetrics.userInteraction;
    return roundUpCvssScore(Math.min((6.42 * impact) + exploitability, 10));
}

function calculateCvssScore(type, vector) {
    if (String(type ?? "").toUpperCase() === "CVSS_V4" || String(vector ?? "").startsWith("CVSS:4.0/")) {
        return calculateCvssV4Score(vector);
    }
    return calculateCvssV3Score(vector);
}

function normalizeVulnerabilitySeverity(severity) {
    const entries = Array.isArray(severity) ? severity : [];
    const entry = entries[0] ?? {};
    const rawScore = entry.score ?? "";
    const numericScore = Number(rawScore);
    const score = Number.isFinite(numericScore)
        ? numericScore
        : calculateCvssScore(entry.type, rawScore);
    const label = cvssSeverityLabel(score);
    return {
        type: entry.type ?? "",
        score,
        vector: rawScore,
        label,
        level: label.toLowerCase()
    };
}

function renderVulnerabilitySeverity(severity) {
    const label = severity?.label ?? "Unknown";
    const score = Number.isFinite(severity?.score) ? severity.score.toFixed(1) : "";
    const title = [severity?.type, severity?.vector].filter(Boolean).join(" ");
    return `
        <span class="cas-vulnerability-severity severity-${escapePalantirMarkup(severity?.level ?? "unknown")}"
              title="${escapePalantirMarkup(title)}">
            ${escapePalantirMarkup(label)}${score ? ` ${escapePalantirMarkup(score)}` : ""}
        </span>`;
}

function renderVulnerabilitySeverityBreakdown(vulnerabilities) {
    const levels = ["critical", "high", "medium", "low", "unknown"];
    const labels = {
        critical: "Critical",
        high: "High",
        medium: "Medium",
        low: "Low",
        unknown: "Unknown"
    };
    const counts = Object.fromEntries(levels.map(level => [level, 0]));
    vulnerabilities.forEach(vulnerability => {
        const level = vulnerability.severity?.level;
        counts[levels.includes(level) && level !== "none" ? level : "unknown"]++;
    });
    const maxCount = Math.max(...levels.map(level => counts[level]), 1);
    const rows = levels.map(level => {
        const count = counts[level];
        const width = count > 0 ? Math.max(2, (count / maxCount) * 100) : 0;
        return `
            <div class="cas-vulnerability-breakdown-row">
                <span class="cas-vulnerability-breakdown-label severity-${level}">${labels[level]}</span>
                <span class="cas-vulnerability-breakdown-track">
                    <span class="cas-vulnerability-breakdown-bar severity-${level}" style="width:${width}%"></span>
                </span>
                <span class="cas-vulnerability-breakdown-count">${count}</span>
            </div>`;
    }).join("");
    $("#casVulnerabilitiesSeverityBreakdown").html(`
        <div class="cas-vulnerability-breakdown-title">Severity breakdown</div>
        <div class="cas-vulnerability-breakdown-body">${rows}</div>`);
    showElements($("#casVulnerabilitiesSeverityBreakdown"));
}

function formatDependencyTimestamp(value) {
    if (!value) {
        return "";
    }
    const date = new Date(value);
    return Number.isNaN(date.getTime()) ? String(value) : date.toLocaleString();
}

function dependenciesVulnerabilitiesEndpoint() {
    const endpoint = CasActuatorEndpoints.dependencies();
    return endpoint ? `${endpoint.replace(/\/$/, "")}/vulnerabilities` : undefined;
}

function setSystemJqueryTabAvailable(anchorSelector, panelSelector, available) {
    const tabItem = $(anchorSelector).parent();
    const panel = $(panelSelector);
    const tabs = tabItem.closest(".jqueryui-tabs");
    if (available) {
        showElements(tabItem);
        showElements(panel);
        showElements(tabs);
    } else {
        const tabIndex = tabItem.index();
        hideElements(tabItem);
        hideElements(panel);
        if (tabs.data("ui-tabs")) {
            tabs.tabs("refresh");
            const firstShown = tabs.find("> ul > li:not(.hide):not(.d-none)").first().index();
            if (tabs.tabs("option", "active") === tabIndex && firstShown >= 0) {
                tabs.tabs("option", "active", firstShown);
            }
        }
        if (tabs.find("> ul > li:not(.hide):not(.d-none)").length === 0) {
            hideElements(tabs);
        }
    }
}

function hideDependenciesTab() {
    setSystemJqueryTabAvailable("#casdependenciestab", "#casdependencies-tab", false);
}

function hideVulnerabilitiesTab() {
    setSystemJqueryTabAvailable("#casvulnerabilitiestab", "#casvulnerabilities-tab", false);
}
