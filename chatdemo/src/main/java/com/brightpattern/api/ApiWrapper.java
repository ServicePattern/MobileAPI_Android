package com.brightpattern.api;

import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.http.HttpMessage;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.util.EntityUtils;

import com.brightpattern.api.data.Availability;
import com.brightpattern.api.data.ChatInfo;
import com.brightpattern.api.data.ChatParameters;
import com.brightpattern.api.data.FileUploadResult;
import com.brightpattern.api.utils.BaseStringResponseHandler;
import com.brightpattern.api.utils.JsonArrayResponseHandler;
import com.brightpattern.api.utils.JsonResponseHandler;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class ApiWrapper {

    private static final String TAG = ApiWrapper.class.getSimpleName();

    private String authHeader;
	
	private HttpClient httpClient;
	
	private String baseUrl;
	
	private boolean useTenantUrlParameter;

	private String tenantUrl;

    private boolean logRequestResponse = true;
	
    public ApiWrapper(String serverAddress,  String tenantUrl, String appId, String clientId) {
        this.tenantUrl = tenantUrl;

        SchemeRegistry registry = new SchemeRegistry();

        if (!serverAddress.contains("://")) {
            serverAddress = "http://" + serverAddress;
        }

        URI uri = URI.create(serverAddress);

        int port = uri.getPort();
        if (port < 0) {
            port = 98;
        }

        if ("https".equalsIgnoreCase(uri.getScheme())) {
            registry.register(new Scheme(uri.getScheme(), SSLSocketFactory.getSocketFactory(), port));
        } else {
            registry.register(new Scheme(uri.getScheme(), PlainSocketFactory.getSocketFactory(), port));
        }

        ThreadSafeClientConnManager connectionManager = new ThreadSafeClientConnManager(new BasicHttpParams(), registry);

        this.httpClient = new DefaultHttpClient(connectionManager, new BasicHttpParams());

        this.authHeader = "MOBILE-API-140-327-PLAIN appId=\""+appId+"\", clientId=\""+clientId+"\"";
        if (!serverAddress.endsWith("/")) {
        	serverAddress = serverAddress + "/";
        }
        this.baseUrl = serverAddress + "clientweb/api/v1/";
        useTenantUrlParameter = !serverAddress.contains(tenantUrl);
    }    
    
    public Availability checkAvailability() throws URISyntaxException, IOException {    	
        return sendGet(buildURI("availability"), new JsonResponseHandler<Availability>(Availability.class));        
    }
    
    public ChatInfo requestChat(ChatParameters params) throws IOException, URISyntaxException {    	
    	return sendPost(buildURI("chats"), params, new JsonResponseHandler<ChatInfo>(ChatInfo.class));
    }
    
    public ChatInfo getActiveChat() throws ClientProtocolException, IOException, URISyntaxException {    	
    	return sendGet(buildURI("chats/active"), new JsonResponseHandler<ChatInfo>(ChatInfo.class));
    }
    
    public List<JsonObject> getChatHistory(String chatId) throws IOException, URISyntaxException {
    	return sendGet(buildURI("chats/"+chatId+"/history"), new JsonArrayResponseHandler("events"));
    }
    
    public List<JsonObject> getChatEvents(String chatId) throws IOException, URISyntaxException {
    	return sendGet(buildURI("chats/"+chatId+"/events"), new JsonArrayResponseHandler("events"));
    }
    
    public List<JsonObject> sendChatEvents(String chatId, JsonObject[] events) throws IOException, URISyntaxException {
    	JsonArray array = new JsonArray();
    	for (JsonElement e : events) {
    		array.add(e);
    	}
    	JsonObject obj = new JsonObject();    	
    	obj.add("events", array);
    	return sendPost(buildURI("chats/"+chatId+"/events"), obj, new JsonArrayResponseHandler("events"));
    }

    public FileUploadResult uploadFile(File file) throws IOException, URISyntaxException {
        return uploadFile(buildURI("files"), file, new JsonResponseHandler<FileUploadResult>(FileUploadResult.class));
    }
    
    private URI buildURI(String method) throws URISyntaxException {
    	return buildURI(method, "");
    }
    
    private URI buildURI(String method, String query) throws URISyntaxException {    	
    	StringBuilder sb = new StringBuilder(baseUrl);    	
    	sb.append(method);
    	if (useTenantUrlParameter) {
    		sb.append("?tenantUrl=").append(tenantUrl);
    		if (!query.isEmpty()) {
    			sb.append("&");
    			sb.append(query);
    		}
    	} else {
    		if (!query.isEmpty()) {
    			sb.append("?");
    			sb.append(query);
    		}
    	}
    	return new URI(sb.toString());
    }
    
    
    private <T> T sendGet(URI uri, final ResponseHandler<T> responseHandler) throws IOException {
    	HttpGet httpGet = new HttpGet(uri);
        setHeaders(httpGet);

        if (logRequestResponse) {
            Log.d(TAG, "REQUEST:\n " + uri.toString());
            if (responseHandler instanceof BaseStringResponseHandler) {
                ((BaseStringResponseHandler) responseHandler).setLogResponse(true);
            }
        }
        return httpClient.execute(httpGet, responseHandler);
    }
    
    private <T> T sendPost(URI uri, Object body, final ResponseHandler<T> responseHandler) throws IOException {
    	HttpPost httpPost = new HttpPost(uri);    	
        setHeaders(httpPost);
        String str;
        if (body instanceof JsonElement) {
        	str = ((JsonElement)body).toString();        	
        } else {
        	Gson gson = new Gson();
        	str = gson.toJson(body);
        }        
        httpPost.setEntity(new StringEntity(str, "UTF-8"));

        if (logRequestResponse) {
            Log.d(TAG, "REQUEST:\n " + uri.toString() + "\n" + str);
            if (responseHandler instanceof BaseStringResponseHandler) {
                ((BaseStringResponseHandler) responseHandler).setLogResponse(true);
            }
        }
        return httpClient.execute(httpPost, responseHandler);
    }

    private <T> T uploadFile(URI uri, File file, ResponseHandler<T> responseHandler) throws IOException {
        HttpPost httpPost = new HttpPost(uri);
        setAuthHeader(httpPost);
        setAcceptHeader(httpPost);
        setUserAgent(httpPost);
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addPart("file",new FileBody(file, ContentType.APPLICATION_OCTET_STREAM, file.getName()));
        httpPost.setEntity(builder.build());
        return httpClient.execute(httpPost, responseHandler);
    }
    
    private void setHeaders(HttpMessage httpMessage) {
        setAuthHeader(httpMessage);
        setAcceptHeader(httpMessage);
        setContentType(httpMessage);
        setUserAgent(httpMessage);
    }

    private void setContentType(HttpMessage httpMessage) {
        httpMessage.addHeader("Content-Type", "application/json; charset=UTF-8");
    }

    private void setUserAgent(HttpMessage httpMessage) {
        httpMessage.addHeader("User-Agent", "MobileClient");
    }

    private void setAcceptHeader(HttpMessage httpMessage) {
        httpMessage.addHeader("Accept", "application/json");
    }

    private void setAuthHeader(HttpMessage httpMessage) {
        httpMessage.addHeader("Authorization", authHeader);
    }
}
