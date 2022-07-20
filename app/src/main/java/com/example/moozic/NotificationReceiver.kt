package com.example.moozic

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class NotificationReceiver : BroadcastReceiver(){
    override fun onReceive(context: Context?, intent: Intent?) {
        when(intent?.action){
            ApplicationClass.PREVIOUS-> {
                Toast.makeText(context,"Previous Clicked",Toast.LENGTH_SHORT).show()
                prevNextSong(false,context!!)

            }
            ApplicationClass.NEXT-> {
                Toast.makeText(context,"Next Clicked",Toast.LENGTH_SHORT).show()
                prevNextSong(true,context!!)
            }
            ApplicationClass.PLAY-> {
                if(PlayerActivity.isPlaying)pauseMusic()else playMusic()

            }
            ApplicationClass.EXIT-> {
                Toast.makeText(context, "Exiting..", Toast.LENGTH_SHORT).show()
                closeApplication()
            }

        }

    }
    private fun playMusic(){
        PlayerActivity.isPlaying=true
        PlayerActivity.musicService!!.mediaPlayer!!.start()
        PlayerActivity.musicService!!.showNotification(R.drawable.pause_icon)
        PlayerActivity.binding.playPauseBtnPA.setIconResource(R.drawable.pause_icon)
    }
    private fun pauseMusic(){
        PlayerActivity.isPlaying=false
        PlayerActivity.musicService!!.mediaPlayer!!.pause()
        PlayerActivity.musicService!!.showNotification(R.drawable.play_icon)
        PlayerActivity.binding.playPauseBtnPA.setIconResource(R.drawable.play_icon)
    }
    //null check? --Imp?
    private fun prevNextSong(increment:Boolean ,context: Context){
        setSongPosition(increment=increment)
        PlayerActivity.musicService!!.createMediaPlayer()
        //change not
        PlayerActivity.musicService!!.showNotification(R.drawable.pause_icon)

        //layout set

            Glide.with(context)
                .load(PlayerActivity.musicListPA[PlayerActivity.songPosition].artUri)
                .apply(RequestOptions().placeholder(R.drawable.music_player_icon).centerCrop())
                .into(PlayerActivity.binding.crrSongImgPA )

        PlayerActivity.binding.crrSongNamePA.text= PlayerActivity.musicListPA[PlayerActivity.songPosition].title
        playMusic()


    }
}