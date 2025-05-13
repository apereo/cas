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

const rawYaml = fs.readFileSync(yamlPath, 'utf8');
const parsed = yaml.load(rawYaml);
const yamlProps = Array.isArray(parsed) ? parsed : [];
console.log(`Found ${yamlProps.length} YAML properties`);

const raw = fs.readFileSync(jsonPath, 'utf8');
const data = JSON.parse(raw);
const jsonProps = data.properties;
console.log(`Found ${jsonProps.length} JSON properties`);

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
