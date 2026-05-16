package com.cts.mfrp.petz.api.swagger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.awt.Desktop;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Writes the OpenAPI spec and a self-contained Swagger UI HTML page, then opens
 * the page in the default browser.
 *
 * <p>The HTML embeds the spec inline as a JS object (no fetch, no CORS, no
 * embedded HTTP server), so it works as a plain {@code file://} URL.
 */
public final class SwaggerReport {

    private static final ObjectMapper JSON = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    private static final String HTML_TEMPLATE = """
            <!doctype html>
            <html lang="en">
            <head>
              <meta charset="utf-8" />
              <title>PETZ API — observed</title>
              <link rel="stylesheet" href="https://unpkg.com/swagger-ui-dist@5/swagger-ui.css" />
              <style> body { margin: 0; } </style>
            </head>
            <body>
              <div id="ui"></div>
              <script src="https://unpkg.com/swagger-ui-dist@5/swagger-ui-bundle.js"></script>
              <script>
                window.onload = function () {
                  window.ui = SwaggerUIBundle({
                    dom_id: '#ui',
                    spec: __SPEC__,
                    deepLinking: true,
                    presets: [SwaggerUIBundle.presets.apis]
                  });
                };
              </script>
            </body>
            </html>
            """;

    private SwaggerReport() {}

    /**
     * @param captures  the recorded HTTP exchanges
     * @param serverUrl base URL to show under "servers" in the UI
     * @param outputDir directory to drop openapi.json + index.html into
     * @param openInBrowser  if true, attempt Desktop.browse on the generated HTML
     */
    public static void write(List<Capture> captures, String serverUrl,
                             Path outputDir, boolean openInBrowser) {
        if (captures.isEmpty()) {
            System.out.println("[swagger] no captures recorded — skipping report.");
            return;
        }

        try {
            Files.createDirectories(outputDir);
            Map<String, Object> spec = OpenApiBuilder.build(captures, serverUrl);
            String specJson = JSON.writeValueAsString(spec);

            Path jsonFile = outputDir.resolve("openapi.json");
            Path htmlFile = outputDir.resolve("index.html");
            Files.writeString(jsonFile, specJson);
            Files.writeString(htmlFile, HTML_TEMPLATE.replace("__SPEC__", specJson));

            System.out.println("[swagger] wrote " + jsonFile.toAbsolutePath());
            System.out.println("[swagger] wrote " + htmlFile.toAbsolutePath());

            if (openInBrowser) openInBrowser(htmlFile);
        } catch (IOException e) {
            System.err.println("[swagger] failed to write report: " + e.getMessage());
        }
    }

    private static void openInBrowser(Path htmlFile) {
        try {
            if (Desktop.isDesktopSupported()
                    && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(htmlFile.toUri());
                System.out.println("[swagger] opened in default browser.");
            } else {
                System.out.println("[swagger] desktop browse unsupported — open manually: "
                        + htmlFile.toAbsolutePath());
            }
        } catch (Exception e) {
            System.out.println("[swagger] could not open browser (" + e.getMessage()
                    + ") — open manually: " + htmlFile.toAbsolutePath());
        }
    }
}
