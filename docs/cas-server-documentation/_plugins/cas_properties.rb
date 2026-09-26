require "cgi"

# Looks up CAS and third-party configuration properties in the generated site data once per build,
# instead of scanning every catalog entry in Liquid for every property block on every page.
module Jekyll
  module CasPropertiesFilter
    KIND_ORDER = { "required" => 0, "optional" => 1, "thirdparty" => 2 }.freeze
    INLINE_TAGS = %w[a b br code em i li ol p pre strong sub sup tt ul].freeze
    BLOCK_TAG = %r{<\s*(?:ul|ol|pre|table|div)\b}i

    @catalogs = {}

    class << self
      def catalog(data)
        return [] unless data.is_a?(Hash)
        @catalogs[data.object_id] ||= build_catalog(data)
      end

      def split(value)
        value.to_s.split(",").map(&:strip).reject(&:empty?)
      end

      # Javadoc descriptions separate paragraphs with bare <p> tags, leave <li> unclosed and escape
      # generics; the result is rebuilt as balanced, single-line HTML.
      def description_html(text)
        html = text.to_s.gsub(/\{@(?:code|link|linkplain|value)\s+([^}]*)\}/) { "<code>#{Regexp.last_match(1).strip}</code>" }
        html = html.gsub(%r{&lt;(/?(?:#{INLINE_TAGS.join("|")}))(\s[^&]*)?&gt;}i) { "<#{Regexp.last_match(1)}#{Regexp.last_match(2)}>" }
        html = html.gsub(%r{<(/?)([a-z][\w-]*)([^<>]*)>|[<>]}i) do |match|
          tag = Regexp.last_match(2)
          tag && INLINE_TAGS.include?(tag.downcase) ? match : CGI.escapeHTML(match)
        end
        html = html.gsub(/\s*\n\s*/, " ")
        html.split(%r{\s*<\s*/?\s*p\s*/?\s*>\s*}i).map(&:strip).reject(&:empty?).map do |segment|
          segment = segment.gsub(%r{<li>(.*?)(?:</li>)?\s*(?=<li>|</ul>|</ol>|\z)}mi) { "<li>#{Regexp.last_match(1).strip}</li>" }
          segment.match?(BLOCK_TAG) ? "<div>#{segment}</div>" : "<p>#{segment}</p>"
        end.join
      end

      def summary_text(text, length = 220)
        plain = CGI.unescapeHTML(description_html(text).gsub(/<[^>]+>/, " ")).squeeze(" ").strip
        plain = "#{plain[0, length - 3].rstrip}..." if plain.length > length
        CGI.escapeHTML(plain)
      end

      private

      def build_catalog(data)
        seen = {}
        data.each do |group, files|
          next unless files.is_a?(Hash)
          files.each_value do |entries|
            next unless entries.is_a?(Array)
            entries.each do |entry|
              next unless entry.is_a?(Hash) && entry["name"].is_a?(String)
              next if seen.key?(entry["name"])
              seen[entry["name"]] = decorate(entry, group == "third-party")
            end
          end
        end
        seen.values.sort_by { |entry| [KIND_ORDER[entry["kind"]], entry["name"]] }
      end

      def decorate(entry, third_party)
        kind = if third_party
                 "thirdparty"
               elsif entry["required"] == true
                 "required"
               else
                 "optional"
               end
        type = entry["type"].to_s
        entry.merge(
          "kind" => kind,
          "displayName" => entry["name"].gsub("[]", "[0]"),
          "displayType" => type.gsub(/\b[a-z][a-z0-9_]*\./, "").gsub("$", "."),
          "defaultText" => entry["defaultValue"].nil? ? "" : entry["defaultValue"].to_s,
          "descriptionHtml" => description_html(entry["description"]),
          "summaryText" => summary_text(entry["shortDescription"].to_s.strip.empty? ? entry["description"] : entry["shortDescription"])
        )
      end
    end

    def cas_properties(data, properties, includes = nil, excludes = nil)
      wanted = CasPropertiesFilter.split(properties)
      return [] if wanted.empty?
      included = CasPropertiesFilter.split(includes)
      excluded = CasPropertiesFilter.split(excludes)
      CasPropertiesFilter.catalog(data).select do |entry|
        name = entry["name"]
        wanted.any? { |p| name.include?(p) } &&
          excluded.none? { |x| name.include?(x) } &&
          (included.empty? || included.any? { |i| name.include?(i) })
      end
    end

    def cas_third_party_properties(data, contains = nil, exact = nil, starts_with = nil, excludes = nil)
      containing = CasPropertiesFilter.split(contains)
      exactly = CasPropertiesFilter.split(exact)
      starting = CasPropertiesFilter.split(starts_with)
      return [] if containing.empty? && exactly.empty? && starting.empty?
      excluded = CasPropertiesFilter.split(excludes)
      CasPropertiesFilter.catalog(data).select do |entry|
        name = entry["name"]
        entry["kind"] == "thirdparty" &&
          excluded.none? { |x| name.include?(x) } &&
          (containing.any? { |p| name.include?(p) } ||
            exactly.include?(name) ||
            starting.any? { |p| name.start_with?(p) })
      end
    end
  end
end

Liquid::Template.register_filter(Jekyll::CasPropertiesFilter)
