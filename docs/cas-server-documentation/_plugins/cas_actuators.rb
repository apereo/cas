require "cgi"

# Prepares actuator endpoint data for the endpoint reference: operations sorted by path and method,
# normalized parameters, a working curl example, and settings snippets in each supported format.
module Jekyll
  module CasActuatorsFilter
    METHOD_ORDER = %w[GET POST PUT PATCH DELETE].freeze
    WITH_BODY = %w[POST PUT PATCH].freeze
    EXPOSED_BY_DEFAULT = %w[info health status].freeze
    SERVER = "https://sso.example.org/cas/actuator/".freeze

    SECURITY_RULES = {
      "authenticated" => [["access", "AUTHENTICATED"]],
      "role" => [["access", "ROLE"], ["required-roles", "ADMIN"]],
      "ip" => [["access", "IP_ADDRESS"], ["required-ip-addresses", "127.0.0.1"]],
      "permit" => [["access", "PERMIT"]]
    }.freeze

    class << self
      def operations(data, endpoint_id)
        blocks = data.is_a?(Hash) && data["actuators"].is_a?(Hash) ? data["actuators"][endpoint_id] : nil
        return [] unless blocks.is_a?(Hash)
        seen = Hash.new(0)
        blocks.values.select { |value| value.is_a?(Array) }.flatten.select { |op| op.is_a?(Hash) && op["path"] }
          .map { |op| decorate(op, endpoint_id) }
          .sort_by { |op| [op["path"], METHOD_ORDER.index(op["method"]) || METHOD_ORDER.size] }
          .each do |op|
            seen[op["anchor"]] += 1
            op["anchor"] = "#{op['anchor']}-#{seen[op['anchor']]}" if seen[op["anchor"]] > 1
          end
      end

      def environment_variable(name)
        name.gsub(/\[(\d+)\]/, '_\1_').tr(".", "_").delete("-").squeeze("_").gsub(/\A_|_\z/, "").upcase
      end

      def snippet(pairs, format)
        case format
        when "yaml" then yaml(pairs)
        when "env" then pairs.map { |key, value| "#{environment_variable(key)}=#{value}" }.join("\n")
        else pairs.map { |key, value| "#{key}=#{value}" }.join("\n")
        end
      end

      private

      def yaml(pairs)
        tree = {}
        pairs.each do |key, value|
          *parents, leaf = key.split(".")
          node = parents.reduce(tree) { |current, segment| current[segment] ||= {} }
          node[leaf] = value
        end
        lines = []
        walk = lambda do |node, depth|
          node.each do |key, value|
            if value.is_a?(Hash)
              lines << "#{'  ' * depth}#{key}:"
              walk.call(value, depth + 1)
            else
              lines << "#{'  ' * depth}#{key}: \"#{value}\""
            end
          end
        end
        walk.call(tree, 0)
        lines.join("\n")
      end

      def decorate(op, endpoint_id)
        method = op["method"].to_s.upcase
        path = op["path"].to_s
        suffix = path.start_with?(endpoint_id) ? path[endpoint_id.length..] : "/#{path}"
        parameters = Array(op["parameters"]).map { |param| parameter(param) }
        produces = Array(op["produces"]).map(&:to_s)
        consumes = Array(op["consumes"]).map(&:to_s)
        owner = op["owner"].to_s
        op.merge(
          "method" => method,
          "methodClass" => method.downcase,
          "suffix" => suffix,
          "pathHtml" => highlight(path),
          "suffixHtml" => highlight(suffix),
          "anchor" => "actuator-#{endpoint_id}-#{method}-#{suffix}".downcase.gsub(/[^a-z0-9]+/, "-").gsub(/\A-|-\z/, ""),
          "params" => parameters,
          "producesText" => produces.join(", "),
          "consumesText" => consumes.join(", "),
          "ownerShort" => owner.split(".").last.to_s,
          "signatureShort" => op["signature"].to_s.sub(/\A[^(]*\./, "").gsub(/\bjava\.(?:lang|util)\./, ""),
          "ownerPackage" => owner.rpartition(".").first,
          "searchText" => [method, path, op["summary"]].join(" ").downcase,
          "curl" => curl(method, path, parameters, produces, consumes)
        )
      end

      def parameter(param)
        return { "name" => param.to_s, "in" => "condition", "description" => "Request must match this parameter condition" } unless param.is_a?(Hash)
        location = if param["selector"] then "path"
                   elsif param["header"] then "header"
                   elsif param["body"] then "body"
                   else "query"
                   end
        param.merge("in" => location, "type" => param["type"].to_s, "description" => param["description"].to_s,
                    "defaultText" => param["defaultValue"].to_s)
      end

      def highlight(path)
        CGI.escapeHTML(path).gsub(/\{([^}]+)\}/) { "<span class=\"cas-op-var\">{#{Regexp.last_match(1)}}</span>" }
      end

      def curl(method, path, parameters, produces, consumes)
        lines = ["curl -u \"casuser:password\""]
        lines << "-X #{method}" unless method == "GET"
        parameters.select { |param| param["in"] == "header" }.each { |param| lines << "-H \"#{param['name']}: ...\"" }
        if WITH_BODY.include?(method)
          lines << "-H \"Content-Type: #{consumes.first || 'application/json'}\""
          lines << "-d @request.json" if parameters.any? { |param| param["in"] == "body" }
        end
        lines << "-H \"Accept: #{produces.first || 'application/json'}\""
        query = parameters.select { |param| param["in"] == "query" }.map { |param| "#{param['name']}=..." }
        url = SERVER + path + (query.empty? ? "" : "?#{query.join('&')}")
        lines << "\"#{url}\""
        lines.join(" \\\n  ")
      end
    end

    def cas_actuator_operations(data, endpoint_id)
      CasActuatorsFilter.operations(data, endpoint_id.to_s.strip)
    end

    def cas_actuator_exposed_by_default(endpoint_id)
      EXPOSED_BY_DEFAULT.include?(endpoint_id.to_s)
    end

    def cas_actuator_enable_snippet(ids, format)
      ids = Array(ids).map(&:to_s)
      pairs = ids.map { |id| ["management.endpoint.#{id}.access", "UNRESTRICTED"] }
      pairs << ["management.endpoints.web.exposure.include", ids.join(",")]
      CasActuatorsFilter.snippet(pairs, format)
    end

    def cas_actuator_security_snippet(ids, rule, format)
      pairs = Array(ids).flat_map do |id|
        SECURITY_RULES.fetch(rule.to_s).map { |key, value| ["cas.monitor.endpoints.endpoint.#{id}.#{key}", value] }
      end
      CasActuatorsFilter.snippet(pairs, format)
    end
  end
end

Liquid::Template.register_filter(Jekyll::CasActuatorsFilter)
