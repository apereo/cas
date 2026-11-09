#!/usr/bin/env node
/*
 * Writes a Markdown copy next to every rendered documentation page, and an llms.txt index for the version.
 * Usage: node ci/docs/markdown.js <rendered site directory> <version> [site base URL]
 * The site directory holds one folder per version (for example gh-pages/_site), and the base URL defaults to
 * https://apereo.github.io/cas. Pages are read as Jekyll rendered them; no dependency beyond Node is needed.
 */
"use strict";

const fs = require("node:fs");
const path = require("node:path");

const VOID_TAGS = new Set(["area", "base", "br", "col", "embed", "hr", "img", "input", "link", "meta", "source", "track", "wbr"]);
const RAW_TAGS = new Set(["script", "style", "textarea"]);
const SKIPPED_TAGS = new Set(["script", "style", "noscript", "template", "button", "svg", "input", "select", "textarea", "form", "nav"]);
const INLINE_TAGS = new Set(["a", "abbr", "b", "br", "cite", "code", "del", "em", "i", "img", "kbd", "label", "mark", "q", "s",
    "samp", "small", "span", "strong", "sub", "sup", "time", "tt", "u", "var"]);
const BLOCK_TAGS = new Set(["address", "article", "aside", "blockquote", "details", "dd", "div", "dl", "dt", "figure", "figcaption",
    "footer", "h1", "h2", "h3", "h4", "h5", "h6", "header", "hr", "li", "main", "ol", "p", "pre", "section", "table", "ul"]);
const ENTITIES = {
    amp: "&", lt: "<", gt: ">", quot: "\"", apos: "'", nbsp: " ", hellip: "…", ndash: "–", mdash: "—", lsquo: "‘",
    rsquo: "’", ldquo: "“", rdquo: "”", copy: "©", reg: "®", trade: "™", rarr: "→", larr: "←", harr: "↔", uarr: "↑",
    darr: "↓", times: "×", middot: "·", bull: "•", laquo: "«", raquo: "»", deg: "°", sect: "§", para: "¶", shy: ""
};
const ALERT_LABELS = {"alert-warning": "Warning", "alert-danger": "Warning", "alert-success": "Tip", "alert-info": "Note"};

function decodeEntities(text) {
    return text.replace(/&(#[xX][0-9a-fA-F]+|#\d+|[a-zA-Z]+);/g, (match, code) => {
        if (code.startsWith("#")) {
            const value = /^#[xX]/.test(code) ? parseInt(code.slice(2), 16) : parseInt(code.slice(1), 10);
            return Number.isFinite(value) && value > 0 && value <= 0x10FFFF ? String.fromCodePoint(value) : match;
        }
        return Object.hasOwn(ENTITIES, code) ? ENTITIES[code] : match;
    });
}

