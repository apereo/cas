#!/usr/bin/env node
/*
 * Compares the configuration catalog of a documentation version with the one of the version before it,
 * as listed in documentation_versions of _config.yml, and writes the added, removed, replaced settings and
 * the settings whose default changed.
 * Usage: node ci/docs/settings-changes.js <gh-pages directory> <version> <site data directory name>
 * Reads assets/data/<version>/index.json and the baseline's index.json, and writes
 * assets/data/<version>/settings-changes.json and _data/<site data directory name>/settings-changes.json.
 */
"use strict";

const fs = require("node:fs");
const path = require("node:path");
const yaml = require("js-yaml");

function settingKey(name) {
    return name.replace(/\[[^\]]*]/g, "").replace(/\.{2,}/g, ".").replace(/\.+$/, "");
}

function isCasSetting(setting) {
    return setting.kind !== "thirdparty" && setting.name.startsWith("cas.");
}

function settingArea(name) {
    const segments = settingKey(name).split(".");
    const depth = segments[1] === "authn" ? (["mfa", "oidc", "saml-idp", "pac4j", "attribute-repository"].includes(segments[2]) ? 4 : 3) : 2;
    return segments.slice(0, Math.min(depth, segments.length - 1)).join(".");
}

function summary(description) {
    const text = String(description || "")
        .replace(/\{@(?:code|link|linkplain|value)\s+([^}]*)}/g, "$1")
        .replace(/<[^>]+>/g, " ")
        .replace(/&lt;/g, "<").replace(/&gt;/g, ">").replace(/&quot;/g, "\"").replace(/&amp;/g, "&")
        .replace(/\s+/g, " ")
        .trim();
    const sentence = text.match(/^.*?[.!?](?=\s+[A-Z]|$)/);
    const first = sentence ? sentence[0] : text;
    return first.length > 240 ? `${first.slice(0, 237).trimEnd()}...` : first;
}

function durationMillis(value) {
    const text = String(value).trim();
    if (/^\d+$/.test(text)) {
        return Number(text);
    }
    const match = text.match(/^P(?:(\d+)D)?(?:T(?:(\d+)H)?(?:(\d+)M)?(?:(\d+(?:\.\d+)?)S)?)?$/i);
    if (!match || text.toUpperCase() === "P" || /T$/i.test(text)) {
        return null;
    }
    const [, days, hours, minutes, seconds] = match;
    return (((Number(days || 0) * 24 + Number(hours || 0)) * 60 + Number(minutes || 0)) * 60 + Number(seconds || 0)) * 1000;
}

function sameDefault(before, after) {
    if (before === after) {
        return true;
    }
    const left = durationMillis(before);
    const right = durationMillis(after);
    return left !== null && right !== null && left === right;
}

function catalog(file) {
    const data = JSON.parse(fs.readFileSync(file, "utf8"));
    const docs = Array.isArray(data) ? data : data.docs || [];
    const settings = new Map();
    docs.filter(isCasSetting).forEach(setting => {
        if (!settings.has(settingKey(setting.name))) {
            settings.set(settingKey(setting.name), setting);
        }
    });
    return settings;
}

function text(value) {
    return value === undefined || value === null ? "" : String(value);
}

function compare(baseline, current) {
    const replaced = [...current.values()]
        .filter(setting => setting.deprecated && setting.replacement && !baseline.get(settingKey(setting.name))?.deprecated)
        .map(setting => ({name: setting.name, replacement: setting.replacement, level: setting.deprecated,
            area: settingArea(setting.name)}));
    const added = [...current.entries()]
        .filter(([key]) => !baseline.has(key))
        .map(([, setting]) => ({name: setting.name, type: text(setting.type), defaultValue: text(setting.defaultValue),
            kind: text(setting.kind), module: text(setting.module), summary: summary(setting.description),
            area: settingArea(setting.name)}));
    const removed = [...baseline.entries()]
        .filter(([key]) => !current.has(key))
        .map(([, setting]) => ({name: setting.name, type: text(setting.type), area: settingArea(setting.name)}));
    const defaults = [...current.entries()]
        .map(([key, setting]) => [baseline.get(key), setting])
        .filter(([before, after]) => before && text(before.defaultValue) !== "" && text(after.defaultValue) !== ""
            && !sameDefault(text(before.defaultValue), text(after.defaultValue)))
        .map(([before, after]) => ({name: after.name, before: text(before.defaultValue), after: text(after.defaultValue),
            area: settingArea(after.name)}));
    const byName = (left, right) => left.name.localeCompare(right.name);
    return {added: added.sort(byName), removed: removed.sort(byName), replaced: replaced.sort(byName), defaults: defaults.sort(byName)};
}

function main() {
    const [pagesDirectory, version, dataDirectory] = process.argv.slice(2);
    if (!pagesDirectory || !version || !dataDirectory) {
        console.error("Usage: node ci/docs/settings-changes.js <gh-pages directory> <version> <site data directory name>");
        process.exit(2);
    }
    const config = yaml.load(fs.readFileSync(path.join(pagesDirectory, "_config.yml"), "utf8")) || {};
    const versions = (config.documentation_versions || []).map(String);
    const baselineVersion = versions[versions.indexOf(version) + 1];
    const currentFile = path.join(pagesDirectory, "assets", "data", version, "index.json");
    const baselineFile = baselineVersion && path.join(pagesDirectory, "assets", "data", baselineVersion, "index.json");
    let result = {version, baseline: null, added: [], removed: [], replaced: [], defaults: []};
    if (!versions.includes(version) || !baselineVersion) {
        console.log(`No earlier documentation version to compare ${version} with.`);
    } else if (!fs.existsSync(baselineFile)) {
        console.log(`The configuration catalog of ${baselineVersion} is not available at ${baselineFile}.`);
    } else {
        result = {version, baseline: baselineVersion, ...compare(catalog(baselineFile), catalog(currentFile))};
        console.log(`Settings changes since ${baselineVersion}: ${result.added.length} added, ${result.removed.length} removed, ` +
            `${result.replaced.length} replaced, ${result.defaults.length} new defaults.`);
    }
    const json = `${JSON.stringify(result, null, 2)}\n`;
    const outputs = [path.join(pagesDirectory, "assets", "data", version, "settings-changes.json"),
        path.join(pagesDirectory, "_data", dataDirectory, "settings-changes.json")];
    outputs.forEach(output => {
        fs.mkdirSync(path.dirname(output), {recursive: true});
        fs.writeFileSync(output, json);
    });
}

if (require.main === module) {
    main();
}

module.exports = {compare, settingKey, settingArea, summary, sameDefault};
