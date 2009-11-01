// $Id$

var W3C_DOM = ((typeof document.getElementById != 'undefined') && (typeof document.createElement != 'undefined')) ? true : false;

var editInnerHTML = "";
var deleteInnerHTML = "";
var currentRow = null;

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

function init(){
    var firstForm = document.forms[0];

    if (firstForm != null) {
        var firstElement = firstForm.elements[0];
        if (firstElement != null) {
            firstElement.focus();
            firstElement.select();
        }
    }
}

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
    if(getRef('msg')) fade('msg', 51,204,0, 221,255,170, 30,1,20);
    if(getRef('status')) fade('status', 187,0,0, 255,238,221, 30,1,20);
    
    var arrayElements = getElementsByAttribute("tr", "class", "added");
    
    if (arrayElements.length > 0) {
        fade(arrayElements[0].id, 255,255,51, 255,255,255, 30, 1, 70)
    }
}
addLoadEvent(fadeIn);

function swapButtonsForConfirm(rowId, serviceId) {

    resetOldValue();
    var row = document.getElementById("row"+rowId);
    var editCell = document.getElementById("edit"+rowId);
    var deleteCell = document.getElementById("delete"+rowId);
    
    removeClass(row, "over");
    addClass(row, "highlightBottom");
    
    editInnerHTML = editCell.innerHTML;
    deleteInnerHTML = deleteCell.innerHTML;
    currentRow = rowId;
    
    editCell.innerHTML = "Really?";
    deleteCell.innerHTML = "<a id=\"yes\" href=\"deleteRegisteredService.html?id=" + serviceId + "\">Yes</a> <a id=\"no\" href=\"#\" onclick=\"resetOldValue();return false;\">No</a>";
}

function resetOldValue() {
    if (currentRow != null) {
        removeClass(document.getElementById("row"+currentRow), "over");
        removeClass(document.getElementById("row"+currentRow), "highlightBottom");
        var editCell = document.getElementById("edit"+currentRow);
        var deleteCell = document.getElementById("delete"+currentRow);
        var row = document.getElementById("row"+currentRow);
       
        editCell.innerHTML = editInnerHTML;
        deleteCell.innerHTML = deleteInnerHTML;
       
        editInnerHTML = null;
        deleteInnerHTML = null;
        currentRow = null;
    }
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
