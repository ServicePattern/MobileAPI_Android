package com.brightpattern.api;


import com.brightpattern.api.chat.ChatEventHandler;
import com.brightpattern.api.chat.EventCreator;
import com.brightpattern.api.chat.events.signalingdata.AnswerCallData;
import com.brightpattern.api.chat.events.signalingdata.CallStateData;
import com.brightpattern.api.chat.events.signalingdata.IceCandidateData;
import com.brightpattern.api.data.ChatInfo;
import com.brightpattern.api.data.ChatParty;
import com.brightpattern.api.data.ConnectionConfig;
import com.brightpattern.api.data.ChatParameters;
import com.brightpattern.api.data.ShowFormResult;
import com.brightpattern.api.chat.events.ChatEvent;

import java.util.List;

public interface Chat {

    boolean isInitiated();

    void init(ConnectionConfig cfg);

    ChatInfo.State getState();

    ChatParty getTypingChatParty();

    boolean isActiveState();

    List<ChatEvent> getChatEvents();

    void checkAvailability(AsyncCallback<Boolean> callback);

    void startNewChat(ChatParameters chatParameters, AsyncCallback<Boolean> callback);

    void checkActiveChatWithHistory(AsyncCallback<Boolean> callback);

    String getChatSessionId();

    void sendMessage(String message);

    void sendFormResult(ShowFormResult formResult);

    void sendStartTyping();

    void sendStopTyping();

    void uploadFile(final String filePath, final AsyncCallback<String> callback);

    void sendFile(String fileId, EventCreator.FileType fileType);

    void finishCurrentConversation();

    void finishCurrentChat();

    void addChatEventHandler(ChatEventHandler handler);

    void removeChatEventHandler(ChatEventHandler handler);

    //****************************************************************

    void sendCallRequest(Boolean requestVideoCall);

    void sendIceCandidate(IceCandidateData iceCandidateData);

    void sendCallAnswer(AnswerCallData answerCallData);

    void sendEndCall();

    void sendCallState(CallStateData callStateData);

}


