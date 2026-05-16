package com.cts.mfrp.petz.api.swagger;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Turns a flat list of {@link Capture}s into an OpenAPI 3 document (as nested Maps —
 * we don't depend on swagger-core; Jackson will serialise this to JSON).
 *
 * <p>Strategy is intentionally simple: group by (path, method), then under each
 * operation list every request/response body verbatim as a named example tagged
 * with the test that produced it. We do not infer schemas — the examples are the
 * documentation.
 */
public final class OpenApiBuilder {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Pattern PATH_PARAM = Pattern.compile("\\{([^}]+)\\}");

    private OpenApiBuilder() {}

    public static Map<String, Object> build(List<Capture> captures, String serverUrl) {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("openapi", "3.0.3");

        Map<String, Object> info = new LinkedHashMap<>();
        info.put("title", "PETZ API — observed");
        info.put("description",
                "Auto-generated from REST Assured test traffic. Each example is " +
                "tagged with the test method that produced it.");
        info.put("version", "1.0.0");
        root.put("info", info);

        root.put("servers", List.of(Map.of("url", serverUrl)));
        root.put("paths", buildPaths(captures));
        return root;
    }

    private static Map<String, Object> buildPaths(List<Capture> captures) {
        // path → method → captures, both kept sorted for stable output
        Map<String, Map<String, List<Capture>>> grouped = new TreeMap<>();
        for (Capture c : captures) {
            grouped
                    .computeIfAbsent(c.path(), k -> new TreeMap<>())
                    .computeIfAbsent(c.method().toLowerCase(), k -> new ArrayList<>())
                    .add(c);
        }

        Map<String, Object> paths = new LinkedHashMap<>();
        for (var pathEntry : grouped.entrySet()) {
            Map<String, Object> pathItem = new LinkedHashMap<>();
            for (var methodEntry : pathEntry.getValue().entrySet()) {
                pathItem.put(methodEntry.getKey(),
                        buildOperation(methodEntry.getKey(), pathEntry.getKey(), methodEntry.getValue()));
            }
            paths.put(pathEntry.getKey(), pathItem);
        }
        return paths;
    }

    private static Map<String, Object> buildOperation(String method, String path, List<Capture> captures) {
        Map<String, Object> op = new LinkedHashMap<>();
        op.put("summary", method.toUpperCase() + " " + path);

        List<String> tests = captures.stream()
                .map(Capture::testName)
                .distinct()
                .sorted()
                .toList();
        op.put("description", "Observed in tests: " + String.join(", ", tests));

        List<Map<String, Object>> params = pathParameters(path);
        if (!params.isEmpty()) op.put("parameters", params);

        Map<String, Object> requestExamples = new LinkedHashMap<>();
        for (Capture c : captures) {
            if (c.requestBody() == null || c.requestBody().isBlank()) continue;
            Map<String, Object> example = new LinkedHashMap<>();
            example.put("summary", c.testName() + " (→ HTTP " + c.statusCode() + ")");
            example.put("value", parseOrString(c.requestBody()));
            requestExamples.put(uniqueKey(requestExamples, c.testName()), example);
        }
        if (!requestExamples.isEmpty()) {
            op.put("requestBody", Map.of(
                    "content", Map.of(
                            "application/json", Map.of("examples", requestExamples)
                    )
            ));
        }

        op.put("responses", buildResponses(captures));
        return op;
    }

    private static Map<String, Object> buildResponses(List<Capture> captures) {
        Map<Integer, List<Capture>> byStatus = new TreeMap<>();
        for (Capture c : captures) {
            byStatus.computeIfAbsent(c.statusCode(), k -> new ArrayList<>()).add(c);
        }

        Map<String, Object> responses = new LinkedHashMap<>();
        for (var entry : byStatus.entrySet()) {
            int status = entry.getKey();
            Map<String, Object> examples = new LinkedHashMap<>();
            for (Capture c : entry.getValue()) {
                if (c.responseBody() == null || c.responseBody().isBlank()) continue;
                Map<String, Object> ex = new LinkedHashMap<>();
                ex.put("summary", c.testName());
                ex.put("value", parseOrString(c.responseBody()));
                examples.put(uniqueKey(examples, c.testName()), ex);
            }

            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("description",
                    "Observed " + status + " — " + entry.getValue().size() + " call(s).");
            if (!examples.isEmpty()) {
                resp.put("content", Map.of(
                        "application/json", Map.of("examples", examples)
                ));
            }
            responses.put(String.valueOf(status), resp);
        }
        return responses;
    }

    private static List<Map<String, Object>> pathParameters(String path) {
        List<Map<String, Object>> params = new ArrayList<>();
        Matcher m = PATH_PARAM.matcher(path);
        while (m.find()) {
            Map<String, Object> p = new LinkedHashMap<>();
            p.put("name", m.group(1));
            p.put("in", "path");
            p.put("required", true);
            p.put("schema", Map.of("type", "string"));
            params.add(p);
        }
        return params;
    }

    /** Parse JSON to Map/List if possible; otherwise keep the raw string. */
    private static Object parseOrString(String body) {
        try {
            return MAPPER.readValue(body, Object.class);
        } catch (Exception ignored) {
            return body;
        }
    }

    /** Avoid clobbering when two captures share a test name. */
    private static String uniqueKey(Map<String, ?> map, String base) {
        if (!map.containsKey(base)) return base;
        int i = 2;
        while (map.containsKey(base + " #" + i)) i++;
        return base + " #" + i;
    }
}
