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
  :url_ignore => [
    %r{^/cas/\d+\..+},
    %r{^/cas/development/$},
    %r{^../images/}
  ],
}

files = Dir.glob("/root/docs/**/*.html")
for file in files 
  dir = File.dirname(file)
  if dir.end_with?("/docs") || file.include?("development")
   puts "Checking file #{file}"
   HTMLProofer.check_file(file, options).run
  end
end