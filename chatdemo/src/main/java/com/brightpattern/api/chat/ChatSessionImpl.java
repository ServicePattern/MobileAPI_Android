package com.brightpattern.api.chat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import com.brightpattern.api.ApiWrapper;
import com.brightpattern.api.AsyncCallback;
import com.brightpattern.api.chat.events.ChatEvent;
import com.brightpattern.api.chat.events.ErrorEvent;
import com.brightpattern.api.chat.events.FileEvent;
import com.brightpattern.api.chat.events.InactivityTimeoutEvent;
import com.brightpattern.api.chat.events.MessageEvent;
import com.brightpattern.api.chat.events.PartyJoinedEvent;
import com.brightpattern.api.chat.events.PartyLeftEvent;
import com.brightpattern.api.chat.events.SignalingDataEvent;
import com.brightpattern.api.chat.events.SessionEndedEvent;
import com.brightpattern.api.chat.events.ShowFormEvent;
import com.brightpattern.api.chat.events.StartTypingEvent;
import com.brightpattern.api.chat.events.StateChangeEvent;
import com.brightpattern.api.chat.events.StopTypingEvent;
import com.brightpattern.api.chat.events.TimeoutWarningEvent;
import com.brightpattern.api.chat.events.signalingdata.SignalingData;
import com.brightpattern.api.data.ChatInfo;
import com.brightpattern.api.data.ChatInfo.State;
import com.brightpattern.api.data.ChatParty;
import com.brightpattern.api.data.ChatParty.PartyType;
import com.brightpattern.api.data.ShowFormData;
import com.brightpattern.api.data.ShowFormResult;
import com.brightpattern.api.utils.RequestTask;
import com.google.gson.JsonObject;

public class ChatSessionImpl implements ChatSession {

	private ApiWrapper apiWrapper;

	private String id;

	private String phoneNumber;

	private String ewt;

	private volatile AtomicReference<State> state = new AtomicReference<ChatInfo.State>(State.UNDEFINED);

	private ChatEventHandler eventsHandler;

	private Map<String, ChatParty> chatParties = new ConcurrentHashMap<String, ChatParty>();

	private ThreadPoolExecutor executor;

	private Thread pollingThread;

	private Runnable pollingRunnable = new Runnable() {

		@Override
		public void run() {
			int errorsCount = 0;

			while (!Thread.currentThread().isInterrupted() && state.get() != State.DISCONNECTED) {				
				try {
					final List<JsonObject> events = apiWrapper.getChatEvents(id);					
					if (!events.isEmpty()) {
						if (!executor.isShutdown()) { 
							executor.submit(new Runnable() {							
								@Override
								public void run() {
									processEvents(parseEvents(events, false));
								}
                                      });
						} else {
                            processEvents(parseEvents(events, true));
						}
					}
				} catch (final Exception e) {
                    if (state.get() == State.DISCONNECTED) {
                        return;
                    }
					handleException(e);
					if (errorsCount > 3) {
						state.set(State.DISCONNECTED);
						eventsHandler.onEvent(StateChangeEvent.create(State.DISCONNECTED));
						shutdown();
					}
					errorsCount++;										
				}					
			}
		}

		private void handleException(final Exception e) {
			if (!executor.isShutdown()) {
				executor.submit(new Runnable() {							
					@Override
					public void run() {
						eventsHandler.onEvent(ErrorEvent.create(e));
					}
				});
			} else {
                eventsHandler.onEvent(ErrorEvent.create(e));
			}
		}				
	};

	private AsyncCallback<Void> defaultCallback = new AsyncCallback<Void>() {

		@Override
		public void onSuccess(Void t) {
		}

		@Override
		public void onFailure(Throwable t) {
            eventsHandler.onEvent(ErrorEvent.create(t));
		}
	};

