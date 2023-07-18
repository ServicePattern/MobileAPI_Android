package com.brightpattern.webRTC

import android.util.Log
import org.webrtc.*

open class PeerConnectionObserver : PeerConnection.Observer {

    companion object {
        const val LOG_TAG = "PCObserver"
    }

    override fun onSignalingChange(p0: PeerConnection.SignalingState?) {
        Log.e(LOG_TAG, "onSignalingChange > $p0")
    }

    override fun onIceConnectionChange(p0: PeerConnection.IceConnectionState?) {
        Log.e(LOG_TAG, "onIceConnectionChange > $p0")
    }

    override fun onIceConnectionReceivingChange(p0: Boolean) {
        Log.e(LOG_TAG, "onIceConnectionReceivingChange > $p0")
    }

    override fun onIceGatheringChange(p0: PeerConnection.IceGatheringState?) {
        Log.e(LOG_TAG, "onIceGatheringChange > $p0")
    }

    override fun onIceCandidate(p0: IceCandidate?) {
        Log.e(LOG_TAG, "onIceCandidate > $p0")
    }

    override fun onIceCandidatesRemoved(p0: Array<out IceCandidate>?) {
        Log.e(LOG_TAG, "onIceCandidatesRemoved > $p0")
    }

    override fun onAddStream(p0: MediaStream?) {
        Log.e(LOG_TAG, "onAddStream > $p0")
    }

    override fun onRemoveStream(p0: MediaStream?) {
        Log.e(LOG_TAG, "onRemoveStream > $p0")
    }

    override fun onDataChannel(p0: DataChannel?) {
        Log.e(LOG_TAG, "onDataChannel > $p0")
    }

    override fun onRenegotiationNeeded() {
        Log.e(LOG_TAG, "onRenegotiationNeeded ")
    }

    override fun onAddTrack(p0: RtpReceiver?, p1: Array<out MediaStream>?) {
        Log.e(LOG_TAG, "onAddTrack $p0 $p1 ")
    }
}