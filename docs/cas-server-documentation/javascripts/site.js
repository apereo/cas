const CONST_CURRENT_VER = "development";

function isDocumentationSiteViewedLocally() {
  return location.href.startsWith("http://localhost:4000");
}

function generateNavigationBarAndCrumbs() {
  const navigation = document.getElementById('docsNavBar');
  if (!navigation) {
    return;
  }
  const crumbs = document.createElement('ol');
  crumbs.className = 'breadcrumb';
  const segments = location.pathname.split('/').filter(Boolean);
  const start = isDocumentationSiteViewedLocally() ? 0 : 1;
  segments.slice(start).forEach((segment, index, items) => {
    const item = document.createElement('li');
    const current = index === items.length - 1;
    const link = document.createElement(current ? 'span' : 'a');
    link.textContent = current ? document.title.split(' · ')[0].replace('CAS -', '').trim()
      : segment.replace(/[-_]/g, ' ');
    if (current) {
      link.setAttribute('aria-current', 'page');
    } else {
      link.href = '/' + segments.slice(0, start + index + 1).join('/') + '/';
      link.className = 'capitalize';
    }
    item.append(link);
    crumbs.append(item);
  });
  navigation.append(crumbs);
}

function getActiveDocumentationVersionInView(returnBlankIfNoVersion) {
  let currentVersion = CONST_CURRENT_VER;
  let href = location.href;
  let index = isDocumentationSiteViewedLocally() ? href.indexOf("4000/") : -1;

  if (index === -1) {
    const uri = new URI(document.location);

    if (uri.filename() !== uri.segment(1) && uri.segment(1) !== "developer") {
      currentVersion = uri.segment(1);
    } else if (returnBlankIfNoVersion) {
      return "";
    }
  } else {
    href = href.substring(index + 5);
    index = href.indexOf("/");
    currentVersion = href.substring(0, index);
  }
  return currentVersion;
}


function loadSidebarForActiveVersion() {
  if (!document.getElementById('sidebar')) {
    return;
  }
  let prefix = isDocumentationSiteViewedLocally() ? "/" : "/cas/";
  $.get(`${prefix + getActiveDocumentationVersionInView()}/sidebar.html`, data => {
    const menu = $(data);

    if (menu.first().is('ul')) {

      menu.addClass('nav flex-column').attr('id', 'sidebarTopics');

      const topLevel = menu.find("> li>a");

      const topLevelUl = menu.find("> li>ul");

      const subLevel = menu.find("> li ul");

      const nestedMenu = menu.find("ul li").has("ul").children("a");

      topLevel.each(function () {
        const el = $(this);
        //console.log("Top level: " + el);
        sidebarTopNav(el);
      });

      topLevelUl.each(function () {
        const el = $(this);
        el.attr({
          'data-bs-parent': '#sidebarTopics'
        });

        if (!el.prev().hasClass('collapsed')) {
          el.addClass('show');
        }
      });

      subLevel.each(function () {
        sidebarSubNav($(this));
      });

      nestedMenu.each(function () {
        sidebarTopNav($(this));
      });

      $('#sidebar-navigation').append(menu);

      generateSidebarLinksForActiveVersion();

      const uri = new URI(document.location);
      if (uri.filename() === "index.html" || uri.filename() === '') {
        return;
      }

      let count = 0;
      let element = $(`#sidebarTopics a[href*='/${uri.filename()}']`);
      let parent = element.parent();
      while (parent !== null && parent !== undefined) {
        let id = parent.attr("id");
        if (id === "sidebarTopics" || count >= 10) {
          break;
        }
        if (id !== undefined) {
          parent.collapse('show');
          parent.prev('a').removeClass('collapsed').attr('aria-expanded', 'true');
        }
        count++;
        parent = parent.parent();
      }
      element.attr("aria-current", "page");

      if (element.length && window.matchMedia('(min-width: 761px)').matches) {
        const sidebar = document.getElementById('sidebar');
        sidebar.scrollTop = Math.max(0, element[0].offsetTop - sidebar.clientHeight / 3);
      }
    }
  });
}

