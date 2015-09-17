/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
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

var Gauge = function (wrapper, percent, options) {
    if (!wrapper || !percent) {
        console.error('wrapper and percentage are required.  Please check your code.');
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
  now = new Date();
  countTo = new Date(countTo);
  difference = (now-countTo);

  days=Math.floor(difference/(60*60*1000*24)*1);
  hours=Math.floor((difference%(60*60*1000*24))/(60*60*1000)*1);
  mins=Math.floor(((difference%(60*60*1000*24))%(60*60*1000))/(60*1000)*1);
  secs=Math.floor((((difference%(60*60*1000*24))%(60*60*1000))%(60*1000))/1000*1);

  days = (days  == 1) ? days  + ' day '    : days  + ' days ';
  hours = (hours == 1) ? hours + ' hour '   : hours + ' hours ';
  mins = (mins  == 1) ? mins  + ' minute ' : mins  + ' minutes ';
  secs = (secs  == 1) ? secs  + ' second ' : secs  + ' seconds';


  var timeString = '<span class="upTime">' + days + hours + mins + secs + '</span>';
  document.getElementById( el ).innerHTML = timeString;

  clearTimeout(upTime.to);
  upTime.to=setTimeout(function(){ upTime(countTo, el); },1000);
}


// Fill modal with content from link href
$("#threadDumpModal").on("show.bs.modal", function(e) {
    var link = $(e.relatedTarget);
    $(this).find(".modal-body pre").load(link.val());
});

/*
$("#pingModal").on("show.bs.modal", function(e) {
    var link = $(e.relatedTarget);

    //$(this).find(".modal-body").load(link.val());
    var resp = {};

    $.get( link.val() , function( data ) {
        if ( data.trim() == 'pong') {
            resp.msg = data.trim();
            resp.status = 'success';
        } else {
            resp.msg = 'Something went wrong';
            resp.status = 'danger';
        }
    })
      .fail(function() {
        resp.msg = 'Something went wrong';
        resp.status = 'danger';
      }).always(function() {
        var alert = '<div class="alert alert-' + resp.status+ '">'+ resp.msg +'</div>';
        $('#pingModal .modal-body').html(alert);
      });
});
*/

/**
 * Thread Dump Preview
 */
$.get( $('#threadDumpViewButton').val() , function( data ) {
    $('#threadDumpPreview').html( data.substr(-400) );
});

/**
 * Ping box
 */
function populatePingBox() {
    console.log('calling pingbox');
    var url = $('#pingBoxRefresh').val();
    var pingBox = $('#pingBox');

    pingBox.text('pinging...');

    var resp = {};

    $.get(url, function (data) {
        console.log('pingBox',data);
        if (data.trim() == 'pong') {
            resp.msg = data.trim();
            resp.status = 'success';
        } else {
            resp.msg = 'Something went wrong';
            resp.status = 'danger';
        }
    })
    .fail(function () {
        resp.msg = 'Something went wrong';
        resp.status = 'danger';
    }).always(function () {
        var alert = '<div class="alert alert-' + resp.status + '">' + resp.msg + '!!</div>';
        pingBox.html(alert);
    });
};
populatePingBox();

$('#pingBoxRefresh').click(function() {
    populatePingBox();
});

function metricsGauges() {
    //http://localhost:8080/cas/statistics/metrics
    $.get('statistics/metrics', function( data ) {
        console.dir(data);

        var gauges;
        var gaugesCount = Object.keys(data.gauges).length;

        var str = '<div class="row equal">';

        var count = 0;

//for(var i in $yourArray){
//   str += '<li><a href="#">String 1</a></li>';
//}

        $.each(data.gauges, function (key, value) {
            console.log('pre count: ', count);
            if (count == 4) {
                str += '</div><div class="row equal">';
                count = 0;
            }
            str += '<div class="col-md-3"><div class="panel panel-default"><div class="panel-heading">'+key+'</div><div class="panel-body">';
            $.each(value, function (index, value) {
                str += value + '</div></div></div>';
            })
            count++;
            console.log('post count: ', count);
        });

        str += '</row>';

        $('#metricsGauges').append(str);

        //for (i=0; i< gaugesCount;i++) {
        //    gauges += '<li>' +
        //}
        //console.log(Object.keys(data.gauges).length);

    }).fail(function(err){
        console.error(err);
    });
}
//metricsGauges();