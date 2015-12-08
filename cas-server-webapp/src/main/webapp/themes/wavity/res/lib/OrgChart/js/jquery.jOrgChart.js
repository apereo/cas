/**
 * We load all our functions directly in the loading of the page.
 * @author Valentin Montmirail
 * @date   19/08/2014.
 * @version 0.04
 */
jQuery(document).ready(function() {

    /** ****************************************************************************************************************** **/

    /**
     * In this method, the perform the action when the user use a left-click.
     * so we can expand/constract a node.
     * @param the object where the left-click is performed.
     */
    function leftClick(onSomething) {

        var $this = $(onSomething);

        var $tr = $this.closest("tr");

        /* We add the fadeToggle effect to every extendable node. */
        $tr.nextAll("tr").fadeToggle("fast");

        if($tr.find(".spacesUser").hasClass('contracted')){

            /* If it was constracted and we click, then it becomes expanded. */
            //$this.css('cursor','n-resize');
            $tr.find(".spacesUser").addClass('expanded');
            $tr.find(".spacesUser").removeClass('constracted');
        } else {

            /* If it was expanded and we click, then it becomes constracted. */
            //$this.css('cursor','n-resize');
            $tr.find(".spacesUser").addClass('contracted');
            $tr.find(".spacesUser").removeClass('expanded');
        }

    }

    /** ****************************************************************************************************************** **/

    /**
     * it's possible to already put a tree in HTML directly.
     * This method will construct the tree from the <ul> structure written in HTML.
     */
    $.fn.jOrgChart = function(options) {

        /* We get the options of the organisational tree (like the maximal depth for example) */
        var opts = $.extend({}, $.fn.jOrgChart.defaults, options);

        /* We get the element where we need to append. */
        var $appendTo = $(opts.chartElement);

        return this.each(function() {

            /* To rename and feel more like in PHP. */
            $this = $(this);

            /* We get the container of the final tree. */
            var $container = $("<div class='" + opts.chartClass + "'/>");

            /* If there is a <ul> then we will build a subtree. */
            if($this.is("ul")) {
                buildNode($this.find("li:first"), $container, 0, opts);
            }

            /* If there is a <li> then we will build a node. */
            else if($this.is("li")) {
                buildNode($this, $container, 0, opts);
            }

            /* Then we append the container to our chartElement. */
            $appendTo.append($container);

        });

    };

    /** ****************************************************************************************************************** **/

    /**
     * This method is the container of all the attribute of the organisational tree.
     * So it defines where is the chartElement, what is the chartClass and what is the maximal authorized depth.
     */
    $.fn.jOrgChart.defaults = {

        /* The container of our tree will be the body of the page. (it usefull for the width of the tree) */
        chartElement : 'body',

        /* We define the maximal authorized depth; here it's 5, written in the specifications. */


        /* Then, we define the chartClass for the .css to apply on the tree. */
        chartClass : "jOrgChart",
        depth : -1,
        levels     : 1,
        showLevels : 2,
        stack      : false

    };

    /** ****************************************************************************************************************** **/

    /**
     * @param $node the new node that we want to create
     * @param $appendTo the old node where the new will be attach
     * @param level the depth where the new node will be attach.
     * @param opts the options of the creation.
     * This function will create a node and put it attach to $appendTo
     * We can precise the level of attach and some options.
     */
    function buildNode($node, $appendTo, level, opts) {

        /* The all sub-tree is contain into a <table> */
        var $table = $("<table cellpadding='0' cellspacing='0' border='0'/>");

        /* We get the text of a <tbody> in order to be clean when we will construct for real the tree. */
        var $tbody = $("<tbody/>");

        /* We construct the node row container. */
        var $nodeRow    = $("<tr/>").addClass("node-cells");

        /* We construct the node itself container. */
        var $nodeCell   = $("<td/>").addClass("node-cell").attr("colspan", 2);

        /* We construct the container of all the children. */
        var $childNodes = $node.children("ul:first").children("li");

        /* If our node has children. */
        if($childNodes.length > 1) {

            /* Then we will need a black line to attach every child. */
            $nodeCell.attr("colspan", $childNodes.length * 2);

        }

        /* Get the contents, any markup except <li> and <ul> allowed. */
        var $nodeContent = $node.clone().children("ul,li").remove().end().html();

        $nodeDiv = $("<div>").addClass("node").append($nodeContent);

        /* This allows you to constract and extends nodes when we click on it. */
        $nodeDiv.find(".spacesUser").click(function() {

            leftClick(this);

        });

        /* The cell contains the div */
        $nodeCell.append($nodeDiv);

        /* The row contains the cell */
        $nodeRow.append($nodeCell);

        /* The body contains the row. */
        $tbody.append($nodeRow);

        if($childNodes.length > 0) {

            /* if it can be expanded then change the cursor */
            $nodeDiv.find(".spacesUser").css('cursor','n-resize').addClass('expanded');

            /* We recurse until we found a leave (-1) or until we reached the specified level */
            if(opts.depth == -1|| (level+1 < opts.depth)) {

                var $downLineRow = $("<tr/>");

                var $downLineCell = $("<td/>").attr("colspan", $childNodes.length*2);

                /* We append the line cell to the line row. */
                $downLineRow.append($downLineCell);

                /* We draw the connecting line from the paretnt node to the horizontal line. */
                $downLine = $("<div></div>").addClass("line down");

                $downLineCell.append($downLine);

                $tbody.append($downLineRow);



                if ($childNodes.length > 0) {
                	$this.css('cursor','n-resize');
                    $nodeDiv.addClass("hasChildren");
                    if (opts.showLevels == -1 || level < opts.showLevels-1) {
                        $nodeDiv.addClass("shownChildren");
                    }
                    else {
                        $nodeDiv.addClass("hiddenChildren");
                    }
                    if (opts.interactive) {
                        $nodeDiv.hover(function() {$(this).addClass(opts.hoverClass);}, function() {$(this).removeClass(opts.hoverClass)});
                    }
                }

                /* We draw the horizontal line. */
                var $linesRow = $("<tr/>");

                $childNodes.each(function() {

                    /* The line on the left part of the symetric. */
                    var $left = $("<td/>").addClass("line left top");

                    /* The line on the right part of the symetric. */
                    var $right = $("<td/>").addClass("line right top");

                    /* Then we put the 2 of them to have a line perfectly on the center of the node. */
                    $linesRow.append($left).append($right);

                });

                /* Horizontal line shouldn't extend beyond the first and last child branches. */
                $linesRow.find("td:first").removeClass("top");

                $linesRow.find("td:last").removeClass("top");

                /* We add the line without horizontal line to the tbody. */
                $tbody.append($linesRow);

                var $childNodesRow = $("<tr/>");

                $childNodes.each(function() {

                    var $td = $("<td class='node-container'/>");

                    $td.attr("colspan", 2);

                    /* We recurse through children lists and items */
                    buildNode($(this), $td, level+1, opts);

                    $childNodesRow.append($td);

                });

            }

            $tbody.append($childNodesRow);
        }
        if (opts.showLevels > -1 && level >= opts.showLevels-1) {
            $nodeRow.nextAll("tr").hide();
        }
        $table.append($tbody);

        $appendTo.append($table);

    };

    /* Represent the last clicked object (to perform action on it, like remove it, add a child...) */
    lastClicked = "";

    /** ****************************************************************************************************************** **/

    /** Our <ul> becomes an Organisational Chart. */
    $("#org").jOrgChart( {

        chartElement : '#chart'
    });

    /** ****************************************************************************************************************** **/

    /** When  we click on nodes...*/
    $("#show-list").click(function(e) {

        /* We block the by-default action. */
        e.preventDefault();

        /* We toggle all the structure under our node when we click on it. */
        $('#list-html').toggle('fast', function() {

            ($(this).is(':visible')) ? $(".topbar").fadeTo('fast',0.9) : $(".topbar").fadeTo('fast',1);

        });

    });

    /** ****************************************************************************************************************** **/

   

    /** ****************************************************************************************************************** **/


});

