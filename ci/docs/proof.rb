require 'html-proofer'
require 'find'
require 'fileutils'
require 'tmpdir'

# system("clear") || system("cls")

VERSION="development"

temp = Dir.tmpdir()
TARGET_DIRECTORY="#{temp}/cas"
puts "Root directory is #{TARGET_DIRECTORY}"

CURRENT_DIR=Dir.pwd
SOURCE_DIRECTORY="#{CURRENT_DIR}/gh-pages/_site"
 

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
    %r{/cas/development/$}
  ],
  :verbose => true
}

Dir.mkdir(TARGET_DIRECTORY) unless File.exist?(TARGET_DIRECTORY)
FileUtils.rm_r("#{TARGET_DIRECTORY}/#{VERSION}") if File.exist?("#{TARGET_DIRECTORY}/#{VERSION}")
FileUtils.rm_r("#{TARGET_DIRECTORY}/developer") if File.exist?("#{TARGET_DIRECTORY}/developer")
FileUtils.rm_r("#{TARGET_DIRECTORY}/assets") if File.exist?("#{TARGET_DIRECTORY}/assets")
FileUtils.rm_r("#{TARGET_DIRECTORY}/images") if File.exist?("#{TARGET_DIRECTORY}/images")
FileUtils.rm_r("#{TARGET_DIRECTORY}/javascripts") if File.exist?("#{TARGET_DIRECTORY}/javascripts")
FileUtils.rm_r("#{TARGET_DIRECTORY}/stylesheets") if File.exist?("#{TARGET_DIRECTORY}/stylesheets")

# Copy project documentation
puts "Copying #{SOURCE_DIRECTORY} to #{TARGET_DIRECTORY}"

FileUtils.cp_r("#{SOURCE_DIRECTORY}/#{VERSION}", "#{TARGET_DIRECTORY}")
FileUtils.cp_r("#{SOURCE_DIRECTORY}/developer", "#{TARGET_DIRECTORY}/developer")
FileUtils.cp_r("#{SOURCE_DIRECTORY}/images", "#{TARGET_DIRECTORY}/images")
FileUtils.cp_r("#{SOURCE_DIRECTORY}/stylesheets", "#{TARGET_DIRECTORY}/stylesheets")
FileUtils.cp_r("#{SOURCE_DIRECTORY}/javascripts", "#{TARGET_DIRECTORY}/javascripts")
FileUtils.cp_r("#{SOURCE_DIRECTORY}/assets", "#{TARGET_DIRECTORY}/assets")
files = Dir.glob("#{SOURCE_DIRECTORY}/*.html")
for file in files 
  FileUtils.cp(file, TARGET_DIRECTORY)
end


# files = Dir.glob("#{TARGET_DIRECTORY}/**/*.*")
# for file in files 
#   puts file
# end

# puts "Checking files..."
HTMLProofer.check_directory("#{TARGET_DIRECTORY}", options).run