function sidebarTopNav(el) {
  if (el.attr('href').search(/(?:^|)#/g) >= 0) {
    el.attr({
      'data-bs-toggle': "collapse",
      'aria-expanded': "false",
      'aria-controls': el.attr('href').substring(1),
      role: 'button',
      title: el.text(),
      class: 'collapsed'
    })
    .append('<i class="expand" aria-hidden="true"></i>');
    el.on('keydown', event => {
      if (event.key === ' ') {
        event.preventDefault();
        el[0].click();
      }
    });
  }

  if (pageSection && el.text() === pageSection) {
    el.removeClass('collapsed').attr('aria-expanded', 'true');
  }

}


function sidebarSubNav(el) {
  let prevId = $(el).prev("a").attr("href");

  if (prevId.search(/^#.*$/) >= 0) {
    prevId = prevId.substr(1);
  } else {
    prevId = '';
  }

  if (prevId === '') {
    $(el).addClass('nav flex-column subnav ms-3');
  } else {
    $(el).addClass('nav flex-column collapse subnav ms-3').attr('id', prevId);
  }
}

function generateSidebarLinksForActiveVersion() {
  $('#sidebar a').each(function () {
    let href = this.href;
    if (href.indexOf("$version") !== -1) {
      href = href.replace("$version", `cas/${getActiveDocumentationVersionInView()}`);
    }
    
    if (href.includes("#")) {
      href = href.substring(href.indexOf("#"));
    } else if (isDocumentationSiteViewedLocally() && href.includes("http://localhost")) {
      href = href.replace("/cas", "");
    }
    $(this).attr('href', href);
  });
}

function navigateSidebar() {
  const hidden = document.body.classList.toggle('docs-sidebar-hidden');
  document.getElementById('sidebarNavButton')?.setAttribute('aria-expanded', String(!hidden));
}

function toggleDarkMode() {
  const newTheme = document.documentElement.dataset.bsTheme === 'dark' ? 'light' : 'dark';
  changeTheme(newTheme);
  try {
    localStorage.setItem('cas-docs-theme', newTheme);
  } catch (error) {
    // The theme still works when browser storage is unavailable.
  }
  document.getElementById('docs-status').textContent = `${newTheme === 'dark' ? 'Dark' : 'Light'} theme enabled`;
}

function generateToolbarIcons() {
  let casRepositoryUrl = $('#forkme_banner').attr('href');
  let activeVersion = getActiveDocumentationVersionInView(true);

  let uri = new URI(document.location);
  let segments = uri.segment();
  let page = "";

  for (let i = isDocumentationSiteViewedLocally() ? 0 : 1; i < segments.length; i++) {
    page += `${segments[i]}/`;
  }
  let editablePage = page.replace(".html", ".md");
  editablePage = editablePage.replace(CONST_CURRENT_VER, "");
  editablePage = editablePage.replace(activeVersion, "");
  if (editablePage === "") {
    editablePage = "index.md";
  }


  let href = location.href.replace("https://apereo.github.io/cas", "http://localhost:4000");
  $('#toolbarIcons').append(`<a href='${href}'><i class='fab fa-codepen' aria-hidden='true'></i><span class='visually-hidden'>See this page running on localhost</span></a>`);

  if (activeVersion !== CONST_CURRENT_VER && activeVersion !== "") {
    let prefix = isDocumentationSiteViewedLocally() ? "/" : "/cas/";
    let linkToDev = prefix + page.replace(activeVersion, CONST_CURRENT_VER).replace("//", "/");
    linkToDev = linkToDev.replace("html/", "html");

    $('#toolbarIcons').append(`<a href='${linkToDev}'><i class='fa fa-code' aria-hidden='true'></i><span class='visually-hidden'>See the latest version of this page</span></a>`);
  }

  let baseLink = casRepositoryUrl;
  let editLink = "";
  let historyLink = "";
  let deleteLink = "";

  if (activeVersion === "") {
    editLink = `${baseLink}/edit/gh-pages/`;
    historyLink = `${baseLink}/commits/gh-pages/`;
    deleteLink = `${baseLink}/delete/gh-pages/`;
  } else if (activeVersion === CONST_CURRENT_VER) {
    editLink = `${baseLink}/edit/master/docs/cas-server-documentation/`;
    historyLink = `${baseLink}/commits/master/docs/cas-server-documentation/`;
    deleteLink = `${baseLink}/delete/master/docs/cas-server-documentation/`;
  } else {
    editLink = `${baseLink}/edit/${activeVersion}/docs/cas-server-documentation/`;
    historyLink = `${baseLink}/commits/${activeVersion}/docs/cas-server-documentation/`;
    deleteLink = `${baseLink}/delete/${activeVersion}/docs/cas-server-documentation/`;
  }

  editLink += editablePage;

  $('#toolbarIcons').append(`<a target='_blank' rel='noopener' href='${editLink}'><i class='fa fa-pencil-alt' aria-hidden='true'></i><span class='visually-hidden'>Edit with GitHub</span></a>`);

  historyLink += editablePage;


  $('#toolbarIcons').append(`<a target='_blank' rel='noopener' href='${historyLink}'><i class='fa fa-history' aria-hidden='true'></i><span class='visually-hidden'>View commit history on GitHub</span></a>`);

  deleteLink += editablePage;

  $('#toolbarIcons').append(`<a target='_blank' rel='noopener' href='${deleteLink}'><i class='fa fa-times' aria-hidden='true'></i><span class='visually-hidden'>Delete with GitHub</span></a>`);
}

function generatePageTOC() {
  const contents = document.querySelector('#pageContents ul');
  if (!contents) {
    return;
  }
  const headings = Array.from(document.querySelectorAll('#cas-docs-container h1[id], #cas-docs-container h2[id], #cas-docs-container h3[id]'));
  headings.forEach(heading => {
    const item = document.createElement('li');
    item.className = `toc-entry toc-${heading.tagName.toLowerCase()}`;
    const link = document.createElement('a');
    link.href = '#' + heading.id;
    link.textContent = heading.textContent;
    item.append(link);
    contents.append(item);
  });
  if (!headings.length) {
    contents.closest('.docs-toc').hidden = true;
    return;
  }
  let scheduled = false;
  const updateCurrentSection = () => {
    const header = document.querySelector('.site-header').offsetHeight + 40;
    const visibleHeadings = headings.filter(heading => heading.getClientRects().length > 0);
    const active = visibleHeadings.filter(heading => heading.getBoundingClientRect().top <= header).at(-1) || visibleHeadings[0];
    contents.querySelectorAll('a').forEach(link => {
      link.parentElement.hidden = !visibleHeadings.some(heading => link.hash === '#' + heading.id);
      if (active && link.hash === '#' + active.id) {
        link.setAttribute('aria-current', 'location');
      } else {
        link.removeAttribute('aria-current');
      }
    });
    scheduled = false;
  };
  const scheduleUpdate = () => {
    if (!scheduled) {
      scheduled = true;
      requestAnimationFrame(updateCurrentSection);
    }
  };
  window.addEventListener('scroll', scheduleUpdate, {passive: true});
  document.getElementById('cas-docs-container').addEventListener('click', scheduleUpdate);
  document.getElementById('cas-docs-container').addEventListener('shown.bs.tab', scheduleUpdate);
  updateCurrentSection();
}

function responsiveImages() {
  $('img').each(function () {
    $(this).addClass('img-fluid');
  });
}

function responsiveTables() {
  $('#cas-docs-container table').not('.rouge-table, .highlight table, .cas-op-params table').each(function () {
    $(this).addClass('table');
    if (!this.closest('.table-scroll') && !this.classList.contains('cas-datatable')) {
      $(this).wrap('<div class="table-scroll" role="region" aria-label="Scrollable table" tabindex="0"></div>');
    }
  });
}


function enableBootstrapTooltips() {
  $('[data-bs-toggle="tooltip"]').tooltip();
}

function generateOverlay(artifactId, type) {
  let id = artifactId.replace("cas-server-", "");
  $("#overlayform").remove();
  $('body').append(` 
  <form id='overlayform' action='https://getcas.apereo.org/starter.zip' method='post'> 
    <input type='submit' value='submit' /> 
    <input type='hidden' name='dependencies' value='${id}' /> 
    <input type='hidden' name='type' value='${type}' /> 
  </form>`);
  $("#overlayform").submit();
}

function showOverlay(artifactId, type) {
  let id = artifactId.replace("cas-server-", "");
  $("#overlaydialog").remove();
  
  let iframe = $('<iframe>', {
    src: `https://getcas.apereo.org/ui?dependencies=webapp-tomcat,${id}`,
    id:  'overlayframe',
    title: 'CAS Initializr',
    frameborder: 0,
    scrolling: 'no'
  }).css({
    width: '100%',
    height: '100%',
    border: 'none'
  });

  let dialogConfig = {
    title: `CAS Initializr with ${artifactId}`,
    width: 800,
    height: 600,
    modal: true,
    resizable: true,
    draggable: true,
    autoOpen: false,
    closeText: 'Close',
    closeOnEscape: true,
    show: {
      effect: 'fade',
      duration: 500
    },
    hide: {
      effect: 'fade',
      duration: 500
    }
  };
  $('body').append("<div id='overlaydialog'></div>");
  $(document).on("click", e => $("#overlaydialog").dialog("destroy"));

  $("#overlaydialog").append(iframe).dialog(dialogConfig).dialog('open');
}

function initializePage() {
    new ClipboardJS('.copy-button');

    let tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'))
    let tooltipList = tooltipTriggerList.map(tooltipTriggerEl => new bootstrap.Tooltip(tooltipTriggerEl));

    let activeVersion = getActiveDocumentationVersionInView(true);
    let filters = [`version: ${activeVersion}`];
    console.log(`Documentation search is filtering by ${filters}`);

    if (typeof docsearch === 'function') {
      // The search overlay contains interactive controls and is a dialog, not a button.
      const searchDialogObserver = new MutationObserver(records => {
        records.forEach(record => record.addedNodes.forEach(node => {
          if (node instanceof HTMLElement && node.classList.contains('DocSearch-Container')) {
            node.setAttribute('role', 'dialog');
            node.setAttribute('aria-modal', 'true');
            node.setAttribute('aria-label', 'Search documentation');
            node.setAttribute('tabindex', '-1');
            node.removeAttribute('aria-expanded');
            node.removeAttribute('aria-haspopup');
            node.removeAttribute('aria-labelledby');
          }
        }));
      });
      searchDialogObserver.observe(document.body, {childList: true});
      docsearch({
        apiKey: 'a95d9cc5493147925fb5d4fdb5afb414',
        appId: 'IW4GLK9JZ0',
        indexName: 'apereoapereo',
        container: '#searchField',
        searchParameters: { 'facetFilters': filters },
        debug: true
      });
    }
}

$(() => {
  const mobileNavigation = window.matchMedia('(max-width: 760px)');
  const updateNavigation = () => {
    document.body.classList.toggle('docs-sidebar-hidden', mobileNavigation.matches);
    document.getElementById('sidebarNavButton')?.setAttribute('aria-expanded', String(!mobileNavigation.matches));
  };
  updateNavigation();
  mobileNavigation.addEventListener('change', updateNavigation);
  document.addEventListener('keydown', event => {
    if (event.key === 'Escape' && mobileNavigation.matches && !document.body.classList.contains('docs-sidebar-hidden')) {
      navigateSidebar();
      document.getElementById('sidebarNavButton')?.focus();
    }
  });
  const header = document.querySelector('.site-header');
  new ResizeObserver(() => {
    document.documentElement.style.setProperty('--docs-header-height', `${header.offsetHeight}px`);
  }).observe(header);
  loadSidebarForActiveVersion();
  generatePageTOC();
  generateToolbarIcons();
  generateNavigationBarAndCrumbs();
  responsiveImages();
  responsiveTables();
  enableBootstrapTooltips();
  initializePage();
});


$(() => $("h2, h3, h4, h5, h6").each((i, el) => {
  let $el, icon, id;
  $el = $(el);
  id = $el.attr('id');
  icon = '<i class="fa fa-link" aria-hidden="true"></i>';
  if (id) {
    return $el.prepend($("<a />").addClass("header-link").attr("href", `#${id}`).attr("aria-label", `Link to ${$el.text()}`).html(icon));
  }
}));


let codes = document.querySelectorAll('.highlight > pre > code .rouge-code pre');
let countID = 0;
codes.forEach((code) => {

  code.setAttribute("id", `code${countID}`);
  
  const btn = document.createElement('button');
  btn.innerHTML = '<i class="fa fa-copy" aria-hidden="true"></i>';
  btn.className = "btn-copy-code";
  
  btn.setAttribute("type", "button");
  btn.setAttribute("aria-label", "Copy code");
  btn.setAttribute("title", "Copy Code");
  btn.setAttribute("data-clipboard-action", "copy");
  btn.setAttribute("data-clipboard-target", `#code${countID}`);
  // btn.setAttribute("onclick", "this.innerHTML='Copied';");

  let div = document.createElement('div');
  div.className = "div-code-button";
  div.appendChild(btn);
  
  code.before(div);
  countID++;
}); 
const codeClipboard = new ClipboardJS('.btn-copy-code');
codeClipboard.on('success', event => {
  document.getElementById('docs-status').textContent = 'Code copied to clipboard';
  event.clearSelection();
});
codeClipboard.on('error', () => {
  document.getElementById('docs-status').textContent = 'Unable to copy. Select the code and copy it manually.';
});

$(document).ready(() => {
  let pageLength = $(".cas-datatable").data("page-length");
  if (pageLength === null || pageLength === undefined || pageLength === "") {
      pageLength = 5;
    }
    $('.cas-datatable').DataTable({
      "processing": true,
      "lengthMenu": [ 5, 10, 15, 25, 50],
      "pageLength": pageLength
    });
    initializeCasProperties();
    initializeCasActuators();

    let popoverTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="popover"]'));
    let popoverList = popoverTriggerList.map(popoverTriggerEl => new bootstrap.Popover(popoverTriggerEl))
});

let ROWS = 5;

function next(id) {

  let rows = $(`#${id} tbody tr`);
  let rowCount = rows.length;

  let s = 0;
  s = rows.attr("start");
  if (s === undefined) {
    s = 0;
  } else {
    if (parseInt(s) + ROWS < rowCount) {
      s = parseInt(s) + ROWS;
    }
  }

  let e = 0;
  e = rows.attr("end");
  if (e === undefined) {
    e = ROWS;
  } else {
    e = parseInt(e) + ROWS;
  }
  if (e > rowCount) {
    e = rowCount;
  }


  rows.hide().slice(s, e).show();
  // console.log("start " + s + " end " + e);

  rows.attr("start", s);
  rows.attr("end", e);

  $(`#${id} thead tr`).show();
}

function previous(id) {
  let rows = $(`#${id} tbody tr`);
  let rowCount = rows.length;

  let start = parseInt(rows.attr("start"));
  let end = parseInt(rows.attr("end"));

  // console.log("current start " + start + " current end " + end);

  start -= ROWS;
  if (start < 0) {
    start = 0;
  }
  end -= ROWS;
  if (end < ROWS) {
    end = ROWS;
  }
  if (end - start < ROWS) {
    end = start + ROWS;
  }

  rows.hide().slice(start, end).show();
  // console.log("start " + start + " end " + end);

  rows.attr("start", start);
  rows.attr("end", end);

  $(`#${id} thead tr`).show();
}




/***********************
 * Tabs
 **********************/

const removeActiveClasses = ulElement => {
    const lis = ulElement.querySelectorAll('li');
    Array.prototype.forEach.call(lis, li => li.classList.remove("active"));
};

const getChildPosition = element => {
  const parent = element.parentNode;
  let i = 0;
  for (let i = 0; i < parent.children.length; i++) {
        let child = parent.children[i];
        console.log(child.classList);
        if (child === element) {
            return i;
        }
    }

    throw new Error('No parent found');
};

window.addEventListener('load', () => {
  const tabLinks = document.querySelectorAll('ul.tab li a');

  Array.prototype.forEach.call(tabLinks, link => {
      let property = link.parentElement.classList.contains("property-name");
      if (property === false) {
        link.addEventListener('click', event => {
            event.preventDefault();

            let liTab = link.parentNode;
            let ulTab = liTab.parentNode;
            let position = getChildPosition(liTab);
            if (liTab.className.includes('active')) {
                return;
            }

            removeActiveClasses(ulTab);
            let tabContentId = ulTab.getAttribute('data-tab');
            let tabContentElement = document.getElementById(tabContentId);
            removeActiveClasses(tabContentElement);

            // let elements = tabContentElement.querySelectorAll('li:not(.property-name):not(.property-tab)');
            tabContentElement.children[position].classList.add('active');
            // elements[position].classList.add('active');
            liTab.classList.add('active');
            ulTab.querySelectorAll(':scope > li > a').forEach(tab => {
              const selected = tab === link;
              tab.setAttribute('aria-selected', String(selected));
              tab.tabIndex = selected ? 0 : -1;
            });
            
            let tabs = $(`#${tabContentId} ul.nav.nav-tabs li a`).not("[href^='#notes']");
            for (let i = 0; i < tabs.length; i++) {
              let tb = tabs[i];
              tb.click();
            }
            
        }, false);
      }
  });
});
/***********************
 * Tabs
 **********************/

window.addEventListener('load', () => {
  document.querySelectorAll('ul.tab[data-tab]').forEach((tabList, group) => {
    const panels = document.getElementById(tabList.dataset.tab);
    if (!panels) {
      return;
    }
    panels.setAttribute('role', 'presentation');
    const links = Array.from(tabList.querySelectorAll(':scope > li > a'));
    tabList.setAttribute('role', 'tablist');
    tabList.setAttribute('aria-label', 'Configuration examples');
    links.forEach((link, index) => {
      const panel = panels.children[index];
      if (!panel) {
        return;
      }
      link.id ||= `docs-tab-${group}-${index}`;
      panel.id ||= `docs-panel-${group}-${index}`;
      link.parentElement.setAttribute('role', 'presentation');
      link.setAttribute('role', 'tab');
      link.setAttribute('aria-controls', panel.id);
      const selected = link.parentElement.classList.contains('active');
      link.setAttribute('aria-selected', String(selected));
      link.tabIndex = selected ? 0 : -1;
      panel.setAttribute('role', 'tabpanel');
      panel.setAttribute('aria-labelledby', link.id);
      panel.tabIndex = 0;
      link.addEventListener('keydown', event => {
        let nextIndex;
        if (event.key === 'ArrowRight') nextIndex = (index + 1) % links.length;
        if (event.key === 'ArrowLeft') nextIndex = (index + links.length - 1) % links.length;
        if (event.key === 'Home') nextIndex = 0;
        if (event.key === 'End') nextIndex = links.length - 1;
        if (nextIndex !== undefined) {
          event.preventDefault();
          links[nextIndex].focus();
          links[nextIndex].click();
        } else if (event.key === ' ') {
          event.preventDefault();
          link.click();
        }
      });
    });
  });
});

window.addEventListener('load', () => {
  document.querySelectorAll('.nav-tabs > li, .nav-pills > li').forEach(item => {
    if (item.parentElement.getAttribute('role') === 'tablist') {
      item.setAttribute('role', 'presentation');
    }
  });
});

// Reveal only decorative homepage cards; technical content remains immediately visible.
$(() => {
  if (!document.body.classList.contains('docs-home')
      || window.matchMedia('(prefers-reduced-motion: reduce)').matches
      || !('IntersectionObserver' in window)) {
    return;
  }
  const observer = new IntersectionObserver(entries => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        entry.target.classList.add('is-revealed');
        observer.unobserve(entry.target);
      }
    });
  }, {threshold: 0.08});
  document.querySelectorAll('.home-paths a, .docs-home .card').forEach((card, index) => {
    card.style.setProperty('--reveal-delay', `${index % 3 * 70}ms`);
    card.classList.add('reveal-ready');
    observer.observe(card);
  });
});

