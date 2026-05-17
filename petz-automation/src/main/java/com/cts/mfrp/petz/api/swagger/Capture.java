package com.cts.mfrp.petz.api.swagger;

/**
 * One recorded HTTP exchange — what the filter saw on a single round trip.
 * Pure data; {@link OpenApiBuilder} groups and renders these into OpenAPI.
 */
public record Capture(
        String method,        // GET, POST, ...
        String path,          // templated path, e.g. /pets/{id}
        String requestBody,   // raw request body string (may be null/blank)
        int statusCode,       // HTTP status of the response
        String responseBody,  // raw response body string (may be null/blank)
        String contentType,   // response content type, e.g. application/json
        String testName       // "TC301_PetsTest.petsCRUD_lifecycle"
) {}
