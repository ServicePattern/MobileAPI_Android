package com.brightpattern.api;

import android.os.Handler;
import android.util.Log;

import com.brightpattern.api.chat.ChatEventHandler;
import com.brightpattern.api.chat.ChatSession;
import com.brightpattern.api.chat.EventCreator;
import com.brightpattern.api.chat.events.ErrorEvent;
import com.brightpattern.api.chat.events.FileEvent;
import com.brightpattern.api.chat.events.MessageEvent;
import com.brightpattern.api.chat.events.PartyJoinedEvent;
import com.brightpattern.api.chat.events.PartyLeftEvent;
import com.brightpattern.api.chat.events.StartTypingEvent;
import com.brightpattern.api.chat.events.StopTypingEvent;
import com.brightpattern.api.chat.events.signalingdata.AnswerCallData;
import com.brightpattern.api.chat.events.signalingdata.CallStateData;
import com.brightpattern.api.chat.events.signalingdata.EndCallData;
import com.brightpattern.api.chat.events.signalingdata.IceCandidateData;
import com.brightpattern.api.chat.events.signalingdata.RequestCallData;
import com.brightpattern.api.data.ChatInfo.State;
import com.brightpattern.api.data.ChatParameters;
import com.brightpattern.api.data.ChatParty;
import com.brightpattern.api.data.ConnectionConfig;
import com.brightpattern.api.data.FileUploadResult;
import com.brightpattern.api.data.ShowFormResult;
import com.brightpattern.api.chat.events.ChatEvent;
import com.brightpattern.api.chat.events.StateChangeEvent;

