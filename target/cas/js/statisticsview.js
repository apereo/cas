var Gauge = function (wrapper, percent, options) {
    if (!wrapper || !percent) {
        //console.error('wrapper and percentage are required.  Please check your code.');
        return;
    }

    var label = (!options.label) ? '' : options.label;

    var textClass = options.textClass || 'progress-meter';

    var width = options.width || 200,
        height = options.height || 200,
        twoPi = 2 * Math.PI,
        progress = 0,
        total = 100,
        formatPercent = d3.format(".0%");

    var colorScale = d3.scale.linear()
        .domain([0, 0.40, 0.50, 1])
        .range(["green", "green", "goldenrod", "red"]);

    var arc = d3.svg.arc()
            .startAngle(0)
            .innerRadius(width * 0.4)
            .outerRadius(width * 0.5)
        ;

    var svg = d3.select(wrapper).append("svg")
        .attr("width", width)
        .attr("height", height)

        .attr('fill', '#2E7AF9')
        .append("g")
        .attr("transform", "translate(" + width / 2 + "," + height / 2 + ")");

    var meter = svg.append("g")
        .attr("class", textClass);

    meter.append("path")
        .attr("class", "background")
        .attr("d", arc.endAngle(twoPi));

    var foreground = meter.append("path")
        .attr("class", "foreground");

    var text = meter.append("text")
        .attr("text-anchor", "middle");

    var text2 = meter.append("text")
        .attr('y', height * 0.15)
        .attr("text-anchor", "middle")
        .attr("class", "text2");

    text2.text(label);

    var animate = function (percentage) {
        var i = d3.interpolate(progress, percentage);

        foreground.transition().duration(2000)
            .tween("progress", function () {

                return function (t) {
                    progress = i(t);

                    foreground.style('fill', colorScale(progress));
                    foreground.attr("d", arc.endAngle(twoPi * progress));
                    text.text(formatPercent(progress));
                };
            });
    };

    // init
    (function () {
        setTimeout(function () {
            animate(percent);
        }, 500);
    })();

    return {
        update: function (newPercent) {
            animate(newPercent);
        }
    };
};


function upTime(countTo, el) {
    var wrapper = document.getElementById('uptime-panel');
    var element = document.getElementById(el);
    var now = new Date();
    countTo = new Date(countTo);
    var difference = (now - countTo);

    var days = Math.floor(difference / (60 * 60 * 1000 * 24) * 1);
    var hours = Math.floor((difference % (60 * 60 * 1000 * 24)) / (60 * 60 * 1000) * 1);
    var mins = Math.floor(((difference % (60 * 60 * 1000 * 24)) % (60 * 60 * 1000)) / (60 * 1000) * 1);
    var secs = Math.floor((((difference % (60 * 60 * 1000 * 24)) % (60 * 60 * 1000)) % (60 * 1000)) / 1000 * 1);

    clearTimeout(upTime.to);

    if (isNaN(days) || isNaN(hours) || isNaN(mins) || isNaN(secs) ) {
        wrapper.style.display = 'none';
    } else {
        days = (days == 1) ? days + ' day ' : days + ' days ';
        hours = (hours == 1) ? hours + ' hour ' : hours + ' hours ';
        mins = (mins == 1) ? mins + ' minute ' : mins + ' minutes ';
        secs = (secs == 1) ? secs + ' second ' : secs + ' seconds';

        var timeString = '<span class="upTime">' + days + hours + mins + secs + '</span>';
        element.innerHTML = timeString;
        wrapper.style.display = 'block';

        upTime.to = setTimeout(function() {
            upTime(countTo, el);
        },1000);

    }
}


// Fill modal with content from link href
$("#threadDumpModal").on("show.bs.modal", function (e) {
    var link = $(e.relatedTarget);
    $(this).find(".modal-body pre").load(link.val());
});

/**
 * Thread Dump Preview
 * Returnes the xx amount of characters from the end of the thread dump for preview sake.
 * The length can be changed by passing ini a value, otherwise it defaults to 400.
 */
function getThreadDumpPreview(len) {
    var len = len || 400;
    $.get($('#threadDumpViewButton').val(), function (data) {
        $('#threadDumpPreview').html(data.substr(-len));
    });
}

