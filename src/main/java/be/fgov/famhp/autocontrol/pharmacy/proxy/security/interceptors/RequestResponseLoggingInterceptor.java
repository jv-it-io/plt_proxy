package be.fgov.famhp.autocontrol.pharmacy.proxy.security.interceptors;

import com.google.common.collect.ImmutableSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toSet;

public class RequestResponseLoggingInterceptor implements ClientHttpRequestInterceptor {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        logRequest(request, body);
        ClientHttpResponse response = execution.execute(request, body);
        logResponse(response);
        return response;
    }

    private void logRequest(HttpRequest request, byte[] body) throws IOException {
//        if (log.isDebugEnabled()) {
            log.info("===========================request begin================================================");
            log.info("URI            : {}", request.getURI());
            log.info("PATH           : {}", request.getURI().getPath());
            log.info("Method         : {}", request.getMethod());
            log.info("Headers        : {}", request.getHeaders());
            if (isTextContent(request.getHeaders().getContentType())) {
                log.info("Request body   : {}", new String(body, "UTF-8"));
            }
            log.info("==========================request end================================================");
//        }
    }

    private void logResponse(ClientHttpResponse response) throws IOException {
//        if (log.isDebugEnabled()) {
            log.info("============================response begin==========================================");
            log.info("Status code    : {}", response.getStatusCode());
            log.info("Headers        : {}", response.getHeaders());
            log.info("Content type   : {}", response.getHeaders().getContentType());
            if (isTextContent(response.getHeaders().getContentType())) {
                log.info("Response body  : {}", StreamUtils.copyToString(response.getBody(), Charset.defaultCharset()));
            }
            log.info("=======================response end=================================================");
//        }
    }

    private boolean isTextContent(MediaType mediaType) {
        if (mediaType == null) return false;

        Set<MediaType> textMedia = Stream.of(
            MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_PLAIN, MediaType.TEXT_HTML
        ).collect(collectingAndThen(toSet(), ImmutableSet::copyOf));

        return mediaType.isPresentIn(textMedia);
    }
}
