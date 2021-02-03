package com.brightpattern.api;

import java.io.File;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.brightpattern.api.chat.ChatEventHandler;
import com.brightpattern.api.chat.ChatSession;
import com.brightpattern.api.chat.ChatSessionImpl;
import com.brightpattern.api.data.ChatInfo;
import com.brightpattern.api.data.ChatParameters;
import com.brightpattern.api.data.FileUploadResult;
import com.brightpattern.api.utils.RequestTask;

public class ClientInterfaceImpl implements ClientInterface {
	
	private ApiWrapper apiWrapper;
	
	private ThreadPoolExecutor executor;
	
	public ClientInterfaceImpl(ApiWrapper apiWrapper) {
		this.apiWrapper = apiWrapper;
		this.executor = new ThreadPoolExecutor(2, 5, 30, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
        this.executor.allowCoreThreadTimeOut(true);
	}

	@Override
	public void isChatAvailable(AsyncCallback<Boolean> callback) {
		executor.execute(new RequestTask<Boolean>(apiWrapper, callback) {
			@Override
			protected Boolean request(ApiWrapper apiWrapper) throws Throwable {
				return apiWrapper.checkAvailability().isChatAvailable();
			}			
		});
	}

	@Override
	public void checkActiveChat(final ChatEventHandler handler, AsyncCallback<ChatSession> callback) {
		executor.execute(new RequestTask<ChatSession>(apiWrapper, callback) {
			@Override
			protected ChatSession request(ApiWrapper apiWrapper) throws Throwable {
				ChatInfo activeChat = apiWrapper.getActiveChat();
				return createChatSession(activeChat, handler);
			}
		});
	}

	@Override
	public void startChatSession(final ChatParameters chatParameters, final ChatEventHandler handler, AsyncCallback<ChatSession> callback) {
		executor.execute(new RequestTask<ChatSession>(apiWrapper, callback) {
			@Override
			protected ChatSession request(ApiWrapper apiWrapper) throws Throwable {
				ChatInfo activeChat = apiWrapper.requestChat(chatParameters);
				return createChatSession(activeChat, handler);
			}
		});
	}

    @Override
    public void uploadFile(final File file, AsyncCallback<FileUploadResult> callback) {
        executor.execute(new RequestTask<FileUploadResult>(apiWrapper, callback) {
            @Override
            protected FileUploadResult request(ApiWrapper apiWrapper) throws Throwable {
                return apiWrapper.uploadFile(file);
            }
        });
    }

    private ChatSession createChatSession(ChatInfo chatInfo, ChatEventHandler handler) {
		return ChatSessionImpl.create(chatInfo, apiWrapper, handler);
	}

	@Override
	public void shutdown() {
		executor.shutdown();		
	}	
}
