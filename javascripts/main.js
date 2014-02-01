  $(function(){
      $('a').each(function() {
          var href = this.href;
          if (href.indexOf("$version") != -1) {
            href = href.replace("$version", "current");
            $(this).attr('href', href);
          }
      });
  });