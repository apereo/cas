require 'html-proofer'
require 'find'
require 'fileutils'

system("clear") || system("cls")

ROOT_DIRECTORY="cas"
VERSION="development"

options = {
  :typhoeus => { followlocation: true },
  :disable_external => false,
  :allow_hash_href => true,
  # :only_4xx => true,
  :empty_alt_ignore => true,
  :http_status_ignore => [401,429,301,302],
  :parallel => { :in_processes => 3},
  :url_swap => {
    %r{^/cas/} => '/'
  }, 
  :url_ignore => [
    %r{/*\d+.\d+.x/},
    %r{^#.+},
    %r{localhost},
    %r{/cas/development/$},
    %r{(images|javascripts|stylesheets)}
  ],
  :verbose => true
}

Dir.mkdir(ROOT_DIRECTORY) unless File.exist?(ROOT_DIRECTORY)
FileUtils.rm_r("#{ROOT_DIRECTORY}/#{VERSION}") if File.exist?("#{ROOT_DIRECTORY}/#{VERSION}")
FileUtils.rm_r("#{ROOT_DIRECTORY}/developer") if File.exist?("#{ROOT_DIRECTORY}/developer")

# Copy project documentation
FileUtils.cp_r("/root/docs/#{VERSION}", "#{ROOT_DIRECTORY}")
FileUtils.cp_r("/root/docs/developer", "#{ROOT_DIRECTORY}/developer")
files = Dir.glob("/root/docs/*.html")
for file in files 
  FileUtils.cp(file, ROOT_DIRECTORY)
end

# files = Dir.glob("#{ROOT_DIRECTORY}/**/*.html")
# for file in files 
#   puts file
# end

puts "Checking files..."
HTMLProofer.check_directory("./#{ROOT_DIRECTORY}", options).run
