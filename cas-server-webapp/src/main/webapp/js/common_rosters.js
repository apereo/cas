// $Id$

var W3C_DOM = ((typeof document.getElementById != 'undefined') && (typeof document.createElement != 'undefined')) ? true : false;

// FireBug (Firefox extension) function to print javascript debug messages to console
function printfire()
{
    if (document.createEvent)
    {
        printfire.args = arguments;
        var ev = document.createEvent("Events");
        ev.initEvent("printfire", false, true);
        dispatchEvent(ev);
    }
}

// Add events to onload event handler
// typeof can be "string", "number", "boolean", "object", "array", "function", or "undefined"
function addLoadEvent(fn)
{
	// Mozilla
	if (typeof window.addEventListener != 'undefined')
	{
		window.addEventListener('load', fn, false);
	}
	// Opera
	else if (typeof document.addEventListener != 'undefined')
	{
		document.addEventListener('load', fn, false);
	}
	// IE
	else if (typeof window.attachEvent != 'undefined')
	{
		window.attachEvent('onload', fn);
	}
	// IE5Mac and others that don't support the above methods
	else
	{
		var oldfn = window.onload;
		if (typeof window.onload != 'function')
		{
			window.onload = fn;
		}
		else
		{
			window.onload = function()
			{
				oldfn();
				fn();
			};
		}
	}
}

// Add className to class attribute
function addClass(target, classValue)
{
	var pattern = new RegExp("(^| )" + classValue + "( |$)");

	if (!pattern.test(target.className))
	{
		if (target.className == "")
		{
			target.className = classValue;
		}
		else
		{
			target.className += " " + classValue;
		}
	}
	
	return true;
}

// Remove className from class attribute
function removeClass(target, classValue)
{
	var removedClass = target.className;
	var pattern = new RegExp("(^| )" + classValue + "( |$)");

	removedClass = removedClass.replace(pattern, "$1");
	removedClass = removedClass.replace(/ $/, "");
	target.className = removedClass;
	
	return true;
}

// Return a reference to element
function getRef(el) 
{
	if(typeof el == "string")
	{
		return document.getElementById(el);
	}
	else if(typeof el == "object")
	{
		return el;
	}
	else return null;
}
/* DEPRECATED - replaced with getElementsByAttribute()
// Return an array of elements with a particular class attribute
function getElementsByClassName(parentNode, tagname, classname)
{
	// printfire("************* start getElementsByClassName()");
	var aEls = getRef(parentNode).getElementsByTagName(tagname);
    var a = [];
    var re = new RegExp('(^| )'+classname+'( |$)');
  //  var els = (getRef(parentNode)) ?  getRef(parentNode).getElementsByTagName(tagname) : (parentNode.getElementsByTagName(tagname)) ? parentNode.getElementsByTagName(tagname) : document.getElementById(parentNode.id).getElementsByTagName(tagname);
    
    for(var i=0; i < aEls.length; i++)
    {
		// printfire("************* for to test for className");
    	if(re.test(aEls[i].className))
    	{
    		// printfire("##### add element to array of class elements");
    		a.push(aEls[i]);
    	}
    }
    
    return a;
}
*/

/* see if following can be used in row highlighting
document.onclick = function(e)
{
	var target = e ? e.target : window.event.srcElement;

	while (target && !/^(a|body)$/i.test(target.nodeName))           while (target && !/^(tr)$/i.test(target.nodeName))
	{
		target = target.parentNode;
	}

	if (target && target.getAttribute('rel') && target.rel == 'external')           if (target && target.getAttribute('class') && target.className == 'external')
	{
		var external = window.open(target.href);
		return external.closed;
	}
}

Using a single, document-wide event handler is the most efficient approach 
-- it's much better than iterating through all the links and binding a handler 
to each one individually. We can find out which element was actually clicked by 
referencing the event target property.

*/

// Find all tables with highlight class and on highlight table mouseover trigger highlightRows function
function initHighlightTables()
{
	if(!W3C_DOM)return;
	
	// printfire("- START initHighlightTables()");

	var tables = getElementsByAttribute("table","class","highlight");

	for (var i=0; i < tables.length; i++)
	{
		// printfire("--- assign onmousover highlightRows event handler to table");
		tables[i].onmouseover = highlightRows;
	}
	// printfire("- END initHighlightTables()");
}