// A thin progress rule under the masthead tracks how far a long guide has been read.
$(() => {
  const indicator = document.getElementById('readingProgress');
  const article = document.getElementById('cas-docs-container');
  if (!indicator || !article) {
    return;
  }
  let scheduled = false;
  const update = () => {
    const start = article.offsetTop;
    const span = article.offsetHeight - window.innerHeight + start;
    const read = span > 0 ? (window.scrollY - start) / (span - start || 1) : 0;
    indicator.style.transform = `scaleX(${Math.min(1, Math.max(0, read))})`;
    scheduled = false;
  };
  const schedule = () => {
    if (!scheduled) {
      scheduled = true;
      requestAnimationFrame(update);
    }
  };
  window.addEventListener('scroll', schedule, {passive: true});
  window.addEventListener('resize', schedule, {passive: true});
  update();
});

const CAS_PROPERTY_FORMATS = {properties: '.properties', yaml: 'YAML', env: 'Env vars'};
const CAS_PROPERTY_FORMAT_KEY = 'cas-docs-property-format';

function readCasPropertyFormat() {
  try {
    const format = localStorage.getItem(CAS_PROPERTY_FORMAT_KEY);
    return CAS_PROPERTY_FORMATS[format] ? format : 'properties';
  } catch (error) {
    return 'properties';
  }
}

