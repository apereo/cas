#!/usr/bin/env node

const fs = require('fs');
const yaml = require('js-yaml');

const jsonPath = process.argv[2];
const yamlPath = process.argv[3];
const outputPath = process.argv[4];
const combinedOutputPath = process.argv[5];
console.log(`JSON path: ${jsonPath}`);
console.log(`YAML path: ${yamlPath}`);
console.log(`Output path: ${outputPath}`);
if (combinedOutputPath) {
    console.log(`Combined metadata output path: ${combinedOutputPath}`);
}

let yamlProps = [];
let inputParsingFailed = false;
try {
    if (fs.existsSync(yamlPath)) {
        const rawYaml = fs.readFileSync(yamlPath, 'utf8');
        const parsed = yaml.load(rawYaml);
        yamlProps = Array.isArray(parsed) ? parsed : [];
        console.log(`Found ${yamlProps.length} YAML properties`);
    } else {
        console.log(`YAML path does not exist: ${yamlPath}`);
        inputParsingFailed = true;
    }
} catch(e) {
    console.log(`Error parsing YAML file: ${e}`);
    inputParsingFailed = true;
}

let jsonMetadata = {};
let jsonProps = [];
try {
    if (fs.existsSync(jsonPath)) {
        const raw = fs.readFileSync(jsonPath, 'utf8');
        jsonMetadata = JSON.parse(raw);
        jsonProps = Array.isArray(jsonMetadata.properties) ? jsonMetadata.properties : [];
        console.log(`Found ${jsonProps.length} JSON properties`);
    } else {
        console.log(`JSON path does not exist: ${jsonPath}`);
        inputParsingFailed = true;
    }
} catch(e) {
    console.log(`Error parsing JSON file: ${e}`);
    inputParsingFailed = true;
}

if (jsonProps.length === 0 && yamlProps.length === 0) {
    console.log(`No properties found, skipping search index generation`);
    process.exit(0);
}
const allProps = [...jsonProps, ...yamlProps];
console.log(`Found ${allProps.length} properties`);

const HINT_REQUIRED = 'org.apereo.cas.configuration.support.RequiredProperty';
const HINT_MODULE = 'org.apereo.cas.configuration.support.RequiresModule';
const HINT_DURATION = 'org.apereo.cas.configuration.support.DurationCapable';
const hintsByName = new Map((Array.isArray(jsonMetadata.hints) ? jsonMetadata.hints : [])
    .map(hint => [hint.name, Array.isArray(hint.values) ? hint.values : []]));

function hintModule(values) {
    const hint = values.find(value => value.description === HINT_MODULE);
    try {
        return hint ? JSON.parse(hint.value).module : undefined;
    } catch (e) {
        return undefined;
    }
}

function defaultText(value) {
    if (value === undefined || value === null) {
        return '';
    }
    return Array.isArray(value) ? value.join(',') : String(value);
}

function shortType(type) {
    return (type || '').replace(/\b[a-z][a-z0-9_]*\./g, '').replace(/\$/g, '.');
}

// Every setting once, CAS settings first; the page searches these directly.
const seen = new Set();
const docs = [];
const addDoc = (prop, thirdParty) => {
    if (!prop || !prop.name || seen.has(prop.name)) {
        return;
    }
    seen.add(prop.name);
    const hints = hintsByName.get(prop.name) || [];
    const deprecation = prop.deprecation || {};
    const doc = {
        name: prop.name,
        type: shortType(prop.type),
        description: prop.description || prop.shortDescription || '',
        defaultValue: defaultText(prop.defaultValue),
        kind: thirdParty ? 'thirdparty' : (hints.some(value => value.description === HINT_REQUIRED) || prop.required === true ? 'required' : 'optional')
    };
    const module = thirdParty ? undefined : (hintModule(hints) || prop.module);
    if (module) {
        doc.module = module;
    }
    if (hints.some(value => value.description === HINT_DURATION) || prop.duration === true) {
        doc.duration = true;
    }
    const level = deprecation.level || prop.deprecationLevel || (prop.deprecated ? 'warning' : '');
    if (level) {
        doc.deprecated = String(level).toLowerCase();
        const replacement = deprecation.replacement || prop.deprecationReplacement;
        if (replacement) {
            doc.replacement = replacement;
        }
    }
    docs.push(doc);
};
jsonProps.forEach(prop => addDoc(prop, false));
yamlProps.forEach(prop => addDoc(prop, true));

const out = {
    generated: new Date().toISOString(),
    docs
};

fs.writeFileSync(outputPath, JSON.stringify(out));
console.log(`Search index is written to ${outputPath}`);

if (combinedOutputPath) {
    if (inputParsingFailed) {
        console.error(`Combined configuration metadata cannot be created because an input could not be read`);
        process.exit(1);
    }
    fs.writeFileSync(combinedOutputPath, JSON.stringify(allProps));
    console.log(`Combined configuration metadata is written to ${combinedOutputPath}`);
}
