---
layout: default
title: CAS - Configuration Properties
category: Configuration
---

{% include variables.html %}

# Configuration Properties

You may search the CAS configuration catalog to find properties and their descriptions.
The search is powered by [Lunr.js](https://lunrjs.com/), a small, full-text search library for JavaScript.
It is designed to be fast and lightweight, making it ideal for client-side applications.

<div class="container py-2">
    <div class="row justify-content-center">
        <div>
            <div class="input-group mb-3">
              <span class="input-group-text" id="search-addon">
                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-search" viewBox="0 0 16 16">
                  <path d="M11.742 10.344a6.5 6.5 0 1 0-1.397 1.398h-.001l3.85 3.85a1 1 0 0 0 1.415-1.414l-3.85-3.85zm-5.242 0a5 5 0 1 1 0-10 5 5 0 0 1 0 10z"/>
                </svg>
              </span>
                <input id="search-input" type="search" class="form-control" 
                    placeholder="Type to search properties..."
                    tabindex="0" style="min-height: 4rem; font-size: 1.1rem; font-family: Menlo, Monaco, Consolas, monospace"
                    aria-label="Search" aria-describedby="search-addon">
            </div>

            <div class="d-flex justify-content-between align-items-center mb-3">
              <div>
                <div class="form-check form-check-inline">
                  <input class="form-check-input" type="checkbox" id="ignore-cache-checkbox">
                  <label class="form-check-label" for="ignore-cache-checkbox">Ignore Cache</label>
                </div>
                <div class="form-check form-check-inline">
                  <input class="form-check-input" type="checkbox" checked id="exact-match-checkbox">
                  <label class="form-check-label" for="exact-match-checkbox">Exact Match</label>
                </div>
              </div>
              <div class="input-group" style="width: auto;">
                <label class="input-group-text" for="max-results-select">Max Results</label>
                <select class="form-select" id="max-results-select">
                  <option value="5">5</option>
                  <option value="10" selected>10</option>
                  <option value="25">25</option>
                  <option value="50">50</option>
                  <option value="100">100</option>
                  <option value="all">All</option>
                </select>
              </div>
            </div>

            <ul id="search-results" class="list-group"></ul>
        </div>
    </div>
</div>


<script src="https://unpkg.com/lunr/lunr.js"></script>
<script>
    (async () => {
        const ignoreCacheEl = document.getElementById('ignore-cache-checkbox');
        
        const url = "{{ basePath }}/assets/data/{{ version }}/index.json?v={{ site.time | date: '%Y%m%d%H%M%S' }}";
        console.log("Loading data from", url);
        const resp = ignoreCacheEl.checked 
            ? await fetch(url, { cache: 'reload' })
            : await fetch(url);

        const { index: indexJson, docs } = await resp.json();
        const idx = lunr.Index.load(indexJson);

        const input = document.getElementById('search-input');
        const exactMatchEl = document.getElementById('exact-match-checkbox');
        const maxResultsEl = document.getElementById('max-results-select');
        const resultsList = document.getElementById('search-results');
        let timer;

        function convertJavadoc(text) {
            if (!text) {
                return '';
            }
            return text
                .replace(/\{@code\s+([^}]+)\}/g, '<code>$1</code>')
                .replace(/\{@link\s+([^\s}]+)\s*([^}]*)\}/g, (m, link, label) => {
                    const lbl = label || link;
                    const url = `${link.replace(/\./g, '/')}.html`;
                    return `<a href="${url}">${lbl}</a>`;
                })
                .replace(/\r?\n/g, ' ')
                .replace(/\s+/g, ' ')
                .trim();
        }
        
        function prepareQuery(input, exact) {
          input = input.replace("[0]", "[]");
          const parts = input.split(/[^A-Za-z0-9]+/).filter(Boolean);
          if (!parts.length) return '';
          if (exact) {
            // exact: match whole tokens
            return input;
          }
          // prefix only on last segment
          return parts.map((term, i) =>
            i === parts.length - 1 ? `*${term}*` : term
          ).join(' ');
        }

        async function performSearch() {
          const raw = input.value.trim();
          resultsList.innerHTML = '';
          if (!raw) return;

          const exact = exactMatchEl.checked;

          const query = prepareQuery(raw, exact);
          if (!query) return;

          let results = query ? idx.search(query) : [];

          const max = maxResultsEl.value;
          if (max !== 'all') {
              results = results.slice(0, Number(max));
          }

          if (!results.length) {
              resultsList.innerHTML = '<li class="list-group-item text-center text-muted">No results found</li>';
              return;
          }

          results.forEach(({ ref, score }) => {
              const doc = docs[ref];
              const li = document.createElement('li');
              li.className = 'list-group-item';
              li.innerHTML = `
                  <h5 class="mb-1"><code>${doc.name.replace(/\[\]/g, '[0]')} = ${doc.defaultValue}</code></h5>
                  <small class="text-muted">Score: ${score.toFixed(2)}</small>
                  <p class="mb-0 text-justify">${convertJavadoc(doc.description) || '<em>No description</em>'}</p>
              `;
              resultsList.appendChild(li);
          });
        }

        window.addEventListener('load', () => inputEl.focus());
        input.addEventListener('input', () => performSearch());
        ignoreCacheEl.addEventListener('change', () => performSearch());
        exactMatchEl.addEventListener('change', () => performSearch());
        maxResultsEl.addEventListener('change', () => performSearch());
        


    })();
</script>
