#!/usr/bin/env node

const fs = require('fs');
const lunr = require('lunr');
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

const docs = allProps.map((prop, idx) => ({
    id: idx,
    name: prop.name,
    type: prop.type,
    description: prop.description || '',
    defaultValue: prop.defaultValue || ''
}));

const idx = lunr(function () {
    this.ref('id');
    this.field('name', { boost: 10 });
    this.field('description');
    docs.forEach(doc => this.add(doc));
});

const out = {
    index: idx.toJSON(),
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
