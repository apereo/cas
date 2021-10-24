require 'html-proofer'
require 'find'
require 'fileutils'

options = {
  :typhoeus => { followlocation: true },
  :disable_external => false,
  :allow_hash_href => true,
  :only_4xx => true,
  :empty_alt_ignore => true,
  :http_status_ignore => [401],
  :parallel => { :in_processes => 3},
  :url_swap => {
    %r{^/cas/} => '/'
  }, 
  :url_ignore => [
    %r{^/\d+.\d+.x},
    %r{^/development/},
    %r{Older-Versions},
    %r{^#.+},
    %r{^../images/}
  ],
  :verbose => true
}

files = Dir.glob("/root/docs/**/*.html")
for file in files 
  dir = File.dirname(file)
  if dir.end_with?("/docs") || file.include?("development")
   puts "Checking file #{file}"
   HTMLProofer.check_file(file, options).run
  end
end