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
  $('#cas-docs-container table').not('.rouge-table, .highlight table').each(function () {
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

function copyPropertyConfig(link) {
  const $td = $(link).closest("td");

  const propertyName = $.trim($td.find("li.property-name code").text());

  const $descriptionP = $td.find("div.property-description p").first();

  const descriptionHtml = $descriptionP.html() || "";
  const description = descriptionHtml
      .replace(/<br\s*\/?>/gi, "\n")
      .replace(/<[^>]+>/g, "")
      .trim();

  let commentLines = description
      .split(/\r?\n/)
      .map(function (line) {
        return $.trim(line);
      })
      .filter(Boolean);

  const $followingUl = $descriptionP.next("ul");

  if ($followingUl.length) {
    $followingUl.find("li").each(function () {
      const itemText = $.trim($(this).text());

      if (itemText) {
        itemText
            .split(/\r?\n/)
            .map(function (line) {
              return $.trim(line);
            })
            .filter(Boolean)
            .forEach(function (line, index) {
              commentLines.push(`${index === 0 ? "- " : "  "}${line}`);
            });
      }
    });
  }

  const comment = commentLines
      .map(function (line) {
        return `# ${line}`;
      })
      .join("\n");

  const textToCopy = `${comment}\n${propertyName}`;

  navigator.clipboard.writeText(textToCopy)
      .then(function () {
        const $status = $(link).siblings(".copy-status");

        $status
            .stop(true, true)
            .text("Copied!")
            .fadeIn(150)
            .delay(1000)
            .fadeOut(150);
      })
      .catch(function (err) {
        console.error("Failed to copy:", err);

        const $status = $(link).siblings(".copy-status");

        $status
            .stop(true, true)
            .text("Copy failed")
            .fadeIn(150)
            .delay(1000)
            .fadeOut(150);
      });
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