import org.apache.http.client.HttpResponseException;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ChatImpl implements Chat {

    private static final String TAG = ChatImpl.class.getSimpleName();

    private final Handler handler;

    private State chatState = State.UNDEFINED;

    private ClientInterface clientInterface;

    private ChatSession currentChatSession;

    private boolean initiated;

    private ChatParty typingParty;

    private ChatParty agentParty; // TODO this will not work for conference

    private List<ChatEvent> chatEvents = new CopyOnWriteArrayList<ChatEvent>();

    private List<ChatEventHandler> chatEventHandlers = new CopyOnWriteArrayList<ChatEventHandler>();

    private ChatEventHandler parentEventHandler = new ChatEventHandler() {
        @Override
        public void onEvent(ChatEvent event) {
            if (event.getType() == ChatEvent.Type.STATE_CHANGE) {
                StateChangeEvent stateChangeEvent = event.cast();
                chatState = stateChangeEvent.getState();
                if (chatState == State.DISCONNECTED) {
                    currentChatSession.stopPolling();
                    typingParty = null;
                }
            } else if (event.getType() == ChatEvent.Type.ERROR) {
                ErrorEvent errorEvent = event.cast();
                Log.e(TAG, "Chat error", errorEvent.getException());
            } else  if (event.getType() == ChatEvent.Type.START_TYPING) {
                StartTypingEvent startTypingEvent = event.cast();
                typingParty = startTypingEvent.getParty();
            } else  if (event.getType() == ChatEvent.Type.STOP_TYPING) {
                StopTypingEvent stopTypingEvent = event.cast();
                typingParty = null; //FIXME for multiple parties
            } else  if (event.getType() == ChatEvent.Type.PARTY_LEFT) {
                if (typingParty != null) {
                    PartyLeftEvent partyLeftEvent = event.cast();
                    if (typingParty.getId().equals(partyLeftEvent.getParty().getId())) {
                        typingParty = null;
                    }
                }
            } else if (event.getType() == ChatEvent.Type.PARTY_JOINED) {
                PartyJoinedEvent partyJoinedEvent = event.cast();
                if (partyJoinedEvent.getParty().getType() == ChatParty.PartyType.INTERNAL) {
                    agentParty = partyJoinedEvent.getParty();
                }
            }
            chatEvents.add(event);
            callHandlers(event);
        }
    };

    public ChatImpl(Handler handler) {
        this.handler = handler;
    }

    @Override
    public ChatParty getTypingChatParty() {
        return typingParty;
    }

    private void setChatState(State state) {
        chatState = state;
        callHandlers(StateChangeEvent.create(state));
    }

    private void callHandlers(final ChatEvent event) {
        for (final ChatEventHandler h : chatEventHandlers) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    h.onEvent(event);
                }
            });
        }
    }

    private <T> void handleSuccessResult(final AsyncCallback<T> callback, final T result) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                callback.onSuccess(result);
            }
        });
    }

    private <T> void handleFailureResult(final AsyncCallback<T> callback, final Throwable t) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                callback.onFailure(t);
            }
        });
    }

    @Override
    public boolean isInitiated() {
        return initiated;
    }

    @Override
    public void init(ConnectionConfig cfg) {
        this.clientInterface = ClientInterfaceFactory.create(cfg);
        this.initiated = true;
    }

    @Override
    public State getState() {
        return chatState;
    }

    @Override
    public String getChatSessionId() {
        return currentChatSession.getId();
    }

    @Override
    public boolean isActiveState() {
        return chatState == State.CONNECTING || chatState == State.CONNECTED || chatState == State.QUEUED;
    }

    @Override
    public List<ChatEvent> getChatEvents() {
        return chatEvents;
    }

    @Override
    public void checkAvailability(final AsyncCallback<Boolean> callback) {
        clientInterface.isChatAvailable(new AsyncCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                handleSuccessResult(callback, result);
            }

            @Override
            public void onFailure(Throwable t) {
                handleFailureResult(callback, t);
            }
        });
    }

    @Override
    public void startNewChat(ChatParameters chatParameters, final AsyncCallback<Boolean> callback) {
        chatEvents.clear();
        typingParty = null;
        setChatState(State.CONNECTING);
        clientInterface.startChatSession(chatParameters, parentEventHandler, new AsyncCallback<ChatSession>() {
            @Override
            public void onSuccess(ChatSession chatSession) {
                currentChatSession = chatSession;
                currentChatSession.startPolling();
                handleSuccessResult(callback, true);
            }

            @Override
            public void onFailure(Throwable t) {
                currentChatSession = null;
                setChatState(State.DISCONNECTED);
                handleFailureResult(callback, t);
            }
        });
    }

    @Override
    public void checkActiveChatWithHistory(final AsyncCallback<Boolean> callback) {
        chatEvents.clear();
        typingParty = null;
        clientInterface.checkActiveChat(parentEventHandler, new AsyncCallback<ChatSession>() {
            @Override
            public void onSuccess(ChatSession chatSession) {
                chatState = chatSession.getState();
                currentChatSession = chatSession;
                currentChatSession.startPolling();
                chatEvents.clear();
                typingParty = null;
                chatSession.requestHistory();
                handleSuccessResult(callback, true);
            }

            @Override
            public void onFailure(Throwable t) {
                if (t instanceof HttpResponseException) {
                    chatState = State.DISCONNECTED;
                    handleSuccessResult(callback, false);
                } else {
                    handleFailureResult(callback, t);
                }
            }
        });
    }

    @Override
    public void sendMessage(String message) {
        currentChatSession.sendMessage(message);
        MessageEvent event = MessageEvent.create(ChatParty.ME, new Date(), message);
        chatEvents.add(event);
        callHandlers(event);
    }

    @Override
    public void sendFormResult(ShowFormResult formResult) {
        currentChatSession.sendFormResult(formResult);
    }

    @Override
    public void sendStartTyping() {
        currentChatSession.startTyping();
    }

    @Override
    public void sendStopTyping() {
        currentChatSession.stopTyping();
    }

    @Override
    public void sendFile(String fileId, EventCreator.FileType fileType) {
        currentChatSession.sendFile(EventCreator.FileType.IMAGE, fileId);
        FileEvent event = FileEvent.create(ChatParty.ME, new Date(), fileType, fileId);
        chatEvents.add(event);
        callHandlers(event);
    }

    @Override
    public void uploadFile(final String filePath, final AsyncCallback<String> callback) {
        clientInterface.uploadFile(new File(filePath), new AsyncCallback<FileUploadResult>() {
            @Override
            public void onSuccess(FileUploadResult fileUploadResult) {
                handleSuccessResult(callback, fileUploadResult.getFileId());
            }

            @Override
            public void onFailure(Throwable t) {
                handleFailureResult(callback, t);
            }
        });
    }

    @Override
    public void finishCurrentChat() {
        if (currentChatSession != null) {
            currentChatSession.endSession();
            currentChatSession.shutdown();
            currentChatSession = null;
        }
        chatEvents.clear();
        typingParty = null;
        setChatState(State.DISCONNECTED);
    }

    @Override
    public void finishCurrentConversation() {
        currentChatSession.endConversation();
    }

    @Override
    public void addChatEventHandler(ChatEventHandler handler) {
        chatEventHandlers.add(handler);
    }

    @Override
    public void removeChatEventHandler(ChatEventHandler handler) {
        chatEventHandlers.remove(handler);
    }


    /**********************************************************************************************/
    @Override
    public void sendCallRequest(Boolean requestVideoCall) {
        currentChatSession.sendSignallingData(RequestCallData.create(requestVideoCall), agentParty.getId());
    }

    @Override
    public void sendIceCandidate(IceCandidateData iceCandidateData) {
        currentChatSession.sendSignallingData(iceCandidateData, agentParty.getId());
    }

    @Override
    public void sendCallAnswer(AnswerCallData answerCallData) {
        currentChatSession.sendSignallingData(answerCallData, agentParty.getId());
    }

    @Override
    public void sendEndCall() {
        currentChatSession.sendSignallingData(EndCallData.create(), agentParty.getId());
    }

    @Override
    public void sendCallState(CallStateData callStateData) {
        currentChatSession.sendSignallingData(callStateData, agentParty.getId());
    }
}
