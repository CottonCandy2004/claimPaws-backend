package cn.czu.claimpaws.notification.infrastructure;

import cn.czu.claimpaws.notification.application.PinnedWebhookEndpoint;
import cn.czu.claimpaws.notification.config.WebhookProperties;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import javax.net.ssl.SNIHostName;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.time.Duration;
import org.springframework.stereotype.Component;

@Component
public class JdkWebhookHttpClient implements WebhookHttpClient {
    private final WebhookProperties properties;

    public JdkWebhookHttpClient(WebhookProperties properties) {
        this.properties = properties;
    }

    @Override
    public WebhookHttpResponse post(PinnedWebhookEndpoint endpoint, String eventId, String eventType, String timestamp,
                                    String signature, String payload) throws IOException, InterruptedException {
        byte[] body = payload.getBytes(StandardCharsets.UTF_8);
        try (Socket rawSocket = new Socket()) {
            rawSocket.connect(new java.net.InetSocketAddress(endpoint.address(), endpoint.port()), properties.connectTimeout());
            rawSocket.setSoTimeout(properties.readTimeout());
            try (SSLSocket socket = (SSLSocket) ((SSLSocketFactory) SSLSocketFactory.getDefault())
                    .createSocket(rawSocket, endpoint.host(), endpoint.port(), true)) {
                SSLParameters parameters = socket.getSSLParameters();
                parameters.setEndpointIdentificationAlgorithm("HTTPS");
                parameters.setServerNames(java.util.List.of(new SNIHostName(endpoint.host())));
                socket.setSSLParameters(parameters);
                socket.startHandshake();
                try (OutputStream output = socket.getOutputStream(); InputStream input = socket.getInputStream()) {
                    String host = endpoint.port() == 443 ? endpoint.host() : endpoint.host() + ":" + endpoint.port();
                    String request = "POST " + endpoint.uri().getRawPath() +
                            (endpoint.uri().getRawQuery() == null ? "" : "?" + endpoint.uri().getRawQuery()) + " HTTP/1.1\r\n"
                            + "Host: " + host + "\r\nConnection: close\r\nContent-Type: application/json\r\n"
                            + "Content-Length: " + body.length + "\r\nX-Webhook-Id: " + eventId + "\r\n"
                            + "X-Webhook-Event: " + eventType + "\r\nX-Webhook-Timestamp: " + timestamp + "\r\n"
                            + "X-Webhook-Signature: sha256=" + signature + "\r\n\r\n";
                    output.write(request.getBytes(StandardCharsets.US_ASCII));
                    output.write(body);
                    output.flush();
                    String statusLine = new String(input.readNBytes(64), StandardCharsets.US_ASCII).split("\\r?\\n", 2)[0];
                    String[] parts = statusLine.split(" ");
                    if (parts.length < 2) throw new IOException("Invalid webhook HTTP response");
                    return new WebhookHttpResponse(Integer.parseInt(parts[1]));
                }
            }
        }
    }
}