function writeCasPropertyFormat(format) {
  try {
    localStorage.setItem(CAS_PROPERTY_FORMAT_KEY, format);
    return true;
  } catch (error) {
    return false;
  }
}

function casPropertyEnvironmentVariable(name) {
  return name
    .replace(/\[(\d+)]/g, '_$1_')
    .replace(/\./g, '_')
    .replace(/-/g, '')
    .replace(/_+/g, '_')
    .replace(/^_|_$/g, '')
    .toUpperCase();
}

function casPropertyValue(row) {
  const value = row.dataset.default;
  return value === '' ? '...' : value;
}

function casPropertyYaml(rows) {
  const tree = {};
  rows.forEach(row => {
    let node = tree;
    const segments = row.dataset.name.split('.');
    segments.forEach((segment, index) => {
      if (index === segments.length - 1) {
        node[segment] = casPropertyValue(row);
      } else {
        node[segment] = typeof node[segment] === 'object' ? node[segment] : {};
        node = node[segment];
      }
    });
  });
  const lines = [];
  const walk = (node, depth) => Object.entries(node).forEach(([key, value]) => {
    if (typeof value === 'object') {
      lines.push(`${'  '.repeat(depth)}${key}:`);
      walk(value, depth + 1);
    } else {
      lines.push(`${'  '.repeat(depth)}${key}: "${value.replace(/"/g, '\\"')}"`);
    }
  });
  walk(tree, 0);
  return lines.join('\n');
}

