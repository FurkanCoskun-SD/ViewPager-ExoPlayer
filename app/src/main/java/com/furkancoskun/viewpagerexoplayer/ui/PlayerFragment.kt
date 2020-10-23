package com.furkancoskun.viewpagerexoplayer.ui

import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.furkancoskun.viewpagerexoplayer.R
import com.furkancoskun.viewpagerexoplayer.model.Movie
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.Player.EventListener
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.upstream.BandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.custom_controller.*
import kotlinx.android.synthetic.main.fragment_player.*

class PlayerFragment : Fragment(), View.OnClickListener {

    private lateinit var exoPlayer: SimpleExoPlayer
    lateinit var trackSelector: DefaultTrackSelector
    private lateinit var movies: Movie
    var flagFullScreen = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_player, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val parsedValue = arguments?.getString("bundleValue")
        movies = Gson().fromJson(parsedValue, object : TypeToken<Movie>() {}.type)

        btn_rew.setOnClickListener(this)
        btn_pause.setOnClickListener(this)
        btn_play.setOnClickListener(this)
        btn_forward.setOnClickListener(this)
        btn_fullscreen.setOnClickListener(this)

        buildExoPlayer(Uri.parse(movies.videoLink))
        exoPlayerListener()

    }

    override fun onClick(view: View?) {
        when (view!!.id) {
            R.id.btn_pause -> {
                // Stop video
                exoPlayer.playWhenReady = false
                // Get playback state
                exoPlayer.playbackState
                btn_pause.visibility = View.GONE
                btn_play.visibility = View.VISIBLE
            }
            R.id.btn_play -> {
                // Play video when ready
                exoPlayer.playWhenReady = true
                // Get playback state
                exoPlayer.playbackState
                btn_pause.visibility = View.VISIBLE
                btn_play.visibility = View.GONE
            }
            R.id.btn_rew -> {
                if (exoPlayer.currentPosition >= 10000) {
                    exoPlayer.seekTo(exoPlayer.currentPosition - 10000)
                }
            }
            R.id.btn_forward -> {
                if ((exoPlayer.duration - exoPlayer.currentPosition) >= 10000) {
                    exoPlayer.seekTo(exoPlayer.currentPosition + 10000)
                }
            }
            R.id.btn_fullscreen -> {
                // Check condition
                if (flagFullScreen) {
                    activity!!.tabLayout.visibility = View.VISIBLE
                    screenOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                } else {
                    activity!!.tabLayout.visibility = View.GONE
                    screenOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        // Stop video
        playerPause()
    }

    override fun onResume() {
        super.onResume()
        // Play video when ready
        playerStart()
    }

    companion object {
        fun create(movie: Movie): PlayerFragment {
            val fragment =
                PlayerFragment()
            val parsedValue = Gson().toJson(movie, object : TypeToken<Movie>() {}.type)
            val bundle = Bundle()
            bundle.putString("bundleValue", parsedValue)
            fragment.arguments = bundle
            return fragment
        }
    }

    private fun buildExoPlayer(videoURL: Uri) {
        // Init load control
        val loadControl: LoadControl = DefaultLoadControl()
        // Init band width meter
        val bandwidthMeter: BandwidthMeter = DefaultBandwidthMeter()
        // Init track selector
        trackSelector = DefaultTrackSelector(AdaptiveTrackSelection.Factory(bandwidthMeter))
        // Init simple exo player
        exoPlayer =
            ExoPlayerFactory.newSimpleInstance(context, trackSelector, loadControl)
        // Init media source
        val mediaSource: MediaSource = buildMediaSource(videoURL)

        // Set player
        player_view.player = exoPlayer
        // Keep screen on
        player_view.keepScreenOn = true
        // Prepare media
        exoPlayer.prepare(mediaSource)

        // Play video when ready
        exoPlayer.playWhenReady = true
    }

    private fun buildMediaSource(videoUrl: Uri): MediaSource {
        val userAgent = "exoplayer_video"
        val defaultHttpDataSourceFactory = DefaultHttpDataSourceFactory(userAgent)
        return if (videoUrl.lastPathSegment!!.contains("mp3") || videoUrl.lastPathSegment!!.contains("mp4")) {
            ExtractorMediaSource.Factory(defaultHttpDataSourceFactory)
                .createMediaSource(videoUrl)
        } else if (videoUrl.lastPathSegment!!.contains("m3u8")) {
            // Hls
            HlsMediaSource.Factory(defaultHttpDataSourceFactory).createMediaSource(videoUrl)
        } else {
            // Dash
            val dataSourceFactory = DefaultHttpDataSourceFactory("ua", DefaultBandwidthMeter())
            val dashChunkSourceFactory = DefaultDashChunkSource.Factory(dataSourceFactory)
            DashMediaSource.Factory(dashChunkSourceFactory, dataSourceFactory).createMediaSource(videoUrl)
        }
    }

    // Player listener
    private fun exoPlayerListener() {
        exoPlayer.addListener(object : EventListener {
            override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters?) {}
            override fun onSeekProcessed() {}
            override fun onTracksChanged(trackGroups: TrackGroupArray?, trackSelections: TrackSelectionArray?) {}
            override fun onLoadingChanged(isLoading: Boolean) {}
            override fun onPositionDiscontinuity(reason: Int) {}
            override fun onRepeatModeChanged(repeatMode: Int) {}
            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {}
            override fun onTimelineChanged(timeline: Timeline?, manifest: Any?, reason: Int) {}
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                // Check condition
                when (playbackState) {
                    Player.STATE_BUFFERING -> {
                        // When buffering
                        // Show progress bar
                        progress_bar.visibility = View.VISIBLE
                    }
                    Player.STATE_READY -> {
                        // When buffering
                        // Hide progress bar
                        progress_bar.visibility = View.GONE
                    }
                    Player.STATE_IDLE -> {
                        Log.d("TAG", "onPlayerStateChanged - STATE_IDLE")
                    }
                    Player.STATE_ENDED -> {
                        Log.d("TAG", "onPlayerStateChanged - STATE_ENDED")
                        btn_pause.visibility = View.GONE
                        btn_play.visibility = View.VISIBLE
                        exoPlayer.seekTo(0)
                        playerPause()
                    }
                }
            }
            override fun onPlayerError(error: ExoPlaybackException?) {
                if (error != null) {
                    when (error.type) {
                        ExoPlaybackException.TYPE_RENDERER -> {
                        }
                        ExoPlaybackException.TYPE_SOURCE -> {
                        }
                        ExoPlaybackException.TYPE_UNEXPECTED -> {
                        }
                        else -> {
                        }
                    }
                }
            }
        })
    }

    // Change screen orientation
    private fun screenOrientation(orientation: Int) {
        if (orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            // Set enter full screen image
            btn_fullscreen.setImageDrawable(context?.let { ContextCompat.getDrawable(it,
                R.drawable.ic_fullscreen
            ) })
            // Set portrait orientation
            activity!!.requestedOrientation = orientation
            // Set flag value is false
            flagFullScreen = false
        } else if (orientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            // Set enter full screen image
            btn_fullscreen.setImageDrawable(context?.let { ContextCompat.getDrawable(it,
                R.drawable.ic_fullscreen_exit
            ) })
            // Set portrait orientation
            activity!!.requestedOrientation = orientation
            // Set flag value is true
            flagFullScreen = true
        }
    }

    // Stop video
    private fun playerPause() {
        // Stop video
        exoPlayer.playWhenReady = false
        // Get playback state
        exoPlayer.playbackState
    }

    // Play video
    private fun playerStart() {
        // Play video when ready
        exoPlayer.playWhenReady = true
        // Get playback state
        exoPlayer.playbackState
    }

}