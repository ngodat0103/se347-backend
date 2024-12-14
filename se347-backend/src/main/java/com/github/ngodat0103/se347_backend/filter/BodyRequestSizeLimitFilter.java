package com.github.ngodat0103.se347_backend.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.URI;

@AllArgsConstructor
@Slf4j
@Component
public class BodyRequestSizeLimitFilter extends OncePerRequestFilter{
    private static final int MAX_UPLOAD_SIZE = 2 * 1_000_000; // 2MB
    private final ObjectMapper objectMapper;
    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        // Check Content-Length header
        String contentLengthHeader = request.getHeader("Content-Length");
        if (contentLengthHeader != null) {
            long contentLength = Long.parseLong(contentLengthHeader);
            if (contentLength > MAX_UPLOAD_SIZE) {
                log.info("Request body size exceeded: {}", contentLength);
                sendPayloadTooLargeResponse(response);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }


    private void sendPayloadTooLargeResponse(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.PAYLOAD_TOO_LARGE.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        ProblemDetail problemDetail = createProblemDetail();
        byte[] responseBytes = objectMapper.writeValueAsBytes(problemDetail);
        response.getOutputStream().write(responseBytes);
    }
    private ProblemDetail createProblemDetail() {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.PAYLOAD_TOO_LARGE);
        problemDetail.setDetail("Request body size should not exceed " + MAX_UPLOAD_SIZE + " bytes");
        problemDetail.setTitle("Request body size exceeded");
        problemDetail.setType(URI.create("https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/413"));
        return problemDetail;
    }

}