function casPropertySnippet(rows, format, withComments) {
  if (format === 'yaml') {
    return casPropertyYaml(rows);
  }
  return rows.map(row => {
    const line = format === 'env'
      ? `${casPropertyEnvironmentVariable(row.dataset.name)}=${casPropertyValue(row)}`
      : `${row.dataset.name}=${casPropertyValue(row)}`;
    const summary = row.querySelector('.cas-property-summary')?.textContent.trim();
    return withComments && summary ? `# ${summary}\n${line}` : line;
  }).join(withComments ? '\n\n' : '\n');
}

function casPropertyPrefix(names) {
  if (names.length < 2) {
    return '';
  }
  const split = names.map(name => name.split('.'));
  const prefix = [];
  for (let i = 0; ; i++) {
    const segment = split[0][i];
    if (segment === undefined || split.some(parts => parts[i] !== segment || parts.length <= i + 1)) {
      break;
    }
    prefix.push(segment);
  }
  return prefix.length > 1 ? `${prefix.join('.')}.` : '';
}

function casPropertyGroupTitle(group) {
  if (group === '') {
    return 'General';
  }
  return group.replace(/\[\d+]/g, '').replace(/[-.]/g, ' ').replace(/\b\w/g, letter => letter.toUpperCase());
}

function renderCasPropertyUsage(row) {
  const usage = row.querySelector('.cas-property-usage');
  if (!usage) {
    return;
  }
  const format = readCasPropertyFormat();
  const name = row.dataset.name;
  usage.querySelector('code').textContent = casPropertySnippet([row], format, false);
  const hints = [];
  if (row.hasAttribute('data-duration')) {
    hints.push('<i class="fa fa-clock" aria-hidden="true"></i><span>Accepts <code>java.time.Duration</code> values such as <code>PT20S</code>, <code>PT15M</code>, <code>PT10H</code> or <code>P2DT3H4M</code>. <code>0</code> or <code>never</code> means zero; blank, <code>-1</code> or <code>infinite</code> means unending.</span>');
  }
  if (row.hasAttribute('data-regex')) {
    hints.push('<i class="fa fa-asterisk" aria-hidden="true"></i><span>Accepts a regular expression, evaluated with <code>java.util.regex.Pattern</code>.</span>');
  }
  const alternatives = [];
  if (format !== 'env') {
    alternatives.push(`<code>${casPropertyEnvironmentVariable(name)}</code> as an environment variable`);
  }
  alternatives.push(`<code>-D${name}=…</code> as a system property`, `<code>--${name}=…</code> on the command line`);
  hints.push(`<i class="fa fa-terminal" aria-hidden="true"></i><span>Also settable as ${alternatives.join(', ')}.</span>`);
  usage.querySelector('.cas-property-hints').innerHTML = hints.map(hint => `<p>${hint}</p>`).join('');
}

