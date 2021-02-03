package com.brightpattern.api.utils;

import android.util.Log;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.util.EntityUtils;

public abstract class BaseStringResponseHandler<T> implements ResponseHandler<T> {

    private static final String TAG = BaseStringResponseHandler.class.getSimpleName();

    private boolean logResponse = false;

    public void setLogResponse(boolean logResponse) {
        this.logResponse = logResponse;
    }

    @Override
	public T handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
		int code = response.getStatusLine().getStatusCode();		
		if (code >= 200 && code < 300) {
            String body = EntityUtils.toString(response.getEntity());
            if (logResponse) {
                Log.d(TAG, "RESPONSE:\n " + response.getStatusLine() + "\n" + body);
            }
			return handleSuccessResponse(response, body);
		} else {
			throw getErrorResponseException(response);
		}
	}
	
	public abstract T handleSuccessResponse(HttpResponse response, String body) throws IOException;
	
	protected IOException getErrorResponseException(HttpResponse response) throws IOException {
		StatusLine status = response.getStatusLine();
		int code = status.getStatusCode();
        //TODO add error message parsing
		throw new HttpResponseException(code, status.getReasonPhrase());
	}
}
