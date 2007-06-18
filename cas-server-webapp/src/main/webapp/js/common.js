var highlightcolor="#fc3";
var ns6=document.getElementById&&!document.all;
var previous='';
var eventobj;

// SET FOCUS TO FIRST ELEMENT AND HIDE/SHOW ELEMENTS IF JAVASCRIPT ENABLED


// REGULAR EXPRESSION TO HIGHLIGHT ONLY FORM ELEMENTS
	var intended=/INPUT|TEXTAREA|SELECT|OPTION/

// FUNCTION TO CHECK WHETHER ELEMENT CLICKED IS FORM ELEMENT
	function checkel(which){
		if (which.style && intended.test(which.tagName)){return true}
		else return false
	}

// FUNCTION TO HIGHLIGHT FORM ELEMENT
	function highlight(e){
		if(!ns6){
			eventobj=event.srcElement
			if (previous!=''){
				if (checkel(previous))
				previous.style.backgroundColor=''
				previous=eventobj
				if (checkel(eventobj)) eventobj.style.backgroundColor=highlightcolor
			}
			else {
				if (checkel(eventobj)) eventobj.style.backgroundColor=highlightcolor
				previous=eventobj
			}
		}
	}

