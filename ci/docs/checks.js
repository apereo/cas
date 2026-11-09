#!/usr/bin/env node
/*
 * Checks the documentation sources and the rendered pages of a version for problems that links alone do not show.
 * Usage: node ci/docs/checks.js <documentation sources> [rendered version directory] [configuration catalog index.json]
 * The rendered pages and the catalog are optional; the checks that need them are skipped when they are missing.
 * Exits with 1 when any check fails. Within GitHub Actions, failures are also reported as annotations.
 */
"use strict";

const fs = require("node:fs");
const path = require("node:path");
const yaml = require("js-yaml");
const {parseHtml, decodeEntities, findFirst} = require("./markdown.js");

const SETTING_NAMES_ALLOWED = [/^release_notes\//, /^planning\/Quick-Start\.md$/];
const SETTING_FENCES = /^(properties|ini|conf|bash|shell|sh|zsh|console|text|plaintext)?$/;
const SETTING_TOKEN = /[a-z][a-z0-9-]*(?:\[[^\]\s]*])?(?:\.[a-z0-9][a-z0-9-]*(?:\[[^\]\s]*])?)+/gi;

const failures = [];

function fail(check, file, line, message) {
    failures.push({check, file, line, message});
}

function markdownFiles(directory, root = directory, results = []) {
    for (const entry of fs.readdirSync(directory, {withFileTypes: true})) {
        if (entry.name.startsWith("_") && entry.name !== "_includes" || ["build", "node_modules", "developer"].includes(entry.name)) {
            continue;
        }
        const file = path.join(directory, entry.name);
        if (entry.isDirectory()) {
            markdownFiles(file, root, results);
        } else if (entry.name.endsWith(".md")) {
            results.push(path.relative(root, file).split(path.sep).join("/"));
        }
    }
    return results;
}

function frontMatter(source) {
    const match = source.match(/^---\r?\n([\s\S]*?)\r?\n---/);
    if (!match) {
        return null;
    }
    try {
        return yaml.load(match[1]) || {};
    } catch (error) {
        return {};
    }
}

function settingPath(name) {
    return name.toLowerCase().replace(/\[[^\]]*]/g, "").split(/[._]/).filter(segment => segment && !/^\d+$/.test(segment))
        .map(segment => segment.replace(/[^a-z0-9]/g, "")).filter(Boolean);
}

function settingFlat(name) {
    return settingPath(name).join("");
}

function settingIndex(catalogFile) {
    const data = JSON.parse(fs.readFileSync(catalogFile, "utf8"));
    const docs = Array.isArray(data) ? data : data.docs || [];
    const exact = new Set();
    const partial = new Set();
    for (const setting of docs) {
        const segments = settingPath(setting.name);
        exact.add(segments.join(""));
        for (let index = 1; index < segments.length; index++) {
            partial.add(segments.slice(0, index).join("."));
            partial.add(segments.slice(index).join("."));
        }
    }
    return {exact, partial};
}

function lines(source) {
    return source.split(/\r?\n/);
}