function toggleCasProperty(row, open) {
  const head = row.querySelector('.cas-property-head');
  const detail = row.querySelector('.cas-property-detail');
  const expanded = open ?? head.getAttribute('aria-expanded') !== 'true';
  head.setAttribute('aria-expanded', String(expanded));
  row.classList.toggle('open', expanded);
  detail.hidden = !expanded;
  if (expanded) {
    renderCasPropertyUsage(row);
  }
}

function applyCasPropertyFilter(block) {
  const filter = block.dataset.filter || 'all';
  const query = (block.querySelector('.cas-properties-search input')?.value || '').trim().toLowerCase();
  let visible = 0;
  block.querySelectorAll(':scope > .cas-properties-list > .cas-properties-group').forEach(group => {
    let shown = 0;
    group.querySelectorAll(':scope > .cas-property').forEach(row => {
      const matchesFilter = filter === 'all'
        || row.dataset.kind === filter
        || (filter === 'duration' && row.hasAttribute('data-duration'))
        || (filter === 'deprecated' && row.hasAttribute('data-deprecated'));
      const matchesQuery = query === '' || row.dataset.search.includes(query);
      row.hidden = !(matchesFilter && matchesQuery);
      shown += row.hidden ? 0 : 1;
    });
    group.hidden = shown === 0;
    visible += shown;
  });
  const empty = block.querySelector(':scope > .cas-properties-empty');
  if (empty) {
    empty.hidden = visible > 0;
  }
  const copy = block.querySelector('.cas-properties-copy span');
  if (copy) {
    copy.textContent = casPropertyCopyLabel(block);
  }
}

function casPropertyCopyLabel(block) {
  const scope = (block.dataset.filter || 'all') === 'all' && block.querySelector('.cas-property[data-kind="required"]') ? 'required' : 'shown';
  return `Copy ${scope} as ${CAS_PROPERTY_FORMATS[readCasPropertyFormat()]}`;
}

function casPropertiesToCopy(block) {
  const rows = [...block.querySelectorAll(':scope > .cas-properties-list .cas-property:not([hidden])')];
  if ((block.dataset.filter || 'all') === 'all') {
    const required = rows.filter(row => row.dataset.kind === 'required');
    return required.length ? required : rows;
  }
  return rows;
}

