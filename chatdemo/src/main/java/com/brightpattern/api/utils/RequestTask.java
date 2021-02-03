package com.brightpattern.api.utils;

import com.brightpattern.api.ApiWrapper;
import com.brightpattern.api.AsyncCallback;

public abstract class RequestTask<T> implements Runnable {		
	
	private AsyncCallback<T> callback;
	private ApiWrapper apiWrapper;

	public RequestTask(ApiWrapper apiWrapper, AsyncCallback<T> callback) {
		this.callback = callback;			
		this.apiWrapper = apiWrapper;
	}

	@Override
	public void run() {
		try {
			callback.onSuccess(request(apiWrapper));
		} catch (Throwable t) {
			callback.onFailure(t);
		}
	}

	protected abstract T request(ApiWrapper apiWrapper) throws Throwable;
}