function fencedBlocks(source) {
    const blocks = [];
    let open = null;
    lines(source).forEach((text, index) => {
        const fence = text.match(/^(\s*)(`{3,}|~{3,})\s*([\w+-]*)/);
        if (open) {
            if (fence && fence[2][0] === open.marker[0] && fence[2].length >= open.marker.length && !fence[3]) {
                blocks.push({...open, end: index});
                open = null;
            } else {
                open.body.push(text.startsWith(open.indent) ? text.slice(open.indent.length) : text.trimStart());
            }
        } else if (fence) {
            open = {indent: fence[1], marker: fence[2], language: fence[3].toLowerCase(), start: index, body: []};
        }
    });
    return blocks;
}

function checkTitles(sources, documents) {
    const seen = new Map();
    for (const file of documents) {
        if (file.startsWith("_includes/")) {
            continue;
        }
        const data = frontMatter(fs.readFileSync(path.join(sources, file), "utf8"));
        if (!data || !data.layout) {
            continue;
        }
        const title = String(data.title || "").trim();
        if (!title) {
            fail("titles", file, 1, "The page has no title.");
        } else if (seen.has(title)) {
            fail("titles", file, 1, `The title "${title}" is also used by ${seen.get(title)}.`);
        } else {
            seen.set(title, file);
        }
    }
}

function relaxedJson(body) {
    return body
        .replace(/'''[\s\S]*?'''/g, "\"\"")
        .replace(/"(?:[^"\\\n]|\\.)*"|\/\*[\s\S]*?\*\/|\/\/[^\n]*|#[^\n]*/g, match => (match.startsWith("\"") ? match : ""))
        .replace(/,(\s*[}\]])/g, "$1");
}

function checkJson(sources, documents) {
    for (const file of documents) {
        const source = fs.readFileSync(path.join(sources, file), "utf8");
        for (const block of fencedBlocks(source)) {
            if (block.language !== "json") {
                continue;
            }
            const body = block.body.join("\n");
            if (/\{%|\{\{/.test(body) || !body.trim()) {
                continue;
            }
            const relaxed = relaxedJson(body);
            if (/\.\.\./.test(relaxed)) {
                continue;
            }
            try {
                JSON.parse(relaxed);
            } catch (error) {
                fail("json", file, block.start + 1, `The JSON block does not parse: ${error.message.replace(/\s+/g, " ")}`);
                continue;
            }
            block.body.forEach((text, index) => {
                const code = relaxedJson(text);
                if (/\{[^{}]*"\s*:[^{}]*,[^{}]*"\s*:[^{}]*}/.test(code) || code.trim().length > 140 && /[{\[]/.test(code)) {
                    fail("json", file, block.start + index + 2, "The JSON block is not pretty-printed; put each member of an object on its own line.");
                }
            });
        }
    }
}

function checkSettingNames(sources, documents, index) {
    for (const file of documents) {
        if (SETTING_NAMES_ALLOWED.some(pattern => pattern.test(file))) {
            continue;
        }
        const source = fs.readFileSync(path.join(sources, file), "utf8");
        const skipped = new Set();
        for (const block of fencedBlocks(source)) {
            if (!SETTING_FENCES.test(block.language)) {
                for (let line = block.start; line <= block.end; line++) {
                    skipped.add(line);
                }
            }
        }
        const prose = source
            .replace(/^---\r?\n[\s\S]*?\r?\n---/, match => match.replace(/[^\n]/g, " "))
            .replace(/\{%[\s\S]*?%}|\{\{[\s\S]*?}}|<button\b[\s\S]*?<\/button>|<\/?[a-zA-Z][^<>]*>|\bhttps?:\/\/\S+/g,
                match => match.replace(/[^\n]/g, " "));
        lines(prose).forEach((text, line) => {
            if (skipped.has(line)) {
                return;
            }
            for (const match of text.matchAll(SETTING_TOKEN)) {
                if (index.exact.has(settingFlat(match[0]))) {
                    fail("setting-names", file, line + 1,
                        `The setting ${match[0]} is named; describe the behavior instead and let the page's settings list name it.`);
                }
            }
        });
    }
}

function checkReleaseNoteSettings(sources, documents, index) {
    const known = name => {
        const key = settingPath(name).join(".");
        return key && (index.exact.has(settingFlat(name)) || index.partial.has(key));
    };
    for (const file of documents.filter(document => document.startsWith("release_notes/"))) {
        const source = fs.readFileSync(path.join(sources, file), "utf8");
        const lineOf = offset => source.slice(0, offset).split("\n").length;
        for (const match of source.matchAll(/`([^`\n]+)`\{:\s*\.cas-setting\s*}/g)) {
            const name = match[1].split(/[=:]/)[0].trim();
            if (!known(name)) {
                fail("release-note-settings", file, lineOf(match.index), `${name} is not in the configuration catalog.`);
            }
        }
        for (const match of source.matchAll(/(`{3,})[^\n]*\n([\s\S]*?)\n\1[ \t]*\n\{:\s*\.cas-settings-linked\s*}/g)) {
            match[2].split("\n").forEach((text, offset) => {
                const name = text.trim().split(/[=:]/)[0].trim();
                if (name && !name.startsWith("#") && !known(name)) {
                    fail("release-note-settings", file, lineOf(match.index) + offset + 1, `${name} is not in the configuration catalog.`);
                }
            });
        }
    }
}

