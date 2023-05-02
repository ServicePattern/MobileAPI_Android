package com.brightpattern.webRTC

import android.app.Application
import android.util.Log
import com.brightpattern.ChatDemo
import com.brightpattern.bpcontactcenter.ContactCenterCommunicator
import com.brightpattern.bpcontactcenter.entity.ContactCenterEvent
import com.brightpattern.bpcontactcenter.entity.SignalingType
import org.webrtc.AddIceObserver
import org.webrtc.AudioTrack
import org.webrtc.Camera2Enumerator
import org.webrtc.CameraVideoCapturer
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.EglBase
import org.webrtc.IceCandidate
import org.webrtc.MediaConstraints
import org.webrtc.PeerConnection
import org.webrtc.PeerConnection.RTCConfiguration
import org.webrtc.PeerConnectionFactory
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import org.webrtc.SurfaceTextureHelper
import org.webrtc.SurfaceViewRenderer
import org.webrtc.VideoTrack

class RTCClient(
    private val application: Application,
    private val partyID: String,
    private val observer: PeerConnection.Observer
) {

    companion object {
        private const val LOG_TAG = "RTCClient"
        private const val VIDEO_TRACK_ID = "video0"
        private const val AUDIO_TRACK_ID = "audio0"
        private const val STREAM_ID = "stream-0"
    }

    init {
        initPeerConnectionFactory(application)
    }

    private var eglContext: EglBase? = EglBase.create()
    private val peerConnectionFactory by lazy { createPeerConnectionFactory() }
    private val iceServer = listOf(
        PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer(),
    )

    private val peerConnection by lazy { createPeerConnection(observer) }
    private val localVideoSource by lazy { peerConnectionFactory.createVideoSource(false) }
    private val localAudioSource by lazy { peerConnectionFactory.createAudioSource(MediaConstraints()) }
    private var videoCapturer: CameraVideoCapturer? = null
    private var localAudioTrack: AudioTrack? = null
    private var localVideoTrack: VideoTrack? = null

    private val api: ContactCenterCommunicator?
        get() {
            return ChatDemo.api
        }

    private fun initPeerConnectionFactory(application: Application) {
        val peerConnectionOption = PeerConnectionFactory.InitializationOptions.builder(application)
            .setEnableInternalTracer(true)
            .setFieldTrials("WebRTC-H264HighProfile/Enabled/")
            .createInitializationOptions()

        PeerConnectionFactory.initialize(peerConnectionOption)
    }

    private fun createPeerConnectionFactory(): PeerConnectionFactory {
        return PeerConnectionFactory.builder()
            .setVideoEncoderFactory(
                DefaultVideoEncoderFactory(
                    eglContext!!.eglBaseContext,
                    true,
                    true
                )
            )
            .setVideoDecoderFactory(DefaultVideoDecoderFactory(eglContext!!.eglBaseContext))
            .setOptions(PeerConnectionFactory.Options().apply {
//                disableEncryption = true
                disableNetworkMonitor = true
            }).createPeerConnectionFactory()
    }

    private fun createPeerConnection(observer: PeerConnection.Observer): PeerConnection? {
        val rtcConfig = RTCConfiguration(iceServer)
        rtcConfig.disableIPv6OnWifi = true
        rtcConfig.bundlePolicy = PeerConnection.BundlePolicy.MAXBUNDLE
        rtcConfig.iceTransportsType = PeerConnection.IceTransportsType.ALL
        return peerConnectionFactory.createPeerConnection(rtcConfig, observer)
    }

    private fun getVideoCapturer(application: Application): CameraVideoCapturer {
        return Camera2Enumerator(application).run {
            deviceNames.find {
                isFrontFacing(it)
            }?.let {
                createCapturer(it, null)
            } ?: throw IllegalStateException()
        }
    }

    fun startLocalVideo(surface: SurfaceViewRenderer) {
        val surfaceTextureHelper =
            SurfaceTextureHelper.create(Thread.currentThread().name, eglContext!!.eglBaseContext)
        videoCapturer = getVideoCapturer(application)
        videoCapturer?.initialize(
            surfaceTextureHelper,
            surface.context, localVideoSource.capturerObserver
        )
        videoCapturer?.startCapture(320, 240, 30)
        localVideoTrack = peerConnectionFactory.createVideoTrack(VIDEO_TRACK_ID, localVideoSource)
        localVideoTrack?.addSink(surface)
        localAudioTrack = peerConnectionFactory.createAudioTrack(AUDIO_TRACK_ID, localAudioSource)
        peerConnection?.addTrack(localVideoTrack, mutableListOf(STREAM_ID))
        peerConnection?.addTrack(localAudioTrack, mutableListOf(STREAM_ID))
    }

    fun initializeSurfaceView(surface: SurfaceViewRenderer) {
        surface.run {
            setEnableHardwareScaler(true)
            setMirror(true)
            init(eglContext!!.eglBaseContext, null)
        }
    }

    fun onRemoteSessionReceived(session: SessionDescription) {
        peerConnection?.setRemoteDescription(object : SdpObserver {
            override fun onCreateSuccess(p0: SessionDescription?) {
                Log.e(LOG_TAG, " setRD onCreateSuccess >>>  $p0")
            }

            override fun onSetSuccess() {
                Log.e(LOG_TAG, " setRD onSetSuccess State ${peerConnection?.connectionState()} ")
            }

            override fun onCreateFailure(p0: String?) {
                Log.e(LOG_TAG, " setRD onCreateFailure $p0")

            }

            override fun onSetFailure(p0: String?) {
                Log.e(LOG_TAG, " setRD onSetFailure  $p0 ")
            }

        }, session)

    }

    fun answer(messageID: Int, session: SessionDescription) {
        val constraints = MediaConstraints()
        constraints.mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))

        candidatesToAdd.forEach { iceCandidate ->
            addIceCandidate(iceCandidate)
        }

        peerConnection?.createAnswer(object : SdpObserver {
            override fun onCreateSuccess(desc: SessionDescription?) {

                peerConnection?.setLocalDescription(object : SdpObserver {
                    override fun onCreateSuccess(p0: SessionDescription?) {
                        Log.e(LOG_TAG, "onCreateSuccess >>>  $p0")
                    }

                    override fun onSetSuccess() {
                        api?.sendSignalingData(ChatDemo.chatID, partyID, messageID, ContactCenterEvent.SignalingData(sdp = desc!!.description, type = SignalingType.ANSWER_CALL)) {
                            Log.e(LOG_TAG, "Result $it")
                        }
                    }

                    override fun onCreateFailure(p0: String?) {
                        Log.e(LOG_TAG, "onCreateFailure >>>  $p0")
                    }

                    override fun onSetFailure(p0: String?) {
                        Log.e(LOG_TAG, "onSetFailure >>>  $p0")
                    }

                }, desc)
            }

            override fun onSetSuccess() {
            }

            override fun onCreateFailure(p0: String?) {
            }

            override fun onSetFailure(p0: String?) {
            }

        }, constraints)
    }

    fun rejectCall(messageID: Int) {
        api?.sendSignalingData(ChatDemo.chatID, partyID, messageID, ContactCenterEvent.SignalingData(type = SignalingType.CALL_REJECTED)) {
            Log.e(LOG_TAG, "Result: $it")
        }
    }

    fun endCall(messageID: Int) {

        api?.sendSignalingData(ChatDemo.chatID, partyID, messageID, ContactCenterEvent.SignalingData(type = SignalingType.END_CALL)) {
            Log.e(LOG_TAG, "Result:  $it")
        }
        peerConnection?.close()
    }

    var candidatesToAdd = mutableListOf<IceCandidate>()

    fun addIceCandidate(p0: IceCandidate?) {
        p0?.let { candidate ->

            if (peerConnection?.remoteDescription != null) {
                peerConnection?.addIceCandidate(candidate, object : AddIceObserver {
                    override fun onAddSuccess() {
                        Log.e(LOG_TAG, "ice candidate added successfully ")
                    }

                    override fun onAddFailure(p0: String?) {
                        Log.e(LOG_TAG, "ERROR ON add Ice candidate \n $p0")
                    }

                })
            } else {
                candidatesToAdd.add(candidate)
            }
        }
    }

    fun switchCamera() {
        videoCapturer?.switchCamera(null)
    }

    fun toggleAudio(mute: Boolean) {
        localAudioTrack?.setEnabled(mute)
    }

    fun toggleCamera(cameraPause: Boolean) {
        localVideoTrack?.setEnabled(cameraPause)
    }

    fun release(surface: SurfaceViewRenderer) {
        surface.release()
        localVideoTrack?.removeSink(surface)

        videoCapturer?.stopCapture()
        peerConnection?.close()
        localVideoSource?.dispose()
        localAudioSource?.dispose()
        localAudioTrack?.dispose()

        eglContext?.releaseSurface()
        eglContext?.release()
        eglContext = null

    }
}