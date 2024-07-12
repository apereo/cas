require 'tmpdir'

module Jekyll
  class TempDirTag < Liquid::Tag
    def render(context)
      Dir.tmpdir
    end
  end
end

Liquid::Template.register_tag('temp_dir', Jekyll::TempDirTag)