function highlightRows()
{
	// printfire("- START highlightRows()");
	var tableRows = this.getElementsByTagName('tr');

	for(var i = 0; i < tableRows.length; i++)
	{
		// printfire("--- row onmouseover and onmouseout highlight");
		tableRows[i].onmouseover = function(e)
		{	
			addClass(this," over");

			// cancel event bubbling to prevent onmouseover of row to trigger unnecessary firing of table onmouseover
			if (typeof e == "undefined")
			{
				e = window.event; // IE
				e.cancelBubble = true;
			}
			else if(e)
			{
				if(e.stopPropagation) e.stopPropagation();
			}
			else e = null;
		}
		
		tableRows[i].onmouseout = function()
		{
			removeClass(this,"over");
			// this.className = this.className.replace(new RegExp(/over/g), "");
		}
	}
	// printfire("- END highlightRows()");
}
addLoadEvent(initHighlightTables);

// transition effect to fade background of element from darker to lighter color
// could use var redBackground = new initArray(12);redBackground[0]="#33CC00"; format
function setbgColor(elId, r, g, b){
	getRef(elId).style.backgroundColor = "rgb("+r+","+g+","+b+")";
}

function fade(elId, sr, sg, sb, er, eg, eb, step, current, speed){
	// printfire("----- START fade()");
	if (current <= step){
		setbgColor(elId,Math.floor(sr * ((step-current)/step) + er * (current/step)),Math.floor(sg * ((step-current)/step) + eg * (current/step)),Math.floor(sb * ((step-current)/step) + eb * (current/step)));
		current++;
		setTimeout("fade('"+elId+"',"+sr+","+sg+","+sb+","+er+","+eg+","+eb+","+step+","+current+","+speed+")",parseInt(speed));
	}
	// printfire("----- END fade()");
}

function fadeIn(){
	if(!W3C_DOM)return;
	// printfire("- START fadeIn()");
	if(getRef('msg')) fade('msg', 51,204,0, 221,255,170, 30,1,20);
	if(getRef('status')) fade('status', 187,0,0, 255,238,221, 30,1,20);
	// printfire("- END fadeIn()");
}
addLoadEvent(fadeIn);

// Handle confirmation of grade submission
function confirmSectionSubmission()
{
	if(!W3C_DOM) return;
	// printfire("- START confirmSectionSubmission()");
	if(!document.getElementById("roster-view")) return;

	// printfire("--- roster-view exists");

	var rosterForms = document.getElementsByTagName("form");
	
	// printfire("--- loop through "+ rosterForms.length +" FORMS to assign onsubmit event handler");
	
	for(var i=0; i < rosterForms.length; i++)
	{	
		// printfire("--- loop : "+ (i+1));
		rosterForms[i].onsubmit = function()
		{
			if(confirm("Are you sure you want to submit this section?\n Click \"OK\" to submit section.\n Click \"Cancel\" to continue working.")) return true;
			return false;
		}
	}
	// printfire("- END confirmSectionSubmission()");
}
addLoadEvent(confirmSectionSubmission);


// 0,8,9,16,17,18,37,38,39,40,46 8 - BACKSPACE; 9 - TAB; 13 - ENTER; 16 - SHIFT; 46 - DELETE;
function autoTab(e) 
{
	var keycode;
	
	if (typeof e == "undefined")
	{
		e = window.event; // IE
		keycode = e.keyCode;
	}
	else if (e)
	{
		keycode = e.which;
	}
	
	// if(window.event){var keycode = window.event.keyCode;}
	// else if (e){var keycode = e.which;}

	// change the maxlength of the unit field based on the first character input
	if((this.id == "unit" || this.id == "section") && this.value.length == 1)
	{
		// if letter then maxlength is 3
		if(keycode > 64 && keycode < 91)
		{
			this.maxLength = 3;
		}
		// if number then maxlength is 2
		else if((keycode > 48 && keycode < 58) || (keycode > 95 && keycode < 106))
		{
			this.maxLength = 2;
		}
	}

	if(this.value.length == this.maxLength && keycode != 8 && keycode != 9 && keycode != 16 && keycode !=17 && keycode != 18 && keycode != 46)
	{
		var focusEl = findNextElement(this.getAttribute("tabindex"),this.form.id);
		focusEl.focus();
	}
}

function findNextElement(tabIndx,formId) 
{
	var aEls = document.forms[formId].elements;

	for(var i = 0; i < aEls.length; i++) {
		var el = aEls[i];
		if(el.getAttribute("tabindex") == (parseInt(tabIndx)+1)) {
			return el;
		}
	}
	return aEls[0];
}

