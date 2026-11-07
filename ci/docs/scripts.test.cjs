// Usage: node --test ci/docs/scripts.test.cjs (after npm --prefix ci/docs install)
const test = require("node:test");
const assert = require("node:assert/strict");
const {parseHtml, pageMarkdown, sidebarSections} = require("./markdown.js");
const {compare, settingKey, sameDefault} = require("./settings-changes.js");

const page = body => `<!DOCTYPE html><html><head><title>CAS - Sample Page · Apereo CAS Documentation</title>
<meta name="description" content="A sample &amp; its description."></head><body><nav>Skip</nav>
<article id="cas-docs-container" tabindex="-1">${body}</article><footer>Footer</footer></body></html>`;

test("parses unclosed list items and paragraphs", () => {
    const root = parseHtml("<ul><li>one<li>two</ul><p>first<p>second");
    const list = root.children[0];
    assert.equal(list.children.filter(child => child.tag === "li").length, 2);
    assert.equal(root.children.filter(child => child.tag === "p").length, 2);
});

test("converts headings, links, code, tables and alerts", () => {
    const html = page(`<h1 id="x">Sample Page</h1><p>See <a href="../other/Page.html">the page</a> and <code>a|b</code>.</p>
<div class="alert alert-info"><strong>Usage</strong><p>Keep it short.</p></div>
<table><thead><tr><th>Name</th><th>Value</th></tr></thead><tbody><tr><td><code>x|y</code></td><td>1</td></tr></tbody></table>
<div class="language-json highlighter-rouge"><div class="highlight"><pre class="highlight"><code><table class="rouge-table"><tbody><tr>
<td class="rouge-gutter gl"><pre class="lineno">1
</pre></td><td class="rouge-code"><pre><span class="p">{</span> &quot;a&quot;: 1 <span class="p">}</span>
</pre></td></tr></tbody></table></code></pre></div></div>`);
    const result = pageMarkdown(html, "https://apereo.github.io/cas/development/planning/Sample.html");
    assert.equal(result.title, "Sample Page");
    assert.equal(result.description, "A sample & its description.");
    const markdown = result.markdown;
    assert.match(markdown, /^# Sample Page\n\nSource: https:\/\/apereo\.github\.io\/cas\/development\/planning\/Sample\.html\n/);
    assert.match(markdown, /\[the page]\(https:\/\/apereo\.github\.io\/cas\/development\/other\/Page\.html\)/);
    assert.match(markdown, /> \*\*Usage\*\*\n>\n> Keep it short\./);
    assert.match(markdown, /\| `x\\\|y` \| 1 \|/);
    assert.match(markdown, /```json\n\{ "a": 1 \}\n```/);
    assert.doesNotMatch(markdown, /Skip|Footer|lineno/);
});

test("lists settings with their kind, type and default", () => {
    const html = page(`<div class="cas-properties"><div class="cas-properties-list">
<div class="cas-property" data-kind="required" data-name="cas.server.name" data-default="https://cas.example.org:8443">
<div class="cas-property-head"><div class="cas-property-key"><code class="cas-property-name">cas.server.name</code></div></div>
<div class="cas-property-detail"><div class="cas-property-info"><div class="cas-property-description"><p>The CAS server name.</p></div>
<dl class="cas-property-meta"><dt>Type</dt><dd><code>String</code></dd><dt>Default</dt><dd><code>https://cas.example.org:8443</code></dd></dl>
</div></div></div></div><p class="cas-properties-legend">Legend</p></div>`);
    const markdown = pageMarkdown(html, "https://apereo.github.io/cas/development/Sample.html").markdown;
    assert.match(markdown, /- `cas\.server\.name` \(Required; type `String`; default `https:\/\/cas\.example\.org:8443`\): The CAS server name\./);
    assert.doesNotMatch(markdown, /Legend/);
});

test("labels tab panes and keeps the active module pane only", () => {
    const html = page(`<ul class="tab" data-name="x"><li class="active"><a href="#">Groovy</a></li><li><a href="#">REST</a></li></ul>
<ul class="tab-content" data-name="x"><li><p>Script</p></li><li><p>Endpoint</p></li></ul>
<div class="cas-module-tabs"><div class="tab-pane fade" id="maven"><p>Maven</p></div><div class="tab-pane fade show active" id="gradle"><p>Gradle</p></div></div>`);
    const markdown = pageMarkdown(html, "https://apereo.github.io/cas/development/Sample.html").markdown;
    assert.match(markdown, /\*\*Groovy\*\*\n\nScript\n\n\*\*REST\*\*\n\nEndpoint/);
    assert.match(markdown, /Gradle/);
    assert.doesNotMatch(markdown, /Maven/);
});

test("reads sidebar sections and names nested entries after their parents", () => {
    const sections = sidebarSections(`<ul><li><a href="#planning">Planning</a><ul>
<li><a href="/cas/development/planning/Architecture.html">Architecture</a></li>
<li><a href="#war">WAR Overlays</a><ul><li><a href="/cas/development/installation/WAR.html">Overview</a></li>
<li><a href="/cas/development/installation/BOM.html">Bill of Materials</a></li></ul></li></ul></li></ul>`, "development");
    assert.deepEqual(sections, [{title: "Planning", links: [
        {name: "Architecture", href: "/cas/development/planning/Architecture.html"},
        {name: "WAR Overlays", href: "/cas/development/installation/WAR.html"},
        {name: "WAR Overlays: Bill of Materials", href: "/cas/development/installation/BOM.html"}]}]);
});

test("compares catalogs while ignoring collection notation and equal durations", () => {
    const setting = (name, defaultValue, extra = {}) => ({name, type: "String", defaultValue, kind: "optional", description: "Text. More.", ...extra});
    const baseline = new Map([
        ["cas.a.list.name", setting("cas.a.list.name", "")],
        ["cas.a.timeout", setting("cas.a.timeout", "5000")],
        ["cas.a.size", setting("cas.a.size", "10")],
        ["cas.a.gone", setting("cas.a.gone", "")]
    ]);
    const current = new Map([
        [settingKey("cas.a.list[0].name"), setting("cas.a.list[0].name", "")],
        ["cas.a.timeout", setting("cas.a.timeout", "PT5S")],
        ["cas.a.size", setting("cas.a.size", "100")],
        ["cas.a.old", setting("cas.a.old", "", {deprecated: "warning", replacement: "cas.a.new"})],
        ["cas.a.new", setting("cas.a.new", "")]
    ]);
    const result = compare(baseline, current);
    assert.deepEqual(result.added.map(entry => entry.name), ["cas.a.new", "cas.a.old"]);
    assert.equal(result.added[0].summary, "Text.");
    assert.deepEqual(result.removed.map(entry => entry.name), ["cas.a.gone"]);
    assert.deepEqual(result.replaced.map(entry => [entry.name, entry.replacement]), [["cas.a.old", "cas.a.new"]]);
    assert.deepEqual(result.defaults.map(entry => [entry.name, entry.before, entry.after]), [["cas.a.size", "10", "100"]]);
    assert.ok(sameDefault("10000", "PT10S"));
    assert.ok(!sameDefault("PT30S", "PT5M"));
});
