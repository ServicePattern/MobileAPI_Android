package com.brightpattern.api;

public interface AsyncCallback<T> {
	
	void onSuccess(T t);
	
	void onFailure(Throwable t);
}
