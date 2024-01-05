package com.lizongying.mytv

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.annotation.OptIn
import androidx.fragment.app.Fragment
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.source.LoadEventInfo
import androidx.media3.exoplayer.source.MediaLoadData
import androidx.media3.ui.PlayerView
import com.lizongying.mytv.databinding.PlayerBinding
import com.lizongying.mytv.models.TVViewModel


class PlayerFragment : Fragment() {

    private var _binding: PlayerBinding? = null
    private var playerView: PlayerView? = null
    private var tvViewModel: TVViewModel? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = PlayerBinding.inflate(inflater, container, false)
        playerView = _binding!!.playerView
        (activity as MainActivity).playerFragment = this
        playerView?.viewTreeObserver?.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                playerView!!.viewTreeObserver.removeOnGlobalLayoutListener(this)
                playerView!!.player = activity?.let {
                    ExoPlayer.Builder(it)
                        .build()
                }
                playerView!!.player?.playWhenReady = true
                playerView!!.player?.addListener(object : Player.Listener {
                    override fun onVideoSizeChanged(videoSize: VideoSize) {
                        val aspectRatio = 16f / 9f
                        val layoutParams = playerView?.layoutParams
                        val ratio = playerView?.measuredWidth?.div(playerView?.measuredHeight!!)
                        if (ratio != null) {
                            if (ratio < aspectRatio) {
                                layoutParams?.height =
                                    (playerView?.measuredWidth?.div(aspectRatio))?.toInt()
                                playerView?.layoutParams = layoutParams
                            } else if (ratio > aspectRatio) {
                                layoutParams?.width =
                                    (playerView?.measuredHeight?.times(aspectRatio))?.toInt()
                                playerView?.layoutParams = layoutParams
                            }
                        }
                    }

                    override fun onPlayerError(error: PlaybackException) {
                        super.onPlayerError(error)

                        Log.e(TAG, "PlaybackException $error")
                    }
                })
            }
        })
        (activity as MainActivity).fragmentReady()
        return _binding!!.root
    }

    @OptIn(UnstableApi::class)
    fun play(tvViewModel: TVViewModel) {
        this.tvViewModel = tvViewModel
        val videoUrlCurrent =
            tvViewModel.videoIndex.value?.let { tvViewModel.videoUrl.value?.get(it) }
        playerView?.player?.run {
            val mediaItem = MediaItem.Builder()
            tvViewModel.id.value?.let { mediaItem.setMediaId(it.toString()) }
            videoUrlCurrent?.let { mediaItem.setUri(it) }
            setMediaItem(mediaItem.build())
            prepare()

//            val httpDataSource = DefaultHttpDataSource.Factory()
//            val hls = HlsMediaSource.Factory(httpDataSource).createMediaSource(
//                MediaItem.fromUri(
//                    Uri.parse(videoUrlCurrent)
//                )
//            )
//            val analyticsListener: AnalyticsListener= MyAnalyticsListener()
//            val exoPlayer = playerView?.player as ExoPlayer
//            exoPlayer.addAnalyticsListener(analyticsListener)
//            exoPlayer.setMediaSource(hls)
//            exoPlayer.playWhenReady = true
//
//
//            exoPlayer.let {
//                val parameters =
//                    TrackSelectionParameters.Builder().setPreferredAudioMimeType("application/id3").build()
//                // 更新轨道选择器参数
//                exoPlayer.trackSelector?.parameters = parameters
//                Log.i(TAG, "parameters $parameters")
//            }
//
//            // 获取当前轨道组
//
//            playerView?.player = exoPlayer
//            playerView?.player?.prepare()


//
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
//                val codecList = MediaCodecList(MediaCodecList.ALL_CODECS)
//                val codecInfos = codecList.codecInfos
//
//                for (codecInfo in codecInfos) {
//
//                    val supportedTypes = codecInfo.supportedTypes
//                    for (type in supportedTypes) {
//                        Log.d("supportedTypes", "$type")
////                        if (type.equals(androidx.media3.exoplayer.mediacode, ignoreCase = true)) {
////                            Log.d("AudioCodecChecker", "Device supports MPEG-L2")
////                            return
////                        }
//                    }
//                }
//
//                Log.d("AudioCodecChecker", "Device does not support MPEG-L2")
//            }

        }
    }

    @UnstableApi
    class MyAnalyticsListener : AnalyticsListener {
        override fun onLoadStarted(
            eventTime: AnalyticsListener.EventTime,
            loadEventInfo: LoadEventInfo,
            mediaLoadData: MediaLoadData
        ) {
            super.onLoadStarted(eventTime, loadEventInfo, mediaLoadData)
//            Log.i(TAG, "loadEventInfo.uri ${loadEventInfo.uri} ${mediaLoadData.trackFormat.toString()}")
        }
    }

    override fun onStart() {
        super.onStart()
        if (playerView != null) {
            playerView!!.player?.play()
        }
    }

    override fun onStop() {
        super.onStop()
        if (playerView != null) {
            playerView!!.player?.stop()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (playerView != null) {
            playerView!!.player?.release()
        }
    }

    companion object {
        private const val TAG = "PlaybackVideoFragment"
    }
}