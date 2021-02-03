package com.brightpattern.api.data;

public class ConnectionConfig {

    private String serverAddress;

    private String tenantUrl;

    private String appId;

    private String clientId;

    public ConnectionConfig(String serverAddress, String tenantUrl, String appId, String clientId) {
        this.serverAddress = serverAddress;
        this.tenantUrl = tenantUrl;
        this.appId = appId;
        this.clientId = clientId;
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public String getTenantUrl() {
        return tenantUrl;
    }

    public void setTenantUrl(String tenantUrl) {
        this.tenantUrl = tenantUrl;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
}