function checkSidebarGroups(sources) {
    const sidebar = fs.readFileSync(path.join(sources, "sidebar.md"), "utf8");
    const script = fs.readFileSync(path.join(sources, "javascripts", "site.js"), "utf8");
    const groups = script.match(/const CAS_SIDEBAR_GROUPS = \[([\s\S]*?)\];/);
    const grouped = new Set(groups ? [...groups[1].matchAll(/"(#[^"]+)"/g)].map(match => match[1]) : []);
    lines(sidebar).forEach((text, line) => {
        const section = text.match(/^\* \[([^\]]+)]\((#[^)]+)\)/);
        if (section && !grouped.has(section[2])) {
            fail("sidebar-groups", "sidebar.md", line + 1, `The section ${section[1]} (${section[2]}) is not in any CAS_SIDEBAR_GROUPS group in javascripts/site.js.`);
        }
    });
}

function renderedPages(directory, results = []) {
    for (const entry of fs.readdirSync(directory, {withFileTypes: true})) {
        const file = path.join(directory, entry.name);
        if (entry.isDirectory()) {
            renderedPages(file, results);
        } else if (entry.name.endsWith(".html")) {
            results.push(file);
        }
    }
    return results;
}

function checkOpeningParagraphs(sources, rendered) {
    const config = yaml.load(fs.readFileSync(path.join(sources, "_config.yml"), "utf8")) || {};
    const siteDescription = String(config.description || "");
    for (const file of renderedPages(rendered)) {
        const html = fs.readFileSync(file, "utf8");
        if (!html.includes("id=\"cas-docs-container\"")) {
            continue;
        }
        const root = parseHtml(html.slice(0, html.indexOf("</head>") + 7));
        const title = findFirst(root, node => node.tag === "title");
        const meta = findFirst(root, node => node.tag === "meta" && node.attrs.name === "description");
        if (!title || !meta) {
            continue;
        }
        const name = decodeEntities(title.children.map(child => child.text || "").join(""))
            .replace(/\s*·\s*Apereo CAS Documentation\s*$/, "").replace(/^CAS - /, "").trim();
        const fallback = `${name}. ${siteDescription}`.slice(0, 60);
        if (meta.attrs.content.startsWith(fallback)) {
            const relative = path.relative(rendered, file).split(path.sep).join("/").replace(/\.html$/, ".md");
            fail("opening-paragraph", relative, 1, "The page has no description and no opening paragraph that can serve as one; " +
                "start it with a sentence that says what the page covers, or add a description to its front matter.");
        }
    }
}

function report() {
    const annotate = process.env.CI === "true";
    for (const failure of failures) {
        const location = `docs/cas-server-documentation/${failure.file}`;
        if (annotate) {
            console.log(`::error file=${location},line=${failure.line},title=Docs check: ${failure.check}::${failure.message}`);
        } else {
            console.log(`${location}:${failure.line}: [${failure.check}] ${failure.message}`);
        }
    }
    const counts = failures.reduce((all, failure) => ({...all, [failure.check]: (all[failure.check] || 0) + 1}), {});
    console.log(failures.length
        ? `Documentation checks failed: ${Object.entries(counts).map(([check, count]) => `${check} ${count}`).join(", ")}.`
        : "Documentation checks passed.");
}

function main() {
    const [sources, rendered, catalog] = process.argv.slice(2);
    if (!sources) {
        console.error("Usage: node ci/docs/checks.js <documentation sources> [rendered version directory] [configuration catalog index.json]");
        process.exit(2);
    }
    const documents = markdownFiles(sources);
    checkTitles(sources, documents);
    checkJson(sources, documents);
    checkSidebarGroups(sources);
    if (catalog && fs.existsSync(catalog)) {
        const index = settingIndex(catalog);
        checkSettingNames(sources, documents, index);
        checkReleaseNoteSettings(sources, documents, index);
    } else {
        console.log("No configuration catalog; skipping the setting name checks.");
    }
    if (rendered && fs.existsSync(rendered)) {
        checkOpeningParagraphs(sources, rendered);
    } else {
        console.log("No rendered pages; skipping the opening paragraph check.");
    }
    report();
    process.exit(failures.length ? 1 : 0);
}

if (require.main === module) {
    main();
}
