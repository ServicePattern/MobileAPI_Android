package com.brightpattern.api;

import com.brightpattern.api.chat.ChatEventHandler;
import com.brightpattern.api.chat.ChatSession;
import com.brightpattern.api.data.ChatParameters;
import com.brightpattern.api.data.FileUploadResult;

import java.io.File;

public interface ClientInterface {
	
	void isChatAvailable(AsyncCallback<Boolean> callback);

	void checkActiveChat(ChatEventHandler handler, AsyncCallback<ChatSession> callback);
	
	void startChatSession(ChatParameters chatParameters, ChatEventHandler handler, AsyncCallback<ChatSession> callback);

    void uploadFile(File file, AsyncCallback<FileUploadResult> callback);

	void shutdown();
		
}
