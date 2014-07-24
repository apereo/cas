/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

var editInnerHTML = "";
var deleteInnerHTML = "";
var currentRow = null;

function swapButtonsForConfirm(rowId, serviceId) {

    resetOldValue();
    var editCell = $("#edit"+rowId);
    var deleteCell = $("#delete"+rowId);

    var row = $("#row" + rowId);
    row.removeClass("over");
    row.addClass("highlightBottom");

    editInnerHTML = editCell.html();
    deleteInnerHTML = deleteCell.html();
    currentRow = rowId;
    
    editCell.html("Really?");
    deleteCell.html("<a id=\"yes\" href=\"deleteRegisteredService.html?id=" + serviceId + "\">Yes</a> <a id=\"no\" href=\"#\" onclick=\"resetOldValue();return false;\">No</a>");
}

function resetOldValue() {
    if (currentRow != null) {
        var curRow = $("#row"+currentRow);
        curRow.removeClass("over");
        curRow.removeClass("highlightBottom");
        var editCell = $("#edit"+currentRow);
        var deleteCell = $("#delete"+currentRow);

        editCell.html(editInnerHTML);
        deleteCell.html(deleteInnerHTML);
       
        editInnerHTML = null;
        deleteInnerHTML = null;
        currentRow = null;
    }
}

function updateRegisteredServiceOrder(movedService, pos) {
	
	var COLUMN_SERVICE_ID = "td1";
	var COLUMN_SERVICE_EVAL_ORDER = "td8";
	
	var rowId = $(movedService).attr('id');	
	
	var serviceId = $('#' + rowId + ' td.' + COLUMN_SERVICE_ID).attr('id');
	var evalOrder = $('#' + rowId + ' td.ac.' + COLUMN_SERVICE_EVAL_ORDER).html();
	
	var targetRow = $(pos).attr('element');
	var relPosition = $(pos).attr('position');
	
	var targetRowId = $(targetRow).attr('id');		
	var targetRowEvalOrder = $('#' + targetRowId + ' td.ac.' + COLUMN_SERVICE_EVAL_ORDER).html();
	
	switch (relPosition) {
		case fluid.position.BEFORE:
			evalOrder = eval(targetRowEvalOrder) - 1;
			break;
		case fluid.position.AFTER:
			evalOrder = eval(targetRowEvalOrder) + 1;
			break;
	}
	
	var result = false;
	$.ajax({
		type: "POST",
		async: false,
		url: "updateRegisteredServiceEvaluationOrder.html",
		data: { id: serviceId, evaluationOrder: evalOrder },
		success: 
			function(data, textStatus, jqXHR) {
				$('#' + rowId + ' td.ac.' + COLUMN_SERVICE_EVAL_ORDER).html(evalOrder);
				result = true;
			},
		error: 
			function(jqXHR, textStatus, errorThrown) {
				$("#errorsDiv").show();
				console.log(data.error);
			}
	});
	return result;
}

$(document).ready(function () {
	$("#errorsDiv").hide();
	if (typeof fluid != 'undefined') {
    	var opts = {
    		selectors: {
    			movables: "tr"
    		},
    		listeners: {
    			onMove: updateRegisteredServiceOrder,
    			onBeginMove: function() {
    				$("#errorsDiv").hide();
    			},
    			afterMove: function() {
    				$("#fluid-ariaLabeller-liveRegion").remove();
    			},
    			onHover: function(item, state) {
    				$(item).css('cursor', state ? 'move' : 'auto');
    			}
    		}
    	};
	
        return fluid.reorderList("#tableWrapper #scrollTable tbody", opts);
    }
    return;
});