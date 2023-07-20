var CONST_CURRENT_VER = "development";


function isDocumentationSiteViewedLocally() {
  return location.href.startsWith("http://localhost:4000");
}

function generateNavigationBarAndCrumbs() {
  var crumbs = "<ol class='breadcrumb'>";

  var activeVersion = getActiveDocumentationVersionInView(true);

  var uri = new URI(document.location);
  var segments = uri.segment();

  for (var i = 1; i < segments.length; i++) {
    var clz = ((i + 1) >= segments.length) ? 'breadcrumb-item active' : 'breadcrumb-item ';
    clz += "capitalize";

    var page = null;

    if ((i + 1) >= segments.length) {
      page = document.title.replace("CAS -", "").trim();
    } else {
      page = segments[i].replace(".html", "").replace(/-/g, " ").replace(/_/g, " ").replace(/index/g, "");
    }
    crumbs += "<li class='" + clz + "'><a href='#'>" + page + "</a></li>";
  }
            
  crumbs += "<li><div id='searchField' class></div></li></ol>";
  $("#docsNavBar").prepend(crumbs);
}

function getActiveDocumentationVersionInView(returnBlankIfNoVersion) {
  var currentVersion = CONST_CURRENT_VER;
  var href = location.href;
  var index = isDocumentationSiteViewedLocally() ? href.indexOf("4000/") : -1;

  if (index == -1) {
    var uri = new URI(document.location);

    if (uri.filename() != uri.segment(1) && uri.segment(1) != "developer") {
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
  let prefix = isDocumentationSiteViewedLocally() ? "/" : "/cas/";
  $.get(prefix + getActiveDocumentationVersionInView() + "/sidebar.html", function (data) {
    var menu = $(data);

    if (menu.first().is('ul')) {

      menu.addClass('nav flex-column').attr('id', 'sidebarTopics');

      var topLevel = menu.find('> li>a');

      var topLevelUl = menu.find('> li>ul');

      var subLevel = menu.find('> li ul');

      var nestedMenu = menu.find("ul li").has("ul").children('a');

      topLevel.each(function () {
        var el = $(this);
        //console.log("Top level: " + el);
        sidebarTopNav(el);
      });

      topLevelUl.each(function () {
        var el = $(this);
        //console.log("Top level UL: " + el);
        el.attr({
          'data-bs-parent': '#sidebarTopics'
        });

        if (!el.prev().hasClass('collapsed')) {
          el.addClass('show');
        };
      });

      subLevel.each(function () {
        //console.log("Sub level: " + this);
        sidebarSubNav($(this));
      });

      nestedMenu.each(function () {
        //console.log("Sub level nested: " + this);
        sidebarTopNav($(this));
      });

      $('#sidebar').append(menu);

      generateSidebarLinksForActiveVersion();

      var uri = new URI(document.location);
      if (uri.filename() === "index.html" || uri.filename() === '') {
        return;
      }

      let count = 0;
      let element = $("#sidebarTopics a[href*='/" + uri.filename() + "']");
      let parent = element.parent();
      while (parent != null && parent != undefined) {
        // parent.collapse('toggle');
        let id = parent.attr("id");
        if (id === "sidebarTopics" || count >= 10) {
          break;
        }
        if (id != undefined) {
          parent.collapse('show');
        }
        count++;
        parent = parent.parent();
      }
      element.css("font-weight", "bold").addClass("text-info");
      let txt = element.text();
      element.prepend("<i class='fa fa-angle-double-right'></i>&nbsp;");

      if (uri.fragment() == null || uri.fragment() === "") {
        setTimeout(function(){
          let top = $(element).offset().top;
          // console.log("Element top position: " + top);
          let offset = top <= 200 ? 30 : 150;
          $("#sidebar").animate({scrollTop: offset }, 1000);
        }, 100);
      }
    }
  });
}

function sidebarTopNav(el) {
  // If the link is an anchor, then wire up toggle functionality, otherwise leave it.
  if (el.attr('href').search(/(?:^|)#/g) >= 0) {
    el.attr({
      'data-bs-toggle': "collapse",
      'aria-expanded': "false",
      title: $(this)[0].innerText,
      class: 'collapsed'
    })
    .append('<i class="expand"></i></a>');
  }

  if (pageSection && el.text() == pageSection) {
    el.removeClass('collapsed')
  }

};


function sidebarSubNav(el) {
  var prevId = $(el).prev('a').attr('href');

  if (prevId.search(/^#.*$/) >= 0) {
    prevId = prevId.substr(1);
  } else {
    prevId = '';
  }

  if (!prevId == '') {
    $(el).addClass('nav flex-column collapse subnav ms-3').attr('id', prevId);
  } else {
    $(el).addClass('nav flex-column subnav ms-3');
  }
}

function generateSidebarLinksForActiveVersion() {
  let prefix = isDocumentationSiteViewedLocally() ? "" : "cas/";

  $('#sidebar a').each(function () {
    var href = this.href;
    if (href.indexOf("$version") != -1) {
      href = href.replace("$version", "cas/" + getActiveDocumentationVersionInView());
    }
    
    if (href.includes("#")) {
      href = href.substring(href.indexOf("#"));
    } else if (isDocumentationSiteViewedLocally() && href.includes("http://localhost")) {
      href = href.replace("/cas", "");
    }
    $(this).attr('href', href);
  });
}

function toggleDarkMode() {
    let theme = $("html").attr("data-bs-theme");
    console.log("Current theme: " + theme);
    if (theme === "dark") {
      $("html").attr('data-bs-theme', 'light');
    } else {
      $("html").attr('data-bs-theme', 'dark');
    }
}

function generateToolbarIcons() {
  var CAS_REPO_URL_GITHUB = $('#forkme_banner').attr('href');
  var activeVersion = getActiveDocumentationVersionInView(true);

  var uri = new URI(document.location);
  var segments = uri.segment();
  var page = "";

  for (var i = isDocumentationSiteViewedLocally() ? 0 : 1; i < segments.length; i++) {
    page += segments[i] + "/";
  }
  editablePage = page.replace(".html", ".md");
  editablePage = editablePage.replace(CONST_CURRENT_VER, "")
  editablePage = editablePage.replace(activeVersion, "")
  if (editablePage == "") {
    editablePage = "index.md";
  }

  $('#toolbarIcons').append("<a href='javascript:toggleDarkMode()'><i class='fa fa-moon' title='See this page running on localhost'></i></a>");

  let href = location.href.replace("https://apereo.github.io/cas", "http://localhost:4000");
  $('#toolbarIcons').append("<a href='" + href + "'><i class='fab fa-codepen' title='See this page running on localhost'></i></a>");

  if (activeVersion != CONST_CURRENT_VER && activeVersion != "") {
    let prefix = isDocumentationSiteViewedLocally() ? "/" : "/cas/";
    var linkToDev = prefix + page.replace(activeVersion, CONST_CURRENT_VER).replace("//", "/");
    linkToDev = linkToDev.replace("html/", "html");

    $('#toolbarIcons').append("<a href='" + linkToDev +
      "'><i class='fa fa-code' title='See the latest version of this page'></i></a>");
  }
 
  var baseLink = CAS_REPO_URL_GITHUB;
  var editLink = "";
  var historyLink = "";
  var deleteLink = "";

  if (activeVersion == "") {
    editLink = baseLink + "/edit/gh-pages/";
    historyLink = baseLink + "/commits/gh-pages/";
    deleteLink = baseLink + "/delete/gh-pages/";
  } else if (activeVersion == CONST_CURRENT_VER) {
    editLink = baseLink + "/edit/master/docs/cas-server-documentation/";
    historyLink = baseLink + "/commits/master/docs/cas-server-documentation/";
    deleteLink = baseLink + "/delete/master/docs/cas-server-documentation/";
  } else if (activeVersion.indexOf("5.") != -1 || activeVersion.indexOf("6.") != -1) {
    editLink = baseLink + "/edit/" + activeVersion + "/docs/cas-server-documentation/";
    historyLink = baseLink + "/commits/" + activeVersion + "/docs/cas-server-documentation/";
    deleteLink = baseLink + "/delete/" + activeVersion + "/docs/cas-server-documentation/";
  } else if (activeVersion != CONST_CURRENT_VER) {
    editLink = baseLink + "/edit/" + activeVersion + "/cas-server-documentation/";
    historyLink = baseLink + "/commits/" + activeVersion + "/cas-server-documentation/";
    deleteLink = baseLink + "/delete/" + activeVersion + "/cas-server-documentation/";
  }

  editLink += editablePage;


  $('#toolbarIcons').append("<a target='_blank' href='" + editLink +
    "'><i class='fa fa-pencil-alt' title='Edit with Github'></i></a>");

  historyLink += editablePage;


  $('#toolbarIcons').append("<a target='_blank' href='" + historyLink +
    "'><i class='fa fa-history' title='View commit history on Github'></i></a>");

  deleteLink += editablePage;


  $('#toolbarIcons').append("<a target='_blank' href='" + deleteLink +
    "'><i class='fa fa-times' title='Delete with Github'></i></a>");
}

function generatePageTOC() {
  var toc = $('#tableOfContents ul');
  var page_contents = $('#pageContents ul');
  var arr = [];

  var headings = $('#cas-docs-container').find('h1, h2,h3');
  var subMenu = false;
  var subMenuId = null;

  headings.each(function (idx) {
    if ($(this).is('h1,h2')) {
      // If it is a H2 and the submenu flag is NOT set, then arr.push('<li>h2 text')
      if (!subMenu) {
        arr.push(tocItem(this.id, this.textContent));
      }

      // If it is a H2 and the submenu flag is set, then arr.push('</ul><li>h2 text')
      if (subMenu) {
        subMenu = false;
        arr.push('</ul></li>');
        arr.push(tocItem(this.id, this.textContent));
      }
    }
    ; // End H2

    if ($(this).is('h3')) {
      // If it is a H3 and the submenu flag is NOT set, then set the submenu flag then arr.push('<ul><li>h3 text</li>')
      if (!subMenu) {
        subMenu = true;
        arr.push('<ul class="nav flex-column">');
        arr.push(tocItem(this.id, this.textContent));
      } else if (subMenu) {
        arr.push(tocItem(this.id, this.textContent));
      }
    }
    ; // End H2
  });

  // After the loop, close the last <li> tag
  if (subMenu) {
    arr.push('</ul></li>');
  } else {
    arr.push('</li>');
  }

  // toc.append(arr.join(''));
  page_contents.append(arr.join(''));
}

function tocItem(id, text) {
  return '<li class="toc-entry toc-h2"><a href="#' + id + '">' + text + '</a>';
}


function guidGenerator() {
  var S4 = function () {
    return (((1 + Math.random()) * 0x10000) | 0).toString(16).substring(1);
  };
  return (S4() + S4() + "-" + S4() + "-" + S4() + "-" + S4() + "-" + S4() + S4() + S4());
}


function responsiveImages() {
  $('img').each(function () {
    $(this).addClass('img-fluid');
  });
}

function responsiveTables() {
  $('table').each(function () {
    $(this).addClass('table table-responsive');
  });
}


function enableBootstrapTooltips() {
  $('[data-bs-toggle="tooltip"]').tooltip();
}

function generateOverlay(artifactId, type) {
  var id = artifactId.replace("cas-server-", "")

  $("#overlayform").remove();
  $('body').append(" \
  <form id='overlayform' action='https://casinit.herokuapp.com/starter.zip' method='post'> \
    <input type='submit' value='submit' /> \
    <input type='hidden' name='dependencies' value='" + id + "' /> \
    <input type='hidden' name='type' value='" + type + "' /> \
  </form>");
  $("#overlayform").submit();
}

function initializePage() {
    new ClipboardJS('.copy-button');

    let tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'))
    let tooltipList = tooltipTriggerList.map(function (tooltipTriggerEl) {
      return new bootstrap.Tooltip(tooltipTriggerEl)
    });

    let activeVersion = getActiveDocumentationVersionInView(true);
    let filters = ["version: " + activeVersion];
    console.log("Documentation search is filtering by " + filters);    

    docsearch({
      apiKey: 'a95d9cc5493147925fb5d4fdb5afb414',
      appId: 'IW4GLK9JZ0',
      indexName: 'apereoapereo',
      container: '#searchField',
      searchParameters: { 'facetFilters': filters },
      debug: true
    });
}

$(function () {
  loadSidebarForActiveVersion();
  generatePageTOC();
  generateToolbarIcons();
  generateNavigationBarAndCrumbs();

  responsiveImages();
  responsiveTables();

  var formattedVersion = getActiveDocumentationVersionInView();
  if (formattedVersion != "" && formattedVersion.indexOf(CONST_CURRENT_VER) == -1) {
    formattedVersion = " (" + formattedVersion + ")"
  } else {
    formattedVersion = "";
  }

  enableBootstrapTooltips();
  initializePage();
});


$(function () {
  return $("h2, h3, h4, h5, h6").each(function (i, el) {
    var $el, icon, id;
    $el = $(el);
    id = $el.attr('id');
    icon = '<i class="fa fa-link"></i>';
    if (id) {
      return $el.prepend($("<a />").addClass("header-link").attr("href", "#" + id).html(icon));
    }
  });
});


let codes = document.querySelectorAll('.highlight > pre > code .rouge-code pre');
let countID = 0;
codes.forEach((code) => {

  code.setAttribute("id", "code" + countID);
  
  let btn = document.createElement('button');
  btn.innerHTML = "Copy";
  btn.className = "btn-copy-code";
  btn.setAttribute("data-clipboard-action", "copy");
  btn.setAttribute("data-clipboard-target", "#code" + countID);
  btn.setAttribute("onclick", "this.innerHTML='Copied';")

  let div = document.createElement('div');
  div.className = "div-code-button";
  div.appendChild(btn);
  
  code.before(div);
  countID++;
}); 
new ClipboardJS('.btn-copy-code');

$(document).ready(() => {
    var pageLength = $('.cas-datatable').data('page-length');
    if (pageLength === null || pageLength === undefined || pageLength === "") {
      pageLength = 5;
    }
    $('.cas-datatable').DataTable({
      "processing": true,
      "lengthMenu": [ 5, 10, 15, 25, 50],
      "pageLength": pageLength
    });

    var popoverTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="popover"]'))
    var popoverList = popoverTriggerList.map(function (popoverTriggerEl) {
      return new bootstrap.Popover(popoverTriggerEl)
    })
});

let ROWS = 5;

function next(id) {

  let rows = $(`#${id} tbody tr`);
  let rowCount = rows.length;

  let s = 0;
  s = rows.attr("start");
  if (s == undefined) {
    s = 0;
  } else {
    if (parseInt(s) + ROWS < rowCount) {
      s = parseInt(s) + ROWS;
    }
  }

  let e = 0;
  e = rows.attr("end");
  if (e == undefined) {
    e = ROWS;
  } else {
    e = parseInt(e) + ROWS;
  }
  if (e > rowCount) e = rowCount;


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

  start = start - ROWS;
  if (start < 0) start = 0;
  end = end - ROWS;
  if (end < ROWS) end = ROWS;
  if (end - start < ROWS) end = start + ROWS;

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
    Array.prototype.forEach.call(lis, li => {
        li.classList.remove('active');
    });
};

const getChildPosition = element => {
    var parent = element.parentNode;
    var i = 0;
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
      if (property == false) {
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
            
            let tabs = $('#' + tabContentId + " ul.nav.nav-tabs li a").not("[href^='#notes']");
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