function parseAttributes(source) {
    const attributes = {};
    for (const match of source.matchAll(/([^\s"'>\/=]+)(?:\s*=\s*(?:"([^"]*)"|'([^']*)'|([^\s"'=<>`]+)))?/g)) {
        attributes[match[1].toLowerCase()] = decodeEntities(match[2] ?? match[3] ?? match[4] ?? "");
    }
    return attributes;
}

function closeTo(current, predicate, stopTags) {
    for (let node = current; node && node.tag !== "#root"; node = node.parent) {
        if (predicate(node)) {
            return node.parent;
        }
        if (stopTags.has(node.tag)) {
            return current;
        }
    }
    return current;
}

function implicitlyClose(current, tag) {
    if (current.tag === "p" && BLOCK_TAGS.has(tag)) {
        current = current.parent;
    }
    if (tag === "li") {
        return closeTo(current, node => node.tag === "li", new Set(["ul", "ol"]));
    }
    if (tag === "dt" || tag === "dd") {
        return closeTo(current, node => node.tag === "dt" || node.tag === "dd", new Set(["dl"]));
    }
    if (tag === "td" || tag === "th") {
        return closeTo(current, node => node.tag === "td" || node.tag === "th", new Set(["tr", "table"]));
    }
    if (tag === "tr") {
        return closeTo(current, node => node.tag === "tr", new Set(["table", "thead", "tbody", "tfoot"]));
    }
    return current;
}

function parseHtml(html) {
    const root = {tag: "#root", attrs: {}, children: [], parent: null};
    const tokens = /<!--[\s\S]*?-->|<![^>]*>|<\/([a-zA-Z][\w:-]*)\s*>|<([a-zA-Z][\w:-]*)((?:\s+[^\s"'>\/=]+(?:\s*=\s*(?:"[^"]*"|'[^']*'|[^\s"'=<>`]+))?)*)\s*(\/?)>/g;
    let current = root;
    let last = 0;
    let match;
    while ((match = tokens.exec(html)) !== null) {
        if (match.index > last) {
            current.children.push({text: decodeEntities(html.slice(last, match.index))});
        }
        last = tokens.lastIndex;
        if (match[1]) {
            const tag = match[1].toLowerCase();
            current = closeTo(current, node => node.tag === tag, new Set());
            continue;
        }
        if (!match[2]) {
            continue;
        }
        const tag = match[2].toLowerCase();
        current = implicitlyClose(current, tag);
        const element = {tag, attrs: parseAttributes(match[3] || ""), children: [], parent: current};
        current.children.push(element);
        if (RAW_TAGS.has(tag)) {
            const end = html.toLowerCase().indexOf(`</${tag}`, last);
            const stop = end < 0 ? html.length : end;
            element.children.push({text: html.slice(last, stop)});
            const close = html.indexOf(">", stop);
            last = close < 0 ? html.length : close + 1;
            tokens.lastIndex = last;
        } else if (!VOID_TAGS.has(tag) && !match[4]) {
            current = element;
        }
    }
    if (last < html.length) {
        current.children.push({text: decodeEntities(html.slice(last))});
    }
    return root;
}

function classesOf(node) {
    return new Set((node.attrs?.class || "").split(/\s+/).filter(Boolean));
}

function hasClass(node, name) {
    return classesOf(node).has(name);
}

function elements(node) {
    return (node.children || []).filter(child => child.tag);
}

function findFirst(node, predicate) {
    for (const child of elements(node)) {
        if (predicate(child)) {
            return child;
        }
        const found = findFirst(child, predicate);
        if (found) {
            return found;
        }
    }
    return null;
}

function findAll(node, predicate, results = []) {
    for (const child of elements(node)) {
        if (predicate(child)) {
            results.push(child);
        } else {
            findAll(child, predicate, results);
        }
    }
    return results;
}

function textOf(node) {
    if (node.text !== undefined) {
        return node.text;
    }
    return (node.children || []).map(textOf).join("");
}

function isSkipped(node) {
    if (SKIPPED_TAGS.has(node.tag) || node.attrs["aria-hidden"] === "true") {
        return true;
    }
    if (/display:\s*none/.test(node.attrs.style || "")) {
        return true;
    }
    const classes = classesOf(node);
    if (node.tag === "i" && [...classes].some(name => name === "fa" || name.startsWith("fa-") || /^fa[bsr]$/.test(name))) {
        return true;
    }
    if (classesOf(node).has("cas-actuator-snippet") && node.attrs["data-format"] !== "properties") {
        return true;
    }
    if (node.tag === "details" && /^\s*Notes on configuration\s*$/.test(textOf(elements(node).find(child => child.tag === "summary") || {children: []}))) {
        return true;
    }
    return ["visually-hidden", "cas-module-controls", "dropdown-menu", "rouge-gutter", "cas-properties-legend", "anchorjs-link", "emoji",
        "cas-actuator-step-number", "cas-actuator-actions", "cas-actuator-tabs", "cas-actuator-choice", "cas-actuator-format"]
        .some(name => classes.has(name));
}

function containsBlock(node) {
    return elements(node).some(child => !isSkipped(child) && (BLOCK_TAGS.has(child.tag) || containsBlock(child)));
}

