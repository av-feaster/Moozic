package com.example.moozic

import android.app.Activity
import android.media.MediaMetadataRetriever
import android.widget.Toast
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess

data class Music(val id:String,val title:String,val album:String,val artist:String,val duration: Long=0,val path:String,val artUri:String) {



}
fun formatDuration(duration: Long):String{
    val minutes= TimeUnit.MINUTES.convert(duration, TimeUnit.MILLISECONDS)
    val seconds=(TimeUnit.SECONDS.convert(duration, TimeUnit.MILLISECONDS)-minutes* TimeUnit.SECONDS.convert(1,
        TimeUnit.MINUTES))
    return String.format("%02d:%02d",minutes,seconds)
}
fun getImageArt(path: String):ByteArray?{
    val retriever=MediaMetadataRetriever()
    retriever.setDataSource(path)
    return retriever.embeddedPicture
}
 fun setSongPosition(increment: Boolean) {
     if (!PlayerActivity.repeat) {
         if (increment) {
             if (PlayerActivity.musicListPA.size - 1 == PlayerActivity.songPosition)
                 PlayerActivity.songPosition = 0
             else ++PlayerActivity.songPosition
         } else {
             if (PlayerActivity.songPosition == 0)
                 PlayerActivity.songPosition = PlayerActivity.musicListPA.size - 1
             else --PlayerActivity.songPosition
         }
     }
 }
fun closeApplication() {
         if (PlayerActivity.musicService != null) {
             PlayerActivity.musicService!!.stopForeground(true)
             PlayerActivity.musicService!!.mediaPlayer!!.release()
             exitProcess(1)
         }
     }


fun showToast(activity:Activity,msg:String){
    Toast.makeText(activity,msg,Toast.LENGTH_SHORT).show()
}