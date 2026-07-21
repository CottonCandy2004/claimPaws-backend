package cn.czu.claimpaws.notification.infrastructure;

public record WebhookHttpResponse(int statusCode) {
    public boolean isSuccessful() {
        return statusCode >= 200 && statusCode < 300;
    }
}
