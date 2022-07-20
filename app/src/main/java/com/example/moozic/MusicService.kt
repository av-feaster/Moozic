package com.example.moozic

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationCompat

class MusicService: Service() {
    private var myBinder=MyBinder()
    var mediaPlayer:MediaPlayer?=null
    private lateinit var mediaSession : MediaSessionCompat
    private lateinit var runnable:Runnable

    override fun onBind(intent: Intent?): IBinder? {
        mediaSession= MediaSessionCompat(baseContext,"My Music")
        return myBinder
    }
    //inner class help to return object to main class
    inner class MyBinder:Binder(){
        fun currentService():MusicService{
            return this@MusicService
        }

    }
    @SuppressLint("UnspecifiedImmutableFlag")
    fun showNotification (playPauseBtn:Int){
        val prevIntent=Intent(baseContext,NotificationReceiver::class.java).setAction(ApplicationClass.PREVIOUS)
        val prevPendingIntent= PendingIntent.getBroadcast(baseContext,0,prevIntent,PendingIntent.FLAG_MUTABLE)

        val playIntent=Intent(baseContext,NotificationReceiver::class.java).setAction(ApplicationClass.PLAY)
        val playPendingIntent=PendingIntent. getBroadcast(baseContext,0,playIntent, PendingIntent.FLAG_MUTABLE)

        val nextIntent=Intent(baseContext,NotificationReceiver::class.java).setAction(ApplicationClass.NEXT)
        val nextPendingIntent=PendingIntent. getBroadcast(baseContext,0,nextIntent, PendingIntent.FLAG_MUTABLE)

        val exitIntent=Intent(baseContext,NotificationReceiver::class.java).setAction(ApplicationClass.EXIT)
        val exitPendingIntent= PendingIntent.getBroadcast(baseContext,0,exitIntent, PendingIntent.FLAG_MUTABLE)


        //current Image
        val imgArt= getImageArt(PlayerActivity.musicListPA[PlayerActivity.songPosition].path)
        //icon will have bitmap value for notification
        val image=if(imgArt != null){
            BitmapFactory.decodeByteArray(imgArt,0,imgArt.size)
        }else{BitmapFactory.decodeResource(resources,R.drawable.music_icon)}

        val notification =androidx.core.app.NotificationCompat.Builder(baseContext,ApplicationClass.CHANNEL_ID )
              .setContentTitle(PlayerActivity.musicListPA[PlayerActivity.songPosition].title)
              .setContentText(PlayerActivity.musicListPA[PlayerActivity.songPosition].artist)
              .setSmallIcon(R.drawable.playlist_icon)
              .setLargeIcon(image)
              .setStyle(androidx.media.app.NotificationCompat.MediaStyle().setMediaSession(mediaSession.sessionToken))
              .setPriority(NotificationCompat.PRIORITY_HIGH)
              .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
              .setOnlyAlertOnce(true)
              .addAction(R.drawable.previous_icon,"Previous" , prevPendingIntent)
              .addAction(playPauseBtn,"Play" , playPendingIntent)
              .addAction(R.drawable.next_icon,"Next" , nextPendingIntent)
              .addAction(R.drawable.exit_icon,"Exit" , exitPendingIntent)
              .build()
        startForeground(13, notification)
    }
    fun createMediaPlayer(){
        try {
            if(PlayerActivity.musicService!!.mediaPlayer==null) PlayerActivity.musicService!!.mediaPlayer=MediaPlayer()
            PlayerActivity.musicService!!.mediaPlayer!!.reset()
            PlayerActivity.musicService!!.mediaPlayer!!.setDataSource(PlayerActivity.musicListPA[PlayerActivity.songPosition].path)
            PlayerActivity.musicService!!.mediaPlayer!!.prepare()

            //when playing pause icon to be displayed
            PlayerActivity.binding.playPauseBtnPA.setIconResource(R.drawable.pause_icon)
            PlayerActivity.musicService!!.showNotification(R.drawable.pause_icon)

            //seekbar changes
            PlayerActivity.binding.tvSeekBarStartPA.text= formatDuration(PlayerActivity.musicService!!.mediaPlayer!!.currentPosition.toLong())
            PlayerActivity.binding.tvSeekBarEndPA.text= formatDuration(PlayerActivity.musicService!!.mediaPlayer!!.duration.toLong())
            PlayerActivity.binding.seekBarPA.progress=0
            PlayerActivity.binding.seekBarPA.max= PlayerActivity.musicService!!.mediaPlayer!!.duration


        }catch (e:Exception){return}
    }

    fun seekBarSetup(){
        runnable=Runnable{
            PlayerActivity.binding.tvSeekBarStartPA.text= formatDuration(PlayerActivity.musicService!!.mediaPlayer!!.currentPosition.toLong())
            PlayerActivity.binding.seekBarPA.progress=mediaPlayer!!.currentPosition
            Handler(Looper.getMainLooper()).postDelayed(runnable,210)
        }
        Handler(Looper.getMainLooper()).postDelayed(runnable,0)

    }
}