function enhanceCasProperties(block) {
  if (block.dataset.enhanced === 'true') {
    return;
  }
  const list = block.querySelector(':scope > .cas-properties-list');
  const rows = list ? [...list.querySelectorAll(':scope > .cas-property')] : [];
  if (rows.length === 0) {
    return;
  }
  block.dataset.enhanced = 'true';
  block.classList.add('cas-properties-enhanced');
  const thirdPartyOnly = rows.every(row => row.dataset.kind === 'thirdparty');
  block.classList.toggle('cas-properties-thirdparty-only', thirdPartyOnly);

  const casNames = rows.filter(row => row.dataset.kind !== 'thirdparty').map(row => row.dataset.name);
  const isContainer = name => casNames.some(other => other !== name && (other.startsWith(`${name}.`) || other.startsWith(`${name}[`)));
  const prefix = casPropertyPrefix(casNames.filter(name => !isContainer(name)));
  const groups = new Map();
  rows.forEach(row => {
    const name = row.dataset.name;
    let group;
    let relative = name;
    if (row.dataset.kind === 'thirdparty' || prefix === '' || !name.startsWith(prefix)) {
      const segments = name.split('.');
      group = row.dataset.kind === 'thirdparty' ? `~${segments.slice(0, 2).join('.')}` : '';
    } else {
      relative = name.substring(prefix.length);
      const segments = relative.split('.');
      group = segments.length > 1 ? segments[0] : '';
    }
    const code = row.querySelector('.cas-property-name');
    code.title = name;
    if (relative !== name) {
      const local = group === '' ? relative : relative.substring(group.length + 1);
      code.innerHTML = `${group === '' ? '' : `<span class="cas-property-parent">${group}.</span>`}${local}`;
    }
    const description = row.querySelector('.cas-property-description');
    const summary = row.querySelector('.cas-property-summary');
    if (description && summary && description.textContent.trim() === summary.textContent.trim()) {
      description.hidden = true;
    }
    row.dataset.search = `${name} ${row.dataset.default} ${description?.textContent || ''}`.toLowerCase();

    const head = row.querySelector('.cas-property-head');
    head.setAttribute('role', 'button');
    head.tabIndex = 0;
    head.setAttribute('aria-expanded', 'false');
    head.insertAdjacentHTML('beforeend', '<i class="cas-property-chevron" aria-hidden="true"></i>');
    const detail = row.querySelector('.cas-property-detail');
    detail.hidden = true;
    detail.insertAdjacentHTML('beforeend', `
      <div class="cas-property-usage">
        <div class="cas-property-snippet"><pre><code></code></pre><button type="button" class="cas-property-copy"><i class="fa fa-copy" aria-hidden="true"></i><span>Copy</span></button></div>
        <div class="cas-property-hints"></div>
      </div>`);

    if (!groups.has(group)) {
      groups.set(group, []);
    }
    groups.get(group).push(row);
  });

  const ordered = [...groups.entries()].sort(([a], [b]) => {
    const rank = key => key === '' ? 0 : key.startsWith('~') ? 2 : 1;
    return rank(a) - rank(b) || a.localeCompare(b);
  });
  list.replaceChildren(...ordered.map(([group, members]) => {
    const section = document.createElement('div');
    section.className = 'cas-properties-group';
    const heading = document.createElement('div');
    heading.className = 'cas-properties-group-heading';
    heading.innerHTML = `${casPropertyGroupTitle(group.replace(/^~/, ''))}<span>${members.length}</span>`;
    section.append(heading, ...members);
    return section;
  }));

  const count = (predicate) => rows.filter(predicate).length;
  const chips = [
    ['all', 'All', rows.length],
    ['required', 'Required', count(row => row.dataset.kind === 'required')],
    ['optional', 'Optional', count(row => row.dataset.kind === 'optional')],
    ['thirdparty', 'Third party', count(row => row.dataset.kind === 'thirdparty')],
    ['duration', 'Duration', count(row => row.hasAttribute('data-duration'))],
    ['deprecated', 'Deprecated', count(row => row.hasAttribute('data-deprecated'))]
  ].filter(([key, , total]) => key === 'all' || (total > 0 && total < rows.length));

  const format = readCasPropertyFormat();
  const searchable = rows.length > 5 && !block.classList.contains('cas-properties-compact');
  const toolbar = document.createElement('div');
  toolbar.className = 'cas-properties-toolbar';
  toolbar.innerHTML = `
    ${searchable ? `<label class="cas-properties-search"><i class="fa fa-magnifying-glass" aria-hidden="true"></i><input type="search" placeholder="Filter ${rows.length} settings by name, value or description" aria-label="Filter settings"></label>` : ''}
    <div class="cas-properties-format" role="group" aria-label="Show settings as">${Object.entries(CAS_PROPERTY_FORMATS).map(([key, label]) =>
      `<button type="button" data-format="${key}" aria-pressed="${key === format}">${label}</button>`).join('')}</div>
    ${chips.length > 1 ? `<div class="cas-properties-filters" role="group" aria-label="Show">${chips.map(([key, label, total]) =>
      `<button type="button" class="cas-properties-filter" data-filter="${key}" aria-pressed="${key === 'all'}">${label}<b>${total}</b></button>`).join('')}</div>` : ''}`;

  const header = document.createElement('div');
  header.className = 'cas-properties-header';
  header.innerHTML = `<span>${prefix ? `Keys below are relative to <code>${prefix}</code>` : `${rows.length}${thirdPartyOnly ? ' third-party' : ''} setting${rows.length === 1 ? '' : 's'}`}</span>
    <button type="button" class="cas-properties-copy"><i class="fa fa-copy" aria-hidden="true"></i><span></span></button>`;

  const empty = document.createElement('p');
  empty.className = 'cas-properties-empty';
  empty.hidden = true;
  empty.textContent = 'No settings match this filter.';

  block.insertBefore(toolbar, list);
  block.insertBefore(header, list);
  list.after(empty);
  applyCasPropertyFilter(block);
}

function copyCasPropertyText(text, button) {
  const label = button.querySelector('span');
  const original = label.textContent;
  navigator.clipboard.writeText(text).then(() => {
    label.textContent = 'Copied';
    document.getElementById('docs-status').textContent = 'Copied to clipboard';
  }).catch(() => {
    label.textContent = 'Copy failed';
  }).finally(() => setTimeout(() => {
    label.textContent = original;
  }, 1200));
}

function applyCasPropertyFormat(format) {
  writeCasPropertyFormat(format);
  document.querySelectorAll('.cas-properties-format button, .cas-actuator-format button').forEach(button =>
    button.setAttribute('aria-pressed', String(button.dataset.format === format)));
  document.querySelectorAll('.cas-property.open').forEach(renderCasPropertyUsage);
  document.querySelectorAll('.cas-properties-copy span').forEach(label =>
    label.textContent = casPropertyCopyLabel(label.closest('.cas-properties')));
  document.querySelectorAll('.cas-actuator-snippet').forEach(snippet => {
    snippet.hidden = snippet.dataset.format !== format;
  });
}

