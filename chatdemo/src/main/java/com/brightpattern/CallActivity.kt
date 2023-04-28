package com.brightpattern

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.brightpattern.bpcontactcenter.ContactCenterCommunicator
import com.brightpattern.bpcontactcenter.entity.ContactCenterEvent
import com.brightpattern.bpcontactcenter.entity.SignalingType
import com.brightpattern.bpcontactcenter.interfaces.ContactCenterEventsInterface
import com.brightpattern.bpcontactcenter.utils.Result
import com.brightpattern.bpcontactcenter.utils.Success
import com.brightpattern.chatdemo.R
import com.brightpattern.chatdemo.databinding.ActivityCallBinding
import com.brightpattern.webRTC.PeerConnectionObserver
import com.brightpattern.webRTC.RTCAudioManager
import com.brightpattern.webRTC.RTCClient
import org.webrtc.IceCandidate
import org.webrtc.MediaStream
import org.webrtc.SessionDescription

class CallActivity : AppCompatActivity() {

    companion object {
        private const val LOG_TAG = "CallActivity"
        private const val CAMERA_AUDIO_PERMISSION_REQUEST_CODE = 1
        private const val CAMERA_PERMISSION = Manifest.permission.CAMERA
        private const val AUDIO_PERMISSION = Manifest.permission.RECORD_AUDIO
    }

    private val api: ContactCenterCommunicator?
        get() {
            return ChatDemo.api
        }

    private var rtcClient: RTCClient? = null
    private var isMute = false
    private var isCameraPause = false
    private val rtcAudioManager by lazy { RTCAudioManager.create(this) }
    private var isSpeakerMode = true
    private var lastKnownMessageID = 0
    private var partyID: String = ""
    private var sessionDescription: String = ""

    private lateinit var binding: ActivityCallBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        intent.extras?.getString("party_id")?.let { partyID = it }
        intent.extras?.getString("session_description")?.let { sessionDescription = it }

