package com.cts.mfrp.petz.api.swagger;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * REST Assured Filter that observes every HTTP call and stores a {@link Capture}
 * for it. Registered globally in BaseApiTest so individual tests stay unchanged.
 *
 * <p>Path templating: we use {@code req.getUserDefinedPath()} which returns the
 * original templated form passed to {@code .get("/pets/{id}")}, not the resolved
 * "/pets/42". That's what makes the resulting OpenAPI usable.
 */
public class RecordingFilter implements Filter {

    private static final List<Capture> CAPTURES =
            Collections.synchronizedList(new ArrayList<>());

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public Response filter(FilterableRequestSpecification req,
                           FilterableResponseSpecification res,
                           FilterContext ctx) {

        Response response = ctx.next(req, res);

        try {
            CAPTURES.add(new Capture(
                    req.getMethod(),
                    pathOf(req),
                    bodyOf(req),
                    response.statusCode(),
                    response.getBody() == null ? null : response.getBody().asString(),
                    response.getContentType(),
                    TestContext.current()
            ));
        } catch (Exception ignored) {
            // Recording must never break a test — swallow and continue.
        }
        return response;
    }

    public static List<Capture> captures() {
        synchronized (CAPTURES) {
            return new ArrayList<>(CAPTURES);
        }
    }

    private static String pathOf(FilterableRequestSpecification req) {
        String userPath = req.getUserDefinedPath();
        if (userPath != null && !userPath.isBlank()) return userPath;
        // Fallback: derive from URI if the test passed a full URL.
        String uri = req.getURI();
        int q = uri.indexOf('?');
        return q >= 0 ? uri.substring(0, q) : uri;
    }

    private static String bodyOf(FilterableRequestSpecification req) throws Exception {
        Object body = req.getBody();
        if (body == null) return null;
        if (body instanceof String s) return s;
        // POJO or Map — let Jackson render it the same way REST Assured will.
        return MAPPER.writeValueAsString(body);
    }
}
