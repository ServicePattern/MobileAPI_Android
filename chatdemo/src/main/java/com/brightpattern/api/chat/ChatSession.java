package com.brightpattern.api.chat;

import com.brightpattern.api.chat.events.signalingdata.SignalingData;
import com.brightpattern.api.data.ChatInfo.State;
import com.brightpattern.api.data.ShowFormResult;

public interface ChatSession {

    String getId();

    void startPolling();

    void stopPolling();

	void sendMessage(String message);
	
	void sendFile(EventCreator.FileType fileType, String fileId);
	
	void sendFormResult(ShowFormResult data);

    void requestHistory();
	
	void startTyping();
	
	void stopTyping();
	
	void endSession();

    void endConversation();
	
	void shutdown();

	State getState();

    void sendSignallingData(SignalingData sessionData, String agentPartyId);
}
