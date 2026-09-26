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
CHECK_EXTERNAL = ENV.fetch("DOCS_PROOF_EXTERNAL", "true") == "true"
EXTERNAL_CACHE_DIRECTORY = ENV.fetch("DOCS_PROOF_CACHE_DIR", "#{CURRENT_DIR}/build/htmlproofer")
EXTERNAL_CACHE_TIMEFRAME = ENV.fetch("DOCS_PROOF_CACHE_TIMEFRAME", "7d")
puts "External links will #{CHECK_EXTERNAL ? "" : "not "}be checked"

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
  :disable_external => !CHECK_EXTERNAL,
  :allow_hash_href => true,
  :allow_missing_href => true,
  :ignore_missing_alt => true,
  :check_external_hash => false,
  # :only_4xx => true,
  :empty_alt_ignore => true,
  :ignore_status_codes => [0,401,429,301,302,502,504],
  :parallel => { :in_processes => 2},
  :enforce_https => false,
  :swap_urls => {
    %r{^/cas/} => '/'
  }, 
  :ignore_urls => [
    %r{/*\d+\.\d+\.x/},
    %r{^#.+},
    %r{localhost},
    %r{/cas/development/$},
    %r{\Ahttps?://(?:[^/]+\.)?refeds\.org(?:/.*)?\z},
    %r{\Ahttps?://apereo\.slack\.com(?:/.*)?\z},
    %r{\Ahttps?://fonts\.gstatic\.com(?:/.*)?\z},
    %r{\Ahttps?://fonts\.googleapis\.com(?:/.*)?\z}
  ],
  :verbose => true
}
if CHECK_EXTERNAL
  # Links that answered successfully within the timeframe are not requested again; failures always are.
  options[:cache] = { :timeframe => { :external => EXTERNAL_CACHE_TIMEFRAME }, :storage_dir => EXTERNAL_CACHE_DIRECTORY }
end

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
FileUtils.rm_f(Dir.glob("#{TARGET_DIRECTORY}/developer/Release-Process-*.html"))

# files = Dir.glob("#{TARGET_DIRECTORY}/**/*.*")
# for file in files 
#   puts file
# end

# puts "Checking files..."
proofer = HTMLProofer.check_directory("#{TARGET_DIRECTORY}", options)
proofer.before_request do |request|
  request.options[:headers]['User-Agent'] = "Mozilla/5.0 (X11; Linux i686; rv:103.0) Gecko/20100101 Firefox/103.0"
end
begin
  proofer.run
rescue SystemExit
  # html-proofer reports its failures and exits; they are sorted below instead.
end

# Exit codes: 3 when internal links, images or scripts are broken (deterministic, never worth a retry),
# 4 when only external links failed (the site is still fine to publish).
external_failures, internal_failures = proofer.failed_checks.partition { |failure| failure.description.to_s.start_with?("External link") }
puts "HTML Proofer found #{internal_failures.size} internal and #{external_failures.size} external failures"
exit 3 unless internal_failures.empty?
exit 4 unless external_failures.empty?