function isInline(node) {
    return INLINE_TAGS.has(node.tag) && !containsBlock(node);
}

function codeSpan(text) {
    const value = text.replace(/\s*\n\s*/g, " ");
    if (!value.trim()) {
        return "";
    }
    const longest = Math.max(0, ...(value.match(/`+/g) || []).map(run => run.length));
    const fence = "`".repeat(longest + 1);
    const padding = value.startsWith("`") || value.endsWith("`") ? " " : "";
    return `${fence}${padding}${value}${padding}${fence}`;
}

function codeFence(text, language) {
    const body = text.replace(/^\n+|\s+$/g, "");
    const longest = Math.max(2, ...(body.match(/^`{3,}/gm) || []).map(run => run.length));
    const fence = "`".repeat(longest + 1);
    return `${fence}${language || ""}\n${body}\n${fence}`;
}

function wrapInline(marker, content) {
    const trimmed = content.trim();
    if (!trimmed) {
        return content;
    }
    const leading = content.match(/^\s*/)[0];
    const trailing = content.match(/\s*$/)[0];
    return `${leading}${marker}${trimmed}${marker}${trailing}`;
}

function indent(text, prefix) {
    return text.split("\n").map(line => (line ? prefix + line : line)).join("\n");
}

class MarkdownConverter {
    constructor(pageUrl) {
        this.pageUrl = pageUrl;
    }

    resolve(href) {
        if (!href || href.startsWith("javascript:")) {
            return "";
        }
        try {
            return new URL(href, this.pageUrl).href;
        } catch (error) {
            return href;
        }
    }

    inline(node) {
        if (node.text !== undefined) {
            return node.text.replace(/\s+/g, " ").replace(/<(?=[a-zA-Z\/!])/g, "\\<");
        }
        if (isSkipped(node)) {
            return "";
        }
        const content = () => node.children.map(child => this.inline(child)).join("");
        switch (node.tag) {
            case "br":
                return "\n";
            case "code":
            case "kbd":
            case "tt":
            case "samp":
                return codeSpan(textOf(node));
            case "strong":
            case "b":
                return wrapInline("**", content());
            case "em":
            case "i":
            case "cite":
                return wrapInline("*", content());
            case "del":
            case "s":
                return wrapInline("~~", content());
            case "img":
                return this.image(node);
            case "a": {
                const label = content().trim();
                const target = this.resolve(node.attrs.href);
                if (!label) {
                    return "";
                }
                return target ? `[${label.replace(/\n/g, " ")}](${target.replace(/[()\s]/g, encodeURIComponent)})` : label;
            }
            default: {
                const text = content();
                return ["span", "label", "small"].includes(node.tag) && node.attrs.class && /\S$/.test(text) ? `${text} ` : text;
            }
        }
    }

    image(node) {
        const source = this.resolve(node.attrs.src);
        return source ? `![${(node.attrs.alt || "").replace(/[\[\]]/g, "")}](${source})` : "";
    }

    paragraph(text) {
        return text.split("\n").map(line => line.replace(/(\*\*[^*\n]+\*\*)(?=[A-Z])/g, "$1 ").replace(/ {2,}/g, " ").trim())
            .join("\n").replace(/\n{2,}/g, "\n").trim();
    }

    blocks(node) {
        const output = [];
        let inline = "";
        const flush = () => {
            const text = this.paragraph(inline);
            if (text) {
                output.push(text);
            }
            inline = "";
        };
        for (const child of node.children || []) {
            if (child.text !== undefined) {
                inline += this.inline(child);
            } else if (isSkipped(child)) {
                continue;
            } else if (isInline(child)) {
                inline += this.inline(child);
            } else {
                flush();
                output.push(...this.block(child, node));
            }
        }
        flush();
        return output;
    }

    block(node, parent) {
        const classes = classesOf(node);
        if (/^h[1-6]$/.test(node.tag)) {
            const text = this.paragraph(this.inline(node)).replace(/\n/g, " ");
            return text ? [`${"#".repeat(Number(node.tag[1]))} ${text}`] : [];
        }
        if (classes.has("highlighter-rouge") || classes.has("highlight") && node.tag !== "pre") {
            return this.code(node);
        }
        if (classes.has("cas-properties-list")) {
            const settings = findAll(node, child => hasClass(child, "cas-property")).map(setting => this.setting(setting));
            return settings.length ? [settings.join("\n")] : [];
        }
        if (classes.has("alert")) {
            const label = Object.entries(ALERT_LABELS).find(([name]) => classes.has(name));
            const body = this.blocks(node);
            if (!body.length) {
                return [];
            }
            if (label && !/^\*\*/.test(body[0])) {
                body[0] = `**${label[1]}:** ${body[0]}`;
            }
            return [indent(body.join("\n\n"), "> ").replace(/^$/gm, ">")];
        }
        if (classes.has("tab-content") && node.tag === "ul") {
            return this.tabs(node, parent);
        }
        if (classes.has("tab") && node.tag === "ul") {
            return [];
        }
        if (classes.has("cas-actuator-chips") || classes.has("cas-op-meta")) {
            const chips = elements(node).map(chip => textOf(chip).replace(/\s+/g, " ").trim()).filter(Boolean);
            return chips.length ? [chips.join(" · ")] : [];
        }
        if (classes.has("cas-op")) {
            return this.operation(node);
        }
        if (classes.has("cas-actuator-panel") || classes.has("cas-actuator-choice-panel")) {
            return this.panel(node, parent);
        }
        if (classes.has("cas-settings")) {
            const version = (new URL(this.pageUrl).pathname.match(/^\/cas\/([^/]+)\//) || [])[1];
            return version ? [`The catalog is also published as JSON: ${new URL(`/cas/assets/data/${version}/index.json`, this.pageUrl).href}`] : [];
        }
        if (classes.has("cas-module-tabs")) {
            const panes = elements(node).filter(child => hasClass(child, "tab-pane"));
            const active = panes.find(pane => hasClass(pane, "active")) || panes[0];
            return active ? this.blocks(active) : [];
        }
        switch (node.tag) {
            case "p":
            case "figcaption":
            case "dt": {
                const text = this.paragraph(this.inline(node));
                return text ? [text] : [];
            }
            case "pre":
                return this.code(node);
            case "ul":
            case "ol":
                return this.list(node);
            case "blockquote": {
                const body = this.blocks(node);
                return body.length ? [indent(body.join("\n\n"), "> ").replace(/^$/gm, ">")] : [];
            }
            case "table":
                return this.table(node);
            case "hr":
                return ["---"];
            case "img":
                return [this.image(node)].filter(Boolean);
            case "details":
                return this.details(node);
            case "dl":
                return this.definitions(node);
            default:
                return this.blocks(node);
        }
    }

    code(node) {
        const container = findFirst(node, child => hasClass(child, "rouge-code")) || node;
        const pre = container.tag === "pre" ? container : findFirst(container, child => child.tag === "pre") || container;
        const language = [node, ...findAll(node, child => child.tag === "code")]
            .flatMap(element => [...classesOf(element)])
            .map(name => (name.match(/^language-([\w+#-]+)$/) || [])[1])
            .find(Boolean) || "";
        const text = textOf(pre);
        return text.trim() ? [codeFence(text, language === "plaintext" ? "" : language)] : [];
    }

    list(node) {
        const ordered = node.tag === "ol";
        let number = Number(node.attrs.start || 1);
        const items = elements(node).filter(child => child.tag === "li" && !isSkipped(child)).map(item => {
            const marker = ordered ? `${number++}. ` : "- ";
            const body = this.blocks(item);
            let text = "";
            body.forEach((part, index) => {
                const separator = index === 0 ? "" : /^(\s*)([-*+]|\d+\.) /.test(part) ? "\n" : "\n\n";
                text += separator + part;
            });
            return marker + indent(text, " ".repeat(marker.length)).slice(marker.length);
        }).filter(item => item.trim() !== "-" && !/^\d+\.\s*$/.test(item.trim()));
        return items.length ? [items.join("\n")] : [];
    }

    table(node) {
        const rows = findAll(node, child => child.tag === "tr").map(row =>
            elements(row).filter(cell => cell.tag === "td" || cell.tag === "th").map(cell =>
                this.blocks(cell).join(" ").replace(/\n+/g, " ").replace(/\|/g, "\\|").trim()));
        if (!rows.length) {
            return [];
        }
        const width = Math.max(...rows.map(row => row.length));
        const line = row => `| ${Array.from({length: width}, (unused, index) => row[index] || "").join(" | ")} |`;
        return [[line(rows[0]), line(Array(width).fill("---")), ...rows.slice(1).map(line)].join("\n")];
    }

    tabs(node, parent) {
        const siblings = elements(parent);
        const labels = siblings.slice(0, siblings.indexOf(node)).reverse().find(sibling => sibling.tag === "ul" && hasClass(sibling, "tab"));
        const names = labels ? elements(labels).filter(item => item.tag === "li").map(item => textOf(item).replace(/\s+/g, " ").trim()) : [];
        return elements(node).filter(item => item.tag === "li").flatMap((pane, index) => {
            const body = this.blocks(pane);
            return names[index] ? [`**${names[index]}**`, ...body] : body;
        });
    }

    operation(node) {
        const row = findFirst(node, child => hasClass(child, "cas-op-row"));
        const part = name => {
            const element = row && findFirst(row, child => hasClass(child, name));
            return element ? textOf(element).replace(/\s+/g, " ").trim() : "";
        };
        const meta = row && findFirst(row, child => hasClass(child, "cas-op-meta"));
        const chips = meta ? elements(meta).map(chip => textOf(chip).trim()).filter(Boolean).join(" · ") : "";
        const title = `**${part("cas-op-method")}** ${codeSpan(part("cas-op-path"))}${part("cas-op-summary") ? `: ${part("cas-op-summary")}` : ""}` +
            (chips ? ` (${chips})` : "");
        const detail = findFirst(node, child => hasClass(child, "cas-op-detail"));
        return [title, ...(detail ? this.blocks(detail) : [])];
    }

    panel(node, parent) {
        const tab = node.attrs.id && findFirst(this.root, child => child.attrs["aria-controls"] === node.attrs.id);
        const choice = node.attrs["data-choice"] && elements(parent)
            .filter(child => hasClass(child, "cas-actuator-choice") && child.attrs["data-group"] === node.attrs["data-group"])
            .map(group => findFirst(group, child => child.attrs["data-choice"] === node.attrs["data-choice"]))
            .find(Boolean);
        const label = tab || choice ? textOf(tab || choice).replace(/\s+/g, " ").trim() : "";
        const body = this.blocks(node);
        return label && body.length ? [tab ? `**${label}**` : `*${label}*`, ...body] : body;
    }

    details(node) {
        const summary = elements(node).find(child => child.tag === "summary");
        const rest = {children: node.children.filter(child => child !== summary)};
        const title = summary ? this.paragraph(this.inline(summary)) : "";
        return [...(title ? [`**${title}**`] : []), ...this.blocks(rest)];
    }

    definitions(node) {
        const lines = [];
        for (const child of elements(node)) {
            const text = this.blocks(child).join(" ").replace(/\n+/g, " ");
            if (child.tag === "dt") {
                lines.push(`- **${text}**`);
            } else if (child.tag === "dd" && lines.length) {
                lines[lines.length - 1] += `: ${text}`;
            }
        }
        return lines.length ? [lines.join("\n")] : [];
    }

    setting(node) {
        const name = node.attrs["data-name"] || textOf(findFirst(node, child => hasClass(child, "cas-property-name")) || {children: []});
        const facts = [{required: "Required", optional: "Optional", thirdparty: "Third party"}[node.attrs["data-kind"]]].filter(Boolean);
        const meta = findFirst(node, child => hasClass(child, "cas-property-meta"));
        let term = "";
        for (const child of meta ? elements(meta) : []) {
            if (child.tag === "dt") {
                term = textOf(child).trim();
            } else if (child.tag === "dd" && ["Type", "Deprecation"].includes(term)) {
                facts.push(`${term.toLowerCase()} ${this.paragraph(this.inline(child))}`);
            }
        }
        if (node.attrs["data-default"]) {
            facts.push(`default ${codeSpan(node.attrs["data-default"])}`);
        }
        const description = findFirst(node, child => hasClass(child, "cas-property-description"));
        const text = description ? this.blocks(description).join(" ").replace(/\s*\n\s*/g, " ") : "";
        return `- ${codeSpan(name)}${facts.length ? ` (${facts.join("; ")})` : ""}${text ? `: ${text}` : ""}`;
    }

    convert(node) {
        this.root = node;
        return this.blocks(node).join("\n\n").replace(/\n{3,}/g, "\n\n").trim();
    }
}

function metaContent(root, name) {
    const meta = findFirst(root, node => node.tag === "meta" && node.attrs.name === name);
    return meta ? meta.attrs.content.trim() : "";
}

function pageTitle(root) {
    const title = findFirst(root, node => node.tag === "title");
    return title ? textOf(title).replace(/\s*·\s*Apereo CAS Documentation\s*$/, "").replace(/^CAS - /, "").trim() : "";
}

function pageMarkdown(html, pageUrl) {
    const root = parseHtml(html);
    const article = findFirst(root, node => node.attrs.id === "cas-docs-container");
    if (!article) {
        return null;
    }
    const title = pageTitle(root);
    const body = new MarkdownConverter(pageUrl).convert(article);
    const source = `Source: ${pageUrl}`;
    const markdown = /^# /.test(body) ? body.replace(/^(# [^\n]*)/, `$1\n\n${source}`) : `# ${title}\n\n${source}\n\n${body}`;
    return {title, description: metaContent(root, "description"), markdown: `${markdown}\n`};
}

function htmlFiles(directory, results = []) {
    for (const entry of fs.readdirSync(directory, {withFileTypes: true})) {
        const file = path.join(directory, entry.name);
        if (entry.isDirectory()) {
            htmlFiles(file, results);
        } else if (entry.name.endsWith(".html")) {
            results.push(file);
        }
    }
    return results;
}

function sidebarSections(html, version) {
    const root = parseHtml(html);
    const menu = findFirst(root, node => node.tag === "ul");
    if (!menu) {
        return [];
    }
    const entries = (list, trail) => elements(list).filter(item => item.tag === "li").flatMap(item => {
        const link = elements(item).find(child => child.tag === "a");
        const nested = elements(item).find(child => child.tag === "ul");
        const label = link ? textOf(link).replace(/\s+/g, " ").trim() : "";
        const href = link ? link.attrs.href || "" : "";
        if (nested) {
            return entries(nested, [...trail, label]);
        }
        const name = label === "Overview" && trail.length ? trail.join(": ") : [...trail, label].join(": ");
        return href && !href.startsWith("#") ? [{name, href}] : [];
    });
    return elements(menu).filter(item => item.tag === "li").map(item => {
        const link = elements(item).find(child => child.tag === "a");
        const nested = elements(item).find(child => child.tag === "ul");
        const title = link ? textOf(link).replace(/\s+/g, " ").trim() : "";
        return {title, links: nested ? entries(nested, []) : entries({children: [item]}, [])};
    }).filter(section => section.title && section.links.length);
}

function llmsText(version, baseUrl, pages, sidebar, extras) {
    const pagePath = `/cas/${version}/`;
    const linked = new Set();
    const markdownUrl = href => {
        const target = new URL(href, `${baseUrl}/${version}/`);
        if (!target.pathname.startsWith(pagePath) || !target.pathname.endsWith(".html")) {
            return {url: target.href, page: null};
        }
        const page = pages.get(target.pathname);
        return page ? {url: `${new URL(target.pathname.replace(/\.html$/, ".md"), baseUrl).href}`, page} : {url: target.href, page: null};
    };
    const lines = [`# Apereo CAS documentation (${version})`, "",
        `> Apereo CAS is an enterprise identity provider and single sign-on server for the web, with support for CAS, SAML2, ` +
        `OpenID Connect, OAuth2, WS-Federation, multifactor authentication, delegated authentication and more. Each link below ` +
        `points to a Markdown copy of a page of the ${version} documentation; the same address ending in .html is the page itself.`,
        "", "Each feature page lists the settings it uses; the configuration catalog has all of them.", ""];
    extras.forEach(extra => lines.push(`- [${extra.name}](${extra.url}): ${extra.description}`));
    for (const section of sidebar) {
        const items = section.links.map(link => {
            const target = markdownUrl(link.href);
            if (target.page) {
                linked.add(target.page);
            }
            const description = target.page?.description ? `: ${target.page.description}` : "";
            return `- [${link.name}](${target.url})${description}`;
        });
        lines.push("", `## ${section.title}`, "", ...[...new Set(items)]);
    }
    const others = [...pages.values()].filter(page => !linked.has(page)).sort((a, b) => a.path.localeCompare(b.path));
    if (others.length) {
        lines.push("", "## Optional", "");
        others.forEach(page => lines.push(`- [${page.title}](${new URL(page.path.replace(/\.html$/, ".md"), baseUrl).href})` +
            `${page.description ? `: ${page.description}` : ""}`));
    }
    return `${lines.join("\n")}\n`;
}

function main() {
    const [siteDirectory, version, base] = process.argv.slice(2);
    if (!siteDirectory || !version) {
        console.error("Usage: node ci/docs/markdown.js <rendered site directory> <version> [site base URL]");
        process.exit(2);
    }
    const baseUrl = (base || "https://apereo.github.io/cas").replace(/\/+$/, "");
    const origin = new URL(baseUrl).origin;
    const versionDirectory = path.join(siteDirectory, version);
    const pages = new Map();
    let failures = 0;
    for (const file of htmlFiles(versionDirectory)) {
        const relative = path.relative(versionDirectory, file).split(path.sep).join("/");
        const pathname = `/cas/${version}/${relative}`;
        try {
            const page = pageMarkdown(fs.readFileSync(file, "utf8"), `${origin}${pathname}`);
            if (!page) {
                continue;
            }
            fs.writeFileSync(file.replace(/\.html$/, ".md"), page.markdown);
            pages.set(pathname, {path: pathname, title: page.title, description: page.description});
        } catch (error) {
            failures++;
            console.error(`Unable to write a Markdown copy of ${relative}: ${error.message}`);
        }
    }
    const sidebarFile = path.join(versionDirectory, "sidebar.html");
    const sidebar = fs.existsSync(sidebarFile) ? sidebarSections(fs.readFileSync(sidebarFile, "utf8"), version) : [];
    const extras = [{name: "Configuration catalog", url: `${baseUrl}/assets/data/${version}/index.json`,
        description: "every CAS and third-party setting with its type, default value, description and module, as JSON"}];
    const changes = [path.join(siteDirectory, "assets", "data", version, "settings-changes.json"),
        path.join(siteDirectory, "..", "assets", "data", version, "settings-changes.json")].find(file => fs.existsSync(file));
    if (changes) {
        const data = JSON.parse(fs.readFileSync(changes, "utf8"));
        if (data.baseline) {
            extras.push({name: "Settings changes", url: `${baseUrl}/assets/data/${version}/settings-changes.json`,
                description: `settings added, removed, replaced or given a new default since ${data.baseline}, as JSON`});
        }
    }
    fs.writeFileSync(path.join(versionDirectory, "llms.txt"), llmsText(version, baseUrl, pages, sidebar, extras));
    console.log(`Wrote ${pages.size} Markdown pages and llms.txt for ${version}.`);
    process.exit(failures ? 1 : 0);
}

if (require.main === module) {
    main();
}

module.exports = {parseHtml, decodeEntities, findFirst, pageMarkdown, sidebarSections, llmsText};