        binding = ActivityCallBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        checkCameraAndAudioPermission()

    }

    private fun initRTC() {

        rtcClient = RTCClient(application, partyID, object : PeerConnectionObserver() {
            override fun onIceCandidate(p0: IceCandidate?) {
                super.onIceCandidate(p0)
                rtcClient?.addIceCandidate(p0)
                val candidate = hashMapOf(
                    "sdpMid" to p0?.sdpMid,
                    "sdpMLineIndex" to p0?.sdpMLineIndex,
                    "sdpCandidate" to p0?.sdp
                )
                Log.e(LOG_TAG, "onIceCandidate > $candidate")
                lastKnownMessageID += 1
                val data = ContactCenterEvent.SignalingData(sdpMid = p0!!.sdpMid, sdpMLineIndex = p0!!.sdpMLineIndex.toString(), candidate = p0!!.sdp)
                api?.sendSignalingData(ChatDemo.chatID, partyID,lastKnownMessageID,data) {
                    Log.e(LOG_TAG, ">> $it")
                }
            }

            override fun onAddStream(p0: MediaStream?) {
                super.onAddStream(p0)
                p0?.videoTracks?.get(0)?.addSink(binding.remoteView)
                Log.d(LOG_TAG, "onAddStream: $p0")


            }
        })
        rtcAudioManager.setDefaultAudioDevice(RTCAudioManager.AudioDevice.SPEAKER_PHONE)
    }

    private fun initView() {
        binding.incomingCallLayout.visibility = View.VISIBLE

        binding.switchCameraButton.setOnClickListener {
            rtcClient?.switchCamera()
        }

        binding.micButton.setOnClickListener {
            if (isMute) {
                isMute = false
                binding.micButton.setImageResource(R.drawable.ic_baseline_mic_off_24)
            } else {
                isMute = true
                binding.micButton.setImageResource(R.drawable.ic_baseline_mic_24)
            }
            rtcClient?.toggleAudio(isMute)
        }

        binding.videoButton.setOnClickListener {
            if (isCameraPause) {
                isCameraPause = false
                binding.videoButton.setImageResource(R.drawable.ic_baseline_videocam_off_24)
            } else {
                isCameraPause = true
                binding.videoButton.setImageResource(R.drawable.ic_baseline_videocam_24)
            }
            rtcClient?.toggleCamera(isCameraPause)
        }

        binding.audioOutputButton.setOnClickListener {
            if (isSpeakerMode) {
                isSpeakerMode = false
                binding.audioOutputButton.setImageResource(R.drawable.ic_baseline_hearing_24)
                rtcAudioManager.setDefaultAudioDevice(RTCAudioManager.AudioDevice.EARPIECE)
            } else {
                isSpeakerMode = true
                binding.audioOutputButton.setImageResource(R.drawable.ic_baseline_speaker_up_24)
                rtcAudioManager.setDefaultAudioDevice(RTCAudioManager.AudioDevice.SPEAKER_PHONE)

            }

        }
        binding.endCallButton.setOnClickListener {
            setCallLayoutGone()
            setIncomingCallLayoutGone()
            lastKnownMessageID += 1
            rtcClient?.endCall(lastKnownMessageID)
        }

        incomingCallAction()
    }

    private fun checkCameraAndAudioPermission() {
        if ((ContextCompat.checkSelfPermission(this, CAMERA_PERMISSION)
                    != PackageManager.PERMISSION_GRANTED) &&
            (ContextCompat.checkSelfPermission(this, AUDIO_PERMISSION)
                    != PackageManager.PERMISSION_GRANTED)
        ) {
            requestCameraAndAudioPermission()
        } else {
            onCameraAndAudioPermissionGranted()
        }
    }

    private fun onCameraAndAudioPermissionGranted() {
        initRTC()
        initView()
    }

    override fun onPause() {
        Log.e(LOG_TAG, "************** onPause **************")
        api?.let { api ->
            api.stopPolling(ChatDemo.chatID) { r ->
                if (r is Success) {
                    Log.d(LOG_TAG, "stopped event polling")
                }
            }
        }
        super.onPause()
    }

    override fun onDestroy() {
        rtcClient?.release(binding.localView)
        rtcClient = null

        super.onDestroy()
    }

    override fun onResume() {
        Log.e(LOG_TAG, "************** onResume **************")
        super.onResume()

        api?.let { api ->
            api.startPolling(ChatDemo.chatID) { r ->
                if (r is Success) {
                    Log.d(LOG_TAG, "started event polling")
                }
            }

            api.callback = object : ContactCenterEventsInterface {
                override fun chatSessionEvents(result: Result<List<ContactCenterEvent>, Error>) {
                    this@CallActivity.resultProcessing(result)
                }
            }
        }
    }

    private fun requestCameraAndAudioPermission(dialogShown: Boolean = false) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, CAMERA_PERMISSION) &&
            ActivityCompat.shouldShowRequestPermissionRationale(this, AUDIO_PERMISSION) &&
            !dialogShown
        ) {
            showPermissionRationaleDialog()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(CAMERA_PERMISSION, AUDIO_PERMISSION), CAMERA_AUDIO_PERMISSION_REQUEST_CODE)
        }
    }

    private fun showPermissionRationaleDialog() {
        AlertDialog.Builder(this)
            .setTitle("Camera And Audio Permission Required")
            .setMessage("This app need the camera and audio to function")
            .setPositiveButton("Grant") { dialog, _ ->
                dialog.dismiss()
                requestCameraAndAudioPermission(true)
            }
            .setNegativeButton("Deny") { dialog, _ ->
                dialog.dismiss()
                onCameraPermissionDenied()
            }
            .show()
    }

    private fun onCameraPermissionDenied() {
        Toast.makeText(this, "Camera and Audio Permission Denied", Toast.LENGTH_LONG).show()
    }

    private fun resultProcessing(result: Result<List<ContactCenterEvent>, Error>) {
        if (result is Success)
            result.value.forEach { event ->
                (event as? ContactCenterEvent.ChatSessionSignaling)?.let { chatSessionSignaling ->
                    when (chatSessionSignaling.data.type) {
                        SignalingType.REQUEST_CALL -> Log.e(LOG_TAG, "REQUEST_CALL")
                        SignalingType.END_CALL -> {
                            Log.e(LOG_TAG, "END_CALL")
                            setResult(ChatDemo.CLOSED_BY_SERVER)
                            finish()
                        }
                        SignalingType.CALL_REJECTED -> Log.e(LOG_TAG, "CALL_REJECTED")
                        SignalingType.ANSWER_CALL -> Log.e(LOG_TAG, "ANSWER_CALL")
                        SignalingType.OFFER_CALL -> Log.e(LOG_TAG, "OFFER_CALL")
                        SignalingType.ICE_CANDIDATE -> {
                            if (!chatSessionSignaling.data.sdpMid.isNullOrBlank() && !chatSessionSignaling.data.sdpMLineIndex.isNullOrBlank() && !chatSessionSignaling.data.candidate.isNullOrBlank()) {
                                val candidate = IceCandidate(chatSessionSignaling.data.sdpMid, chatSessionSignaling.data.sdpMLineIndex!!.toInt(), chatSessionSignaling.data.candidate)
                                Log.e(LOG_TAG, "ICE_CANDIDATE \n $candidate")
                                candidates.add(candidate)
                                rtcClient?.addIceCandidate(candidate)
                            }
                        }
                        else -> {
                            Log.e(LOG_TAG, "Result processing ELSE case")
                            Log.e(LOG_TAG, "> ${event.data.type} \n ${event.data}")
                        }
                    }
                    if (lastKnownMessageID < chatSessionSignaling.msg_id.toInt())
                        lastKnownMessageID = chatSessionSignaling.msg_id.toInt()
                }
            }
    }

    private var candidates = arrayListOf<IceCandidate>()
    private fun incomingCallAction() {
        runOnUiThread {
            setIncomingCallLayoutVisible()
            binding.incomingNameTV.text = "Operator is calling you"
            binding.acceptButton.setOnClickListener {
                setIncomingCallLayoutGone()
                setCallLayoutVisible()

                rtcClient?.initializeSurfaceView(binding.localView)
                rtcClient?.initializeSurfaceView(binding.remoteView)
                rtcClient?.startLocalVideo(binding.localView)

                val session = SessionDescription(
                    SessionDescription.Type.OFFER,
                    sessionDescription
                )
//                rtcClient?.call("")

                rtcClient?.onRemoteSessionReceived(session)
                lastKnownMessageID += 1
                rtcClient?.answer(lastKnownMessageID, session)
                binding.remoteViewLoading.visibility = View.GONE

            }
            binding.rejectButton.setOnClickListener {
                setIncomingCallLayoutGone()

                lastKnownMessageID += 1
                rtcClient?.rejectCall(lastKnownMessageID)

                lastKnownMessageID += 1
                rtcClient?.endCall(lastKnownMessageID)
            }

        }
    }

    private fun setIncomingCallLayoutGone() {
        binding.incomingCallLayout.visibility = View.GONE
    }

    private fun setIncomingCallLayoutVisible() {
        binding.incomingCallLayout.visibility = View.VISIBLE
    }

    private fun setCallLayoutGone() {
        binding.callLayout.visibility = View.GONE
    }

    private fun setCallLayoutVisible() {
        binding.callLayout.visibility = View.VISIBLE
    }

}