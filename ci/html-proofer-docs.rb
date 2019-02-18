require 'html-proofer'
require 'html/pipeline'
require 'find'
require 'fileutils'

# make an out dir
Dir.mkdir("out") unless File.exist?("out")

pipeline = HTML::Pipeline.new [
  HTML::Pipeline::MarkdownFilter,
  HTML::Pipeline::TableOfContentsFilter
], :gfm => true

# iterate over files, and generate HTML from Markdown
Find.find("./docs") do |path|
  if File.extname(path) == ".md"
    contents = File.read(path)
    result = pipeline.call(contents)
    dirname = File.dirname(path)
    FileUtils.mkdir_p ("out/" + dirname)
    content_str = result[:output].to_s
    filename = path.split("/").pop.sub('.md', '.html')
    if filename == "sidebar.html"
      content_str = content_str.gsub! '/%24version' '.'
    end
    File.open("out/#{dirname}/#{filename}", 'w') { |file| file.write(content_str) }
  end
end
# url_ignore - ignore links content not in branch
# file_ignore - ignore CAS spec b/c it has lots of bad anchor links, only *.html files are processed
options = {
            :file_ignore =>  [ %r{.*/CAS-Protocol-Specification.html} ],
            :disable_external => true,
            :only_4xx => true,
            :empty_alt_ignore => true,
            :url_ignore => [ %r{^/cas}, %r{^../images/}, %r{^../../developer/} ],
          }
# test your out dir!
HTMLProofer.check_directory("./out", options).run