	public static ChatSession create(ChatInfo chatInfo, ApiWrapper apiWrapper, ChatEventHandler handler) {
		ChatSessionImpl chat = new ChatSessionImpl();
		chat.apiWrapper    = apiWrapper;
		chat.ewt           = chatInfo.getEwt();
		chat.id            = chatInfo.getChatId();
		chat.phoneNumber   = chatInfo.getPhoneNumber();
		chat.state.set(chatInfo.getState());
		chat.eventsHandler = handler;

		chat.executor = new ThreadPoolExecutor(2, 5, 30, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

		return chat;
	}

    private List<ChatEvent> parseEvents(List<JsonObject> events, boolean historyEvents) {
        List<ChatEvent> chatEvents = new ArrayList<ChatEvent>();

        for (JsonObject event : events) {
            String eventType = event.get("event").getAsString();
            if ("chat_session_status".equals(eventType)) {
                String state = event.get("state").getAsString();
                if ("failed".equals(state)) {
                    this.state.set(State.DISCONNECTED);
                    chatEvents.add(StateChangeEvent.create(State.DISCONNECTED));
                    shutdown();
                } else if ("queued".equals(state)) {
                    this.state.set(State.QUEUED);
                    chatEvents.add(StateChangeEvent.create(State.QUEUED));
                } else if ("connected".equals(state)) {
                    this.state.set(State.CONNECTED);
                    chatEvents.add(StateChangeEvent.create(State.CONNECTED));
                } else if ("connecting".equals(state)) {
                    this.state.set(State.CONNECTING);
                    chatEvents.add(StateChangeEvent.create(State.CONNECTING));
                }
            } else if ("chat_session_ended".equals(eventType)) {
                this.state.set(State.DISCONNECTED);
                chatEvents.add(SessionEndedEvent.create());
                chatEvents.add(StateChangeEvent.create(State.DISCONNECTED));
                shutdown();
            } else if ("chat_session_party_joined".equals(eventType)) {
                ChatParty chatParty = ChatParty.create(event);
                chatParties.put(chatParty.getId(), chatParty);
                chatEvents.add(PartyJoinedEvent.create(getTimestamp(event), chatParty));
            } else if ("chat_session_party_left".equals(eventType)) {
                String partyId = event.get("party_id").getAsString();
                chatEvents.add(PartyLeftEvent.create(getTimestamp(event), getChatParty(partyId)));
            } else if ("chat_session_message".equals(eventType)) {
                String partyId = event.get("party_id").getAsString();
                String message = event.get("msg").getAsString();
                chatEvents.add(MessageEvent.create(getChatParty(partyId), getTimestamp(event), message));
            } else if ("chat_session_signaling".equals(eventType)) {
                if (!historyEvents) { //TODO filter out signalling messages from history on server
                    String partyId = event.get("party_id").getAsString();
                    JsonObject data = event.get("data").getAsJsonObject();
                    chatEvents.add(SignalingDataEvent.create(getChatParty(partyId), getTimestamp(event), data));
                }
            } else if ("chat_session_file".equals(eventType)) {
                String partyId = event.get("session_id").getAsString(); //TODO fix to partyId after server fix
                String fileId = event.get("file_id").getAsString();
                String fileType = event.get("file_type").getAsString();
                chatEvents.add(FileEvent.create(getChatParty(partyId), getTimestamp(event), fileType, fileId));
            } else if ("chat_session_typing".equals(eventType)) {
                String partyId = event.get("party_id").getAsString();
                chatEvents.add(StartTypingEvent.create(getChatParty(partyId)));
            } else if ("chat_session_not_typing".equals(eventType)) {
                String partyId = event.get("party_id").getAsString();
                chatEvents.add(StopTypingEvent.create(getChatParty(partyId)));
            } else if ("chat_session_form_show".equals(eventType)) {
                ShowFormData data = ShowFormData.create(event);
                chatEvents.add(ShowFormEvent.create(data));
            } else if ("chat_session_timeout_warning".equals(eventType)) {
                String msg = event.get("msg").getAsString();
                chatEvents.add(TimeoutWarningEvent.create(getTimestamp(event), msg));
            } else if ("chat_session_inactivity_timeout".equals(eventType)) {
                String msg = event.get("msg").getAsString();
                this.state.set(State.DISCONNECTED);
                chatEvents.add(InactivityTimeoutEvent.create(getTimestamp(event), msg));
                chatEvents.add(StateChangeEvent.create(State.DISCONNECTED));
                shutdown();
            }
        }

        return chatEvents;
    }

	private void processEvents(List<ChatEvent> chatEvents) {
		for (ChatEvent event : chatEvents) {
            eventsHandler.onEvent(event);
		}
	}

    private Date getTimestamp(JsonObject event) {
        if (event.has("timestamp")) {
            return new Date(Long.parseLong(event.get("timestamp").getAsString()) * 1000);
        } else {
            return new Date();
        }
    }

    private ChatParty getChatParty(String partyId) {
		if (id.equals(partyId)) {
			return new ChatParty(partyId, "", "", "", PartyType.ME);
		} else if (chatParties.containsKey(partyId)) {
			return chatParties.get(partyId);
		} else {
			return new ChatParty(partyId, "", "", "", PartyType.UNDEFINED);
		}		
	}

	private void sendEvent(final JsonObject ... events) {
        if (getState() != State.DISCONNECTED) {
            executor.execute(new RequestTask<Void>(apiWrapper, defaultCallback) {
                @Override
                protected Void request(ApiWrapper apiWrapper) throws Throwable {
                    apiWrapper.sendChatEvents(id, events);
                    return null;
                }
            });
        }
	}

    @Override
    public String getId() {
        return id;
    }

    @Override
	public State getState() {
		return state.get();
	}

    @Override
    public void sendSignallingData(SignalingData signalingData, String agentPartyId) {
        sendEvent(EventCreator.createSignallingData(signalingData, agentPartyId));
    }

    @Override
	public void sendMessage(String message) {
		sendEvent(EventCreator.createMessage(message));
	}	

	@Override
	public void sendFile(EventCreator.FileType fileType, String fileId) {
        sendEvent(EventCreator.createFile(fileType, fileId));
	}

	@Override
	public void sendFormResult(ShowFormResult formResult) {
		sendEvent(formResult.toJson());
	}

	@Override
	public void startTyping() {
		sendEvent(EventCreator.startTyping());		
	}

	@Override
	public void stopTyping() {
		sendEvent(EventCreator.stopTyping());
	}

	@Override
	public void endSession() {
		sendEvent(EventCreator.endSession());
	}

    @Override
    public void endConversation() {
        sendEvent(EventCreator.disconnectSession());
    }

    @Override
    public void requestHistory() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    List<JsonObject> chatHistory = apiWrapper.getChatHistory(id);
                    processEvents(parseEvents(chatHistory, true));
                } catch (Throwable t) {
                    eventsHandler.onEvent(ErrorEvent.create(t));
                }
            }
        });
    }

	@Override
	public synchronized void shutdown() {
		this.state.set(State.DISCONNECTED);
		executor.shutdownNow();
		pollingThread.interrupt();		
	}

    @Override
    public synchronized void startPolling() {
        if (pollingThread == null || pollingThread.isInterrupted()) {
            pollingThread = new Thread(pollingRunnable, "Chat polling: " + id);
            pollingThread.start();
        }
        executor.allowCoreThreadTimeOut(false);
    }

    @Override
    public synchronized void stopPolling() {
        if (pollingThread != null) {
            pollingThread.interrupt();
        }
        executor.allowCoreThreadTimeOut(true);
    }
}
