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
  :typhoeus => {
    :method => :get,
    :followlocation => true,
    :connecttimeout => 20,
    :timeout => 60,
    :ssl_verifypeer => false,
    :ssl_verifyhost => 0,
    :cookiefile => ".cookies",
    :cookiejar => ".cookies",
    :headers =>{
      "User-Agent" => "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/115.0.0.0 Safari/537.36"
    }
  },
  :disable_external => false,
  :allow_hash_href => true,
  :allow_missing_href => true,
  :ignore_missing_alt => true,
  :check_external_hash => false,
  # :only_4xx => true,
  :empty_alt_ignore => true,
  :ignore_status_codes => [0,401,429,301,302,502,504],
  :parallel => { :in_processes => 8},
  :enforce_https => false,
  :swap_urls => {
    %r{^/cas/} => '/'
  }, 
  :ignore_urls => [
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
proofer = HTMLProofer.check_directory("#{TARGET_DIRECTORY}", options)
proofer.before_request do |request|
  request.options[:headers]['User-Agent'] = "Mozilla/5.0 (X11; Linux i686; rv:103.0) Gecko/20100101 Firefox/103.0"
end
proofer.run
