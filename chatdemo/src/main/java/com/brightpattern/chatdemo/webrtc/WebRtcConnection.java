package com.brightpattern.chatdemo.webrtc;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.brightpattern.api.chat.events.signalingdata.CallStateData;

import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.Logging;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RtpReceiver;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebRtcConnection {

    private static final String TAG = WebRtcConnection.class.getSimpleName();

    private AudioSource localAudioSource;
    private Context applicationContext;

    public enum State {NOT_ACTIVE, CONNECTING, CONNECTED}

    public interface StateListener {
        void onLocalStreamStateChange(boolean audioEnabled, boolean videoEnabled);

        void onRemoteStreamStateChange(boolean audioEnabled, boolean videoEnabled);

        void onConnectionStateChange(State state);
    }

    private Set<StateListener> listeners = new CopyOnWriteArraySet<StateListener>();

    private PeerConnectionFactory factory;
    private final Handler handler;
    private Signaling signaling;
    private State state = State.NOT_ACTIVE;

    private SDPObserver sdpObserver;

    private PCObserver pcObserver;

    private CallStateData localCallState = new CallStateData();
    private CallStateData remoteCallState = new CallStateData();

    private Signaling.SignalingListener listener = new Signaling.SignalingListener() {
        @Override
        public void onOffer(String sdp) {
            if (pc != null) {
                SessionDescription sessionDescription = new SessionDescription(SessionDescription.Type.OFFER, preferISAC(sdp));
                pc.setRemoteDescription(sdpObserver, sessionDescription);
            }
        }

        @Override
        public void onIceCandidate(String id, int index, String candidateDescription) {
            if (pc != null) {
                pc.addIceCandidate(new IceCandidate(id, index, candidateDescription));
            }
        }

        @Override
        public void onCallState(boolean audioEnabled, boolean videoEnabled) {
            remoteCallState.setAudioEnabled(audioEnabled);
            remoteCallState.setVideoEnabled(videoEnabled);
            onRemoteStreamStateChange(audioEnabled, videoEnabled);
        }

        @Override
        public void onEndCall() {
            dispose();
        }
    };
    private volatile PeerConnection pc;

    private LinkedList<IceCandidate> queuedRemoteCandidates;
    private VideoSource videoSource;

    private VideoCapturer localVideoCapturer;
    private VideoTrack localVideoTrack;
    private VideoRenderer localVideoRenderer;
    private VideoRenderer.Callbacks localVideoRendererCallback;
    private AudioTrack localAudioTrack;

    private VideoTrack remoteVideoTrack;
    private VideoRenderer remoteVideoRenderer;
    private VideoRenderer.Callbacks remoteVideoRendererCallback;

    private boolean factoryStaticInitialized;

    public WebRtcConnection(Signaling signaling, Handler handler) {
        this.signaling = signaling;
        this.handler = handler;
    }

    public State getState() {
        return state;
    }

    public boolean isActive() {
        return state != State.NOT_ACTIVE;
    }

    public void registerSignallingListener() {
        signaling.addListener(listener);
    }

    public void unregisterSignallingListener() {
        signaling.removeListener(listener);
    }

    public void setVideoRenderCallbacks(VideoRenderer.Callbacks local, VideoRenderer.Callbacks remote) {
        this.localVideoRendererCallback = local;
        this.remoteVideoRendererCallback = remote;

        if (localVideoTrack != null) {
            localVideoRenderer = new VideoRenderer(localVideoRendererCallback);
            localVideoTrack.addRenderer(localVideoRenderer);
        }
        if (remoteVideoTrack != null) {
            remoteVideoRenderer = new VideoRenderer(remoteVideoRendererCallback);
            remoteVideoTrack.addRenderer(remoteVideoRenderer);
        }
    }

    public void removeAllVideoRenderCallbacks() {
        if (localVideoRenderer != null) {
            localVideoTrack.removeRenderer(localVideoRenderer);
            localVideoRenderer = null;
        }
        if (remoteVideoRenderer != null) {
            remoteVideoTrack.removeRenderer(remoteVideoRenderer);
            remoteVideoRenderer = null;
        }
    }

    public void sendCallRequest(boolean videoCall) {
        signaling.requestCall(videoCall);
        localCallState.setAudioEnabled(true);
        localCallState.setVideoEnabled(true);
    }


    public void endCall() {
        dispose();
        signaling.endCall();
    }

    public boolean isMuted() {
        return !localAudioTrack.enabled();
    }

    public boolean isLocalVideoEnabled() {
        return localVideoTrack != null && localVideoTrack.enabled();
    }

    public boolean isRemoteVideoEnabled() {
        return remoteVideoTrack != null && remoteVideoTrack.enabled();
    }

    public void mute() {
        localAudioTrack.setEnabled(false);
        localCallState.setAudioEnabled(false);
        sendLocalCallState();
    }

    public void unmute() {
        localAudioTrack.setEnabled(true);
        localCallState.setAudioEnabled(true);
        sendLocalCallState();
    }

    public void disableVideo() {
        localVideoTrack.setEnabled(false);
        localCallState.setVideoEnabled(false);
        sendLocalCallState();
    }

    public void enableVideo() {
        localVideoTrack.setEnabled(true);
        localCallState.setVideoEnabled(true);
        sendLocalCallState();
    }

    private void sendLocalCallState() {
        signaling.sendCallState(localCallState.getAudioEnabled(), localCallState.getVideoEnabled());
    }

    public void changeCamera() {

    }

    public void addStateListener(StateListener listener) {
        listeners.add(listener);
    }

    public void removeStateListener(StateListener listener) {
        listeners.remove(listener);
    }

    private void setState(State state) {
        this.state = state;
        for (StateListener listener : listeners) {
            listener.onConnectionStateChange(state);
        }
    }

    private void onRemoteStreamStateChange(boolean audioEnabled, boolean videoEnabled) {
        for (StateListener listener : listeners) {
            listener.onRemoteStreamStateChange(audioEnabled, videoEnabled);
        }
    }


    private synchronized void dispose() {
        if (pc != null) {
            pc.dispose();
            pc = null;
            localAudioSource.dispose();
            videoSource.dispose();
            factory.dispose();
            factory = null;
            localVideoCapturer.dispose();
            localVideoRendererCallback = null;
            localVideoRenderer = null;
            localVideoTrack = null;
//        remoteVideoRenderer.dispose();
            remoteVideoRendererCallback = null;
            remoteVideoRenderer = null;
            remoteVideoTrack = null;
            setState(State.NOT_ACTIVE);
        }
    }

    public void init(Context applicationContext, boolean videoCall) {
        this.applicationContext = applicationContext;
        if (state == State.NOT_ACTIVE) {
            if (!factoryStaticInitialized) {
                abortUnless(PeerConnectionFactory.initializeAndroidGlobals(applicationContext, true, true, true), "Failed to initializeAndroidGlobals");
                this.factoryStaticInitialized = true;
            }

            localCallState.setVideoEnabled(videoCall);

            this.factory = new PeerConnectionFactory(null);
            this.sdpObserver = new SDPObserver();
            this.pcObserver = new PCObserver();
            this.queuedRemoteCandidates = new LinkedList<IceCandidate>();

            MediaConstraints pcConstraints = new MediaConstraints();
            pcConstraints.optional.add(new MediaConstraints.KeyValuePair("googImprovedWifiBwe", "true"));
            pcConstraints.optional.add(new MediaConstraints.KeyValuePair("RtpDataChannels", "true"));
            pcConstraints.optional.add(new MediaConstraints.KeyValuePair("DtlsSrtpKeyAgreement", "true"));
            pcConstraints.mandatory.add(new MediaConstraints.KeyValuePair("video", "true"));
            pcConstraints.mandatory.add(new MediaConstraints.KeyValuePair("audio", "true"));

            String[] stunServers = {"stun:stun.l.google.com:19302"};

            ArrayList<PeerConnection.IceServer> iceServers = new ArrayList<PeerConnection.IceServer>();
            for (String uri : stunServers) {
                iceServers.add(new PeerConnection.IceServer(uri));
            }

            ///iceServers.add(new PeerConnection.IceServer("turn:host:port", "username", "password"));

            pc = factory.createPeerConnection(iceServers, pcConstraints, pcObserver);

            Log.w(TAG + " livecycle", "create peer connection");

            // Uncomment to get ALL WebRTC tracing and SENSITIVE libjingle logging.
            // NOTE: this _must_ happen while |factory| is alive!
//            Logging.enableTracing(
//                    "logcat:",
//                    EnumSet.of(Logging.TraceLevel.TRACE_ALL),
//                    Logging.Severity.LS_SENSITIVE);

            {
                Log.d(TAG, "Creating local video source...");
                MediaStream lMS = factory.createLocalMediaStream("mobileMS");
                localVideoCapturer = getVideoCapturer();
//                MediaConstraints videoConstarains = new MediaConstraints();
//                videoConstarains.mandatory.add(new MediaConstraints.KeyValuePair("maxHeight", "400"));
//                videoConstarains.mandatory.add(new MediaConstraints.KeyValuePair("maxWidth", "400"));
//                videoConstarains.mandatory.add(new MediaConstraints.KeyValuePair("minFrameRate", "5"));//TODO remove (added to try fix low frame rate in Chrome)
                videoSource = factory.createVideoSource(localVideoCapturer);
                localVideoTrack = factory.createVideoTrack("mobileV0", videoSource);

                if (localVideoRendererCallback != null) {
                    localVideoRenderer = new VideoRenderer(localVideoRendererCallback);
                    localVideoTrack.addRenderer(localVideoRenderer);
                }

                lMS.addTrack(localVideoTrack);
                localAudioSource = factory.createAudioSource(new MediaConstraints());
                localAudioTrack = factory.createAudioTrack("mobileA0", localAudioSource);
                lMS.addTrack(localAudioTrack);

                Log.w(TAG + " livecycle", "add local stream");
                pc.addStream(lMS);
            }
        }
    }

    private static void abortUnless(boolean condition, String msg) {
        if (!condition) {
            throw new RuntimeException(msg);
        }
    }

    private static String preferISAC(String sdpDescription) {
        String[] lines = sdpDescription.split("\r\n");
        int mLineIndex = -1;
        String isac16kRtpMap = null;
        Pattern isac16kPattern =
                Pattern.compile("^a=rtpmap:(\\d+) ISAC/16000[\r]?$");
        for (int i = 0;
             (i < lines.length) && (mLineIndex == -1 || isac16kRtpMap == null);
             ++i) {
            if (lines[i].startsWith("m=audio ")) {
                mLineIndex = i;
                continue;
            }
            Matcher isac16kMatcher = isac16kPattern.matcher(lines[i]);
            if (isac16kMatcher.matches()) {
                isac16kRtpMap = isac16kMatcher.group(1);
                continue;
            }
        }
        if (mLineIndex == -1) {
            Log.d(TAG, "No m=audio line, so can't prefer iSAC");
            return sdpDescription;
        }
        if (isac16kRtpMap == null) {
            Log.d(TAG, "No ISAC/16000 line, so can't prefer iSAC");
            return sdpDescription;
        }
        String[] origMLineParts = lines[mLineIndex].split(" ");
        StringBuilder newMLine = new StringBuilder();
        int origPartIndex = 0;
        // Format is: m=<media> <port> <proto> <fmt> ...
        newMLine.append(origMLineParts[origPartIndex++]).append(" ");
        newMLine.append(origMLineParts[origPartIndex++]).append(" ");
        newMLine.append(origMLineParts[origPartIndex++]).append(" ");
        newMLine.append(isac16kRtpMap);
        for (; origPartIndex < origMLineParts.length; ++origPartIndex) {
            if (!origMLineParts[origPartIndex].equals(isac16kRtpMap)) {
                newMLine.append(" ").append(origMLineParts[origPartIndex]);
            }
        }
        lines[mLineIndex] = newMLine.toString();
        StringBuilder newSdpDescription = new StringBuilder();
        for (String line : lines) {
            newSdpDescription.append(line).append("\r\n");
        }
        return newSdpDescription.toString();
    }

    // Cycle through likely device names for the camera and return the first
    // capturer that works, or crash if none do.
    private VideoCapturer getVideoCapturer() {
        return createCameraCapturer(new Camera2Enumerator(applicationContext));
    }

    private VideoCapturer createCameraCapturer(CameraEnumerator enumerator) {
        final String[] deviceNames = enumerator.getDeviceNames();

        // First, try to find front facing camera
        Logging.d(TAG, "Looking for front facing cameras.");
        for (String deviceName : deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                Logging.d(TAG, "Creating front facing camera capturer.");
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        // Front facing camera not found, try something else
        Logging.d(TAG, "Looking for other cameras.");
        for (String deviceName : deviceNames) {
            if (!enumerator.isFrontFacing(deviceName)) {
                Logging.d(TAG, "Creating other camera capturer.");
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        return null;
    }


    private class SDPObserver implements SdpObserver {

        private SessionDescription localSdp;

        @Override
        public void onCreateSuccess(final SessionDescription origSdp) {
            abortUnless(localSdp == null, "multiple SDP create?!?");
            final SessionDescription sdp = new SessionDescription(origSdp.type, preferISAC(origSdp.description));
            localSdp = sdp;
            if (pc != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.w(TAG + " livecycle", "set local description");
                        pc.setLocalDescription(sdpObserver, sdp);
                    }
                });
            }
        }

        // Helper for sending local SDP (offer or answer, depending on role) to the
        // other participant.  Note that it is important to send the output of
        // create{Offer,Answer} and not merely the current value of
        // getLocalDescription() because the latter may include ICE candidates that
        // we might want to filter elsewhere.
        private void sendLocalDescription() {
            Log.d(TAG, "Sending " + localSdp.type);
            signaling.sendAnswer(localSdp.description);
        }

        @Override
        public void onSetSuccess() {
            if (pc != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (pc.getLocalDescription() == null) {
                            // We just set the remote offer, time to create our answer.
                            Log.w(TAG + " livecycle", "create answer");
                            MediaConstraints sdpMediaConstraints = new MediaConstraints();
                            sdpMediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
                            sdpMediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", localCallState.getVideoEnabled() ? "true" : "false"));
                            pc.createAnswer(SDPObserver.this, sdpMediaConstraints);
                        } else {
                            // Answer now set as local description; send it and drain
                            // candidates.
                            sendLocalDescription();
                            drainRemoteCandidates();
                        }
                    }
                });
            }
        }


        @Override
        public void onCreateFailure(final String error) {
            handler.post(new Runnable() {
                public void run() {
                    throw new RuntimeException("createSDP error: " + error);
                }
            });
        }

        @Override
        public void onSetFailure(final String error) {
            handler.post(new Runnable() {
                public void run() {
                    throw new RuntimeException("setSDP error: " + error);
                }
            });
        }

        private void drainRemoteCandidates() {
            if (queuedRemoteCandidates != null) {
                for (IceCandidate candidate : queuedRemoteCandidates) {
                    Log.w(TAG + " livecycle", "add ice candidate");
                    pc.addIceCandidate(candidate);
                }
            }
            queuedRemoteCandidates = null;
        }
    }

    // Implementation detail: observe ICE & stream changes and react accordingly.
    private class PCObserver implements PeerConnection.Observer {
        @Override
        public void onIceCandidate(final IceCandidate candidate) {
            handler.post(new Runnable() {
                public void run() {
                    signaling.sendIceCandidate(candidate.sdpMid, candidate.sdpMLineIndex, candidate.sdp);
                }
            });
        }

        @Override
        public void onIceConnectionReceivingChange(boolean b) {
        }

        @Override
        public void onIceCandidatesRemoved(IceCandidate[] var1) {
        }

        @Override
        public void onAddTrack(RtpReceiver var1, MediaStream[] var2) {

        }

        @Override
        public void onSignalingChange(final PeerConnection.SignalingState sigState) {
            if (sigState == PeerConnection.SignalingState.STABLE) {
                setState(State.CONNECTED);
            } else if (sigState == PeerConnection.SignalingState.CLOSED) {
                handler.post(new Runnable() {
                    public void run() {
                        dispose();
                    }
                });
            }
        }

        @Override
        public void onIceConnectionChange(PeerConnection.IceConnectionState newState) {
            newState = newState;
        }


        @Override
        public void onIceGatheringChange(PeerConnection.IceGatheringState newState) {
            newState = newState;
        }

        @Override
        public void onAddStream(final MediaStream stream) {
            handler.post(new Runnable() {
                public void run() {
                    abortUnless(stream.audioTracks.size() <= 1 &&
                                    stream.videoTracks.size() <= 1,
                            "Weird-looking stream: " + stream
                    );
                    if (stream.videoTracks.size() == 1) {
                        remoteVideoTrack = stream.videoTracks.get(0);
                        if (remoteVideoRendererCallback != null) {
                            remoteVideoRenderer = new VideoRenderer(remoteVideoRendererCallback);
                            remoteVideoTrack.addRenderer(remoteVideoRenderer);
                        }
                    }
                }
            });
        }

        @Override
        public void onRemoveStream(final MediaStream stream) {
            handler.post(new Runnable() {
                public void run() {
                    stream.videoTracks.get(0).dispose();
                }
            });
        }

        @Override
        public void onDataChannel(final DataChannel dc) {
            handler.post(new Runnable() {
                public void run() {
                    throw new RuntimeException(
                            "AppRTC doesn't use data channels, but got: " + dc.label() +
                                    " anyway!"
                    );
                }
            });
        }

        @Override
        public void onRenegotiationNeeded() {
            // No need to do anything; AppRTC follows a pre-agreed-upon
            // signaling/negotiation protocol.
        }
    }
}