function initAutoTab(){
	if(!W3C_DOM)return;
	
	// printfire("- START initAutoTab()");
	
	if(!document.getElementById("roster-view") && document.getElementsByTagName("input").length > 0 && getElementsByAttribute("table","class","search").length > 0)
	{
		// printfire("--- there are inputs and search tables ");

		var aAutoTabEls = new Array();
		aAutoTabEls = getElementsByAttribute("input","class","autoTab");
		

		for(var i = 0; i < aAutoTabEls.length; i++)
		{
			// printfire("--- assign onkeyup to field");
			aAutoTabEls[i].onkeyup = autoTab;
		}
	}
	// printfire("- END initAutoTab()");
}
addLoadEvent(initAutoTab);

// Form submission onchange of select - classRostersDrillDown.jsp
function doSubmitForm(sourceElement, newAction){
	var oForm = sourceElement.form;
	oForm.action = newAction;
	oForm.submit();
}

function initOnChangeSubmit(){
	// printfire("- START initOnChangeSubmit()");
	
	if(!document.getElementById("roster-view"))
	{
		var aEls = getElementsByAttribute("select","class","submit");
	
		for (var i = 0; i < aEls.length; i++) 
		{
			aEls[i].onchange = function ()
			{
				doSubmitForm(this,"drillDown.htm?activeTab=t2");
			}
		}
	}
	
	// printfire("- END initOnchange()");
}
addLoadEvent(initOnChangeSubmit);



function showPhoto(eA)
{	
	// create and attach a new image to clicked link
	if(eA.childNodes.length < 2) // prevent subsequent clicks from creating more images
	{
	    var eIMG = createPhoto(eA);
	    eA.style.position = "relative"; // set clicked link position to establish new positioning context for child nodes
		eA.style.textDecoration = "none";
		eA.style.color = "purple";
	
		// append new image to the clicked link in document
		eA.appendChild(eIMG);
	}
	else if (eA.childNodes.length == 2)
	{
		eA.parentNode.parentNode.className = eA.parentNode.parentNode.className.replace(new RegExp(/over/g), "");
		eA.removeChild(eA.lastChild);
		eA.blur();
	}

	// remove the image when active link is defocused
	eA.onblur = function()
	{
		eA.style.position = "static"; // IE needs this to keep links below new images

		// check if the link has more than 1 child (link text) and remove extras (images)
		while(eA.childNodes.length > 1)
		{
			eA.removeChild(eA.lastChild);
		}
	}

	return false;
}

function createPhoto(eA)
{
	// create new image element in memory
	var eIMG = document.createElement("img");

	// set image attributes and styles
	eIMG.src = eA.href;
	eIMG.style.position = "absolute";
	eIMG.style.left = 0;
	eIMG.style.top = "1.5em";
	eIMG.style.display = "block";
	eIMG.style.width = "200px";
	eIMG.style.height = "200px";
	eIMG.style.zIndex = 100;
	return eIMG;
}

function getElementsByAttribute(elementType, attribute, attributeValue)
{
	// printfire("----- START getElementsByAttribute()");
	var elementArray = new Array();
	var matchedArray = new Array();
	
	
	if (elementType != null || elementType != "")
	{
		elementArray = document.getElementsByTagName(elementType);
	}
	else if (document.all)
	{
		elementArray = document.all;
	}
	else
	{
		elementArray = document.getElementsByTagName("*");
	}

	// printfire("-------- loop " + elementArray.length + " times through "+elementType+ " collection to find ones with " + attribute +" attribute of " + attributeValue);
	for (var i = 0; i < elementArray.length; i++)
	{
		// printfire("-------- loop : "+ (i+1));
		
		if (attribute == "class")
		{
			var pattern = new RegExp("(^| )" + attributeValue + "( |$)");

			if (pattern.test(elementArray[i].className))
			{
				matchedArray[matchedArray.length] = elementArray[i];
			}
		}
		else if (attribute == "for")
		{
			if (elementArray[i].getAttribute("htmlFor") || elementArray[i].getAttribute("for"))
     		{
				if (elementArray[i].htmlFor == attributeValue)
				{
					matchedArray[matchedArray.length] = elementArray[i];
				}
			}
		}
		else if (elementArray[i].getAttribute(attribute) == attributeValue)
		{
			matchedArray[matchedArray.length] = elementArray[i];
		}
	}
	// printfire("-------- found "+ matchedArray.length +" matching " + elementType + "s");
	// printfire("----- END getElementsByAttribute()");
	return matchedArray;
}