function initializeCasProperties() {
  document.querySelectorAll('.cas-properties').forEach(enhanceCasProperties);

  document.addEventListener('click', event => {
    const target = event.target;
    const format = target.closest('.cas-properties-format button, .cas-actuator-format button');
    if (format) {
      applyCasPropertyFormat(format.dataset.format);
      return;
    }
    const filter = target.closest('.cas-properties-filter');
    if (filter) {
      const block = filter.closest('.cas-properties');
      block.dataset.filter = filter.dataset.filter;
      block.querySelectorAll('.cas-properties-filter').forEach(button =>
        button.setAttribute('aria-pressed', String(button === filter)));
      applyCasPropertyFilter(block);
      return;
    }
    const copy = target.closest('.cas-property-copy');
    if (copy) {
      copyCasPropertyText(copy.closest('.cas-property-snippet').querySelector('code').textContent, copy);
      return;
    }
    const copyAll = target.closest('.cas-properties-copy');
    if (copyAll) {
      const block = copyAll.closest('.cas-properties');
      copyCasPropertyText(casPropertySnippet(casPropertiesToCopy(block), readCasPropertyFormat(), true), copyAll);
      return;
    }
    const head = target.closest('.cas-properties-enhanced .cas-property-head');
    if (head && !target.closest('a')) {
      toggleCasProperty(head.closest('.cas-property'));
    }
  });

  document.addEventListener('keydown', event => {
    const head = event.target.closest?.('.cas-properties-enhanced .cas-property-head');
    if (head && (event.key === 'Enter' || event.key === ' ')) {
      event.preventDefault();
      toggleCasProperty(head.closest('.cas-property'));
    }
  });

  document.addEventListener('input', event => {
    if (event.target.matches('.cas-properties-search input')) {
      applyCasPropertyFilter(event.target.closest('.cas-properties'));
    }
  });
}

function toggleCasOperation(operation, open) {
  const expand = open ?? !operation.classList.contains('open');
  operation.classList.toggle('open', expand);
  operation.querySelector('.cas-op-row').setAttribute('aria-expanded', String(expand));
  operation.querySelector('.cas-op-detail').hidden = !expand;
}

function selectCasActuatorTab(block, name) {
  block.querySelectorAll('.cas-actuator-tab').forEach(tab => tab.setAttribute('aria-selected', String(tab.dataset.tab === name)));
  block.querySelectorAll('.cas-actuator-panel').forEach(panel => {
    panel.hidden = panel.dataset.tab !== name;
  });
}

function applyCasActuatorFilter(section) {
  const text = (section.querySelector('.cas-actuator-search input')?.value || '').trim().toLowerCase();
  const method = section.querySelector('.cas-actuator-method[aria-pressed="true"]')?.dataset.method || '';
  section.querySelectorAll('.cas-op').forEach(operation => {
    operation.hidden = (method !== '' && operation.dataset.method !== method)
      || (text !== '' && !operation.dataset.text.includes(text));
  });
}

function openCasOperationFromHash() {
  if (!location.hash.startsWith('#actuator-')) {
    return;
  }
  const operation = document.getElementById(decodeURIComponent(location.hash.substring(1)));
  if (operation?.classList.contains('cas-op')) {
    toggleCasOperation(operation, true);
    operation.scrollIntoView({block: 'start'});
  }
}

function initializeCasActuators() {
  if (!document.querySelector('.cas-actuators')) {
    return;
  }
  const format = readCasPropertyFormat();
  document.querySelectorAll('.cas-actuator-format button').forEach(button =>
    button.setAttribute('aria-pressed', String(button.dataset.format === format)));
  document.querySelectorAll('.cas-actuator-snippet').forEach(snippet => {
    snippet.hidden = snippet.dataset.format !== format;
  });

  document.addEventListener('click', event => {
    const target = event.target;
    const row = target.closest('.cas-op-row');
    if (row) {
      toggleCasOperation(row.closest('.cas-op'));
      return;
    }
    const tab = target.closest('.cas-actuator-tab');
    if (tab) {
      selectCasActuatorTab(tab.closest('.cas-actuators'), tab.dataset.tab);
      return;
    }
    const goto = target.closest('.cas-actuator-goto');
    if (goto) {
      const block = goto.closest('.cas-actuators');
      selectCasActuatorTab(block, goto.dataset.tab);
      block.querySelector('.cas-actuator-shared').scrollIntoView({behavior: 'smooth', block: 'start'});
      return;
    }
    const choice = target.closest('.cas-actuator-choice button');
    if (choice) {
      const group = choice.closest('.cas-actuator-choice').dataset.group;
      const panel = choice.closest('.cas-actuator-panel');
      choice.closest('.cas-actuator-choice').querySelectorAll('button').forEach(button =>
        button.setAttribute('aria-pressed', String(button === choice)));
      panel.querySelectorAll(`.cas-actuator-choice-panel[data-group="${group}"]`).forEach(option => {
        option.hidden = option.dataset.choice !== choice.dataset.choice;
      });
      return;
    }
    const method = target.closest('.cas-actuator-method');
    if (method) {
      const section = method.closest('.cas-actuator');
      section.querySelectorAll('.cas-actuator-method').forEach(button =>
        button.setAttribute('aria-pressed', String(button === method)));
      applyCasActuatorFilter(section);
    }
  });

  document.addEventListener('input', event => {
    if (event.target.matches('.cas-actuator-search input')) {
      applyCasActuatorFilter(event.target.closest('.cas-actuator'));
    }
  });

  window.addEventListener('hashchange', openCasOperationFromHash);
  openCasOperationFromHash();
}
