require "set"

# Release notes pages carry a `release:` front matter block. Before rendering, their markdown is rewritten so
# change tags become classes kramdown puts on the headings and list items, and the "Other Stuff" list is grouped
# by area. The digest include and the changelog filters in site.js read what is computed here.
module CasReleaseNotes
  TYPES = {
    "new" => "New", "changed" => "Changed", "fixed" => "Fixed", "action" => "Action Required", "removed" => "Removed"
  }.freeze

  SHORT = { "action" => "Action" }.freeze

  AREAS = [
    ["authentication", "Authentication"],
    ["passwordless", "Passwordless"],
    ["mfa", "Multifactor & Passkeys"],
    ["oidc", "OAuth & OpenID Connect"],
    ["saml", "SAML2"],
    ["protocols", "CAS & Other Protocols"],
    ["authorization", "Authorization"],
    ["attributes", "Attributes & Consent"],
    ["tickets", "Tickets & Sessions"],
    ["services", "Service Registry"],
    ["ui", "Interface & Accounts"],
    ["operations", "Platform & Operations"],
    ["docs", "Documentation"],
    ["project", "Build & Project"],
    ["general", "General"]
  ].freeze
  AREA_LABELS = AREAS.to_h.freeze

  LINK_AREAS = [
    [%r{authentication/Passwordless}i, "passwordless"],
    [%r{authentication/(OIDC|OAuth)|protocol/(OIDC|OAuth)}i, "oidc"],
    [%r{SAML2|protocol/SAML|SAML2-}i, "saml"],
    [%r{integration/Attribute|integration/Person-Directory}i, "attributes"],
    [%r{integration/Delegate}i, "authentication"],
    [%r{\.\./mfa/}i, "mfa"],
    [%r{\.\./authorization/}i, "authorization"],
    [%r{\.\./ticketing/}i, "tickets"],
    [%r{\.\./services/}i, "services"],
    [%r{\.\./(webflow|ux|registration|password_management)/}i, "ui"],
    [%r{\.\./protocol/}i, "protocols"],
    [%r{\.\./authentication/}i, "authentication"],
    [%r{\.\./(installation|configuration|monitoring|logging|high_availability|planning|integration|audits|notifications|multitenancy)/}i, "operations"],
    [%r{developer/|github\.com/apereo}i, "project"]
  ].freeze

  KEYWORD_AREAS = [
    [/\b(unit|integration|browser) tests?\b|\bdependencies\b|\bREADME\b|\bGradle\b/i, "project"],
    [/WebAuthn|passkey|Google Authenticator|YubiKey|Duo Security|multifactor/i, "mfa"],
    [/OpenID Connect|OAuth|\bJWKS\b/i, "oidc"],
    [/ticket registry|ticket-granting|\bMongoDB?\b|\bRedis\b/i, "tickets"],
    [/service registry|service definition|\bservice\b parameter/i, "services"],
    [/\bcookie|logout|session\b/i, "authentication"]
  ].freeze

  TAG = /\{:\s*([^}]*)\}/.freeze
  OTHER = /\Aother (stuff|changes)\z/i.freeze
  NOTEWORTHY = /\Anew (&|and) noteworthy\z/i.freeze

  module_function

  # Parses `{: .fixed data-area="mfa"}` into [type, area, remaining attributes].
  def parse_tag(body)
    type = nil
    area = nil
    rest = []
    body.scan(/\.([\w-]+)|([\w-]+)="([^"]*)"|(#[\w-]+)/) do |cls, key, value, id|
      if cls && TYPES.key?(cls) && type.nil?
        type = cls
      elsif cls&.start_with?("cas-change")
        next
      elsif cls
        rest << ".#{cls}"
      elsif key == "data-area" || key == "area"
        area = value
      elsif key&.end_with?("-label")
        next
      elsif key == "data-type"
        type ||= value if TYPES.key?(value)
      elsif key
        rest << "#{key}=\"#{value}\""
      elsif id
        rest << id
      end
    end
    [type, area, rest]
  end

  def area_for(text)
    links = text.scan(/\]\(([^)\s]+)/).flatten
    links.each do |href|
      LINK_AREAS.each { |pattern, area| return area if href.match?(pattern) }
    end
    KEYWORD_AREAS.each { |pattern, area| return area if text.match?(pattern) }
    "general"
  end

  def tag_for(kind, type, area, rest)
    parts = [".cas-change", ".cas-change-#{kind}"]
    parts << "data-type=\"#{type}\"" if type
    parts << "data-area=\"#{area}\""
    parts << "data-area-label=\"#{AREA_LABELS.fetch(area, area)}\"" if kind == "topic"
    parts << "data-type-label=\"#{SHORT[type] || TYPES[type]}\"" if type
    parts.concat(rest)
    "{: #{parts.join(' ')}}"
  end

  # Mirrors kramdown-parser-gfm's header ids so links and the digest point at the rendered headings.
  def gfm_id(text, counter)
    id = text.strip.downcase.gsub(/[^\p{Word}\- \t]/u, "").tr(" \t", "-")
    counter[id] += 1
    counter[id].positive? ? "#{id}-#{counter[id]}" : id
  end

  def transform(content, release)
    lines = content.split("\n", -1)
    output = []
    section = nil
    other = nil
    fence = false
    i = 0
    while i < lines.length
      line = lines[i]
      fence = !fence if line.lstrip.start_with?("```")
      if !fence && (heading = line.match(/\A(#{'#'}{2,3}) +(.+?)\s*\z/))
        if heading[1] == "##"
          output.concat(group_other(other)) if other
          other = nil
          section = heading[2]
          output << line
          if section.match?(OTHER)
            other = []
            output << "{: .cas-change-others}"
          end
          i += 1
          next
        elsif section&.match?(NOTEWORTHY)
          body_end = i + 1
          body_end += 1 while body_end < lines.length && !lines[body_end].match?(/\A\#{2,3} /)
          existing = lines[i + 1]&.match(/\A\s*#{TAG}\s*\z/)
          type, area, rest = existing ? parse_tag(existing[1]) : [nil, nil, []]
          body = lines[(i + 1)...body_end].join("\n")
          area ||= area_for(body)
          output << line
          output << tag_for("topic", type, area, rest)
          i += existing ? 2 : 1
          next
        end
      end
      if other && !fence
        other << line
      else
        output << line
      end
      i += 1
    end
    output.concat(group_other(other)) if other
    output.join("\n")
  end

  def group_other(lines)
    collected = []
    leading = []
    trailing = []
    item = nil
    lines.each do |line|
      if trailing.empty? && line.start_with?("- ", "* ")
        item = [line.sub(/\A[-*] +/, "")]
        collected << item
      elsif item && trailing.empty? && (line.start_with?("  ") || line.strip.empty?)
        item << line
      elsif collected.empty?
        leading << line
      else
        trailing << line
      end
    end
    out = leading.dup
    entries = collected.map do |entry|
      text = entry.first
      tag = text.match(/\A\s*#{TAG}\s*/)
      type, area, rest = tag ? parse_tag(tag[1]) : [nil, nil, []]
      text = text.sub(/\A\s*#{TAG}\s*/, "") if tag
      continuation = entry.drop(1)
      continuation.pop while continuation.any? && continuation.last.strip.empty?
      area ||= area_for(([text] + continuation).join(" "))
      { area: area, lines: ["- #{tag_for('item', type, area, rest)} #{text}"] + continuation }
    end
    AREAS.each do |key, label|
      matching = entries.select { |entry| entry[:area] == key }
      next if matching.empty?
      out << ""
      out << "### #{label}"
      out << "{: .cas-change-group data-area=\"#{key}\"}"
      out << ""
      matching.each { |entry| out.concat(entry[:lines]) }
    end
    unknown = entries.reject { |entry| AREA_LABELS.key?(entry[:area]) }
    unless unknown.empty?
      out << ""
      out << "### #{AREA_LABELS['general']}"
      out << "{: .cas-change-group data-area=\"general\"}"
      out << ""
      unknown.each { |entry| out.concat(entry[:lines]) }
    end
    out << ""
    out.concat(trailing)
    out
  end

  # Reads the transformed markdown back: headings with their ids, and every tagged topic and item.
  def inventory(markdown)
    counter = Hash.new(-1)
    headings = []
    changes = []
    fence = false
    lines = markdown.split("\n")
    lines.each_with_index do |line, index|
      fence = !fence if line.lstrip.start_with?("```")
      next if fence
      if (heading = line.match(/\A(\#{1,6}) +(.+?)\s*\z/))
        nxt = lines[index + 1].to_s
        explicit = nxt.match(/\A\s*\{:[^}]*#([\w-]+)/)
        id = explicit ? explicit[1] : gfm_id(heading[2], counter)
        headings << { "level" => heading[1].length, "title" => heading[2], "id" => id, "line" => index }
        if (tag = nxt.match(/\A\s*#{TAG}\s*\z/)) && tag[1].include?("cas-change-topic")
          changes << change(tag[1], "topic", heading[2], id)
        end
      elsif (tag = line.match(/\A[-*] +#{TAG}/)) && tag[1].include?("cas-change-item")
        changes << change(tag[1], "item", nil, nil)
      end
    end
    [headings, changes]
  end

  def change(tag, kind, title, id)
    type = tag[/data-type="([^"]+)"/, 1]
    area = tag[/data-area="([^"]+)"/, 1]
    { "kind" => kind, "type" => type, "area" => area, "title" => title, "id" => id }
  end

  def section_counts(markdown, headings)
    lines = markdown.split("\n")
    headings.each_with_index.to_h do |heading, index|
      stop = headings[(index + 1)..].find { |other| other["level"] <= 3 }
      body = lines[(heading["line"] + 1)...(stop ? stop["line"] : lines.length)]
      [heading["title"], body.count { |line| line.start_with?("- ", "* ") }]
    end
  end

  def digest(markdown, release, nav)
    headings, changes = inventory(markdown)
    counts = section_counts(markdown, headings)
    by_title = headings.to_h { |heading| [heading["title"].strip.downcase, heading] }
    resolve = lambda do |name|
      found = by_title[name.to_s.strip.downcase]
      Jekyll.logger.warn("Release notes:", "no heading named #{name.inspect}") if found.nil? && defined?(Jekyll.logger)
      found
    end
    topics = changes.select { |entry| entry["kind"] == "topic" }
    items = changes.select { |entry| entry["kind"] == "item" }
    highlights = Array(release["highlights"]).map do |entry|
      heading = resolve.call(entry["section"])
      topic = topics.find { |candidate| heading && candidate["id"] == heading["id"] }
      area = entry["area"] || topic&.dig("area") || "general"
      type = entry["type"] || topic&.dig("type")
      {
        "title" => entry["title"] || entry["section"], "summary" => entry["summary"], "anchor" => heading&.dig("id"),
        "area" => area, "areaLabel" => AREA_LABELS.fetch(area, area), "type" => type, "typeLabel" => SHORT[type] || TYPES[type],
        "count" => heading ? counts[heading["title"]] : 0
      }
    end
    upgrade = Array(release["upgrade"]).map do |entry|
      heading = entry["section"] ? resolve.call(entry["section"]) : nil
      type = entry["type"] || "changed"
      entry.merge("anchor" => heading&.dig("id"), "typeLabel" => SHORT[type] || TYPES.fetch(type, type), "type" => type,
                  "areaLabel" => entry["area"] && AREA_LABELS.fetch(entry["area"], entry["area"]))
    end
    spotlight = release["spotlight"] && begin
      heading = resolve.call(release["spotlight"]["section"])
      release["spotlight"].merge("anchor" => heading&.dig("id"))
    end
    types = TYPES.map { |key, label| { "key" => key, "label" => label, "count" => changes.count { |c| c["type"] == key } } }
    areas = AREAS.filter_map do |key, label|
      count = changes.count { |c| c["area"] == key }
      { "key" => key, "label" => label, "count" => count } if count.positive?
    end
    {
      "nav" => nav, "highlights" => highlights, "upgrade" => upgrade, "spotlight" => spotlight,
      "types" => types.select { |entry| entry["count"].positive? }, "areas" => areas,
      "stats" => {
        "topics" => topics.size, "upgrade" => upgrade.size, "others" => items.size,
        "fixed" => changes.count { |c| c["type"] == "fixed" }, "changes" => changes.size
      }
    }
  end

  def rc_number(page)
    page.name[/\ARC(\d+)\.md\z/, 1]&.to_i
  end

  def version(page)
    page.data["release"]["version"] || page.data["title"].to_s[/(\d+\.\d+\.\d+[-\w.]*)/, 1] || "RC#{rc_number(page)}"
  end
end

if defined?(Jekyll::Hooks)
  Jekyll::Hooks.register :site, :post_read do |site|
    releases = site.pages.select { |page| page.data["release"].is_a?(Hash) && CasReleaseNotes.rc_number(page) }
    overview = Hash.new { |hash, key| hash[key] = [] }
    releases.group_by(&:dir).each do |dir, pages|
      pages = pages.sort_by { |page| CasReleaseNotes.rc_number(page) }
      pages.each do |page|
        page.content = CasReleaseNotes.transform(page.content, page.data["release"])
        nav = pages.map do |other|
          { "label" => "RC#{CasReleaseNotes.rc_number(other)}", "url" => other.name.sub(/\.md\z/, ".html"),
            "current" => other.equal?(page) }
        end
        page.data["release_digest"] = CasReleaseNotes.digest(page.content, page.data["release"], nav)
        page.data["release_digest"]["version"] = CasReleaseNotes.version(page)
        overview[dir] << {
          "label" => "RC#{CasReleaseNotes.rc_number(page)}", "version" => CasReleaseNotes.version(page),
          "url" => page.name.sub(/\.md\z/, ".html"), "summary" => page.data["release"]["summary"],
          "date" => page.data["release"]["date"], "stats" => page.data["release_digest"]["stats"],
          "highlights" => page.data["release_digest"]["highlights"].map { |entry| entry["title"] }
        }
      end
    end
    site.data["cas_release_notes"] = overview.transform_values(&:reverse)
  end
end
