#!/usr/bin/env node

const fs = require('fs');
const lunr = require('lunr');
const yaml = require('js-yaml');

const jsonPath = process.argv[2];
const yamlPath = process.argv[3];
const outputPath = process.argv[4];
console.log(`JSON path: ${jsonPath}`);
console.log(`YAML path: ${yamlPath}`);
console.log(`Output path: ${outputPath}`);

let yamlProps = [];
try {
    if (fs.existsSync(yamlPath)) {
        const rawYaml = fs.readFileSync(yamlPath, 'utf8');
        const parsed = yaml.load(rawYaml);
        yamlProps = Array.isArray(parsed) ? parsed : [];
        console.log(`Found ${yamlProps.length} YAML properties`);
    } else {
        console.log(`YAML path does not exist: ${yamlPath}`);
    }
} catch(e) {
    console.log(`Error parsing YAML file: ${e}`);
}

let jsonProps = []
try {
    if (fs.existsSync(jsonPath)) {
        const raw = fs.readFileSync(jsonPath, 'utf8');
        const data = JSON.parse(raw);
        jsonProps = data.properties;
        console.log(`Found ${jsonProps.length} JSON properties`);
    } else {
        console.log(`JSON path does not exist: ${jsonPath}`);
    }
} catch(e) {
    console.log(`Error parsing JSON file: ${e}`);
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
