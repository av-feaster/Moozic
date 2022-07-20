package com.example.moozic

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.media.MediaPlayer
import android.media.audiofx.AudioEffect
import android.os.Bundle
import android.os.IBinder
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.moozic.databinding.ActivityPlayerBinding
import com.google.android.material.bottomsheet.BottomSheetDialog

class PlayerActivity : AppCompatActivity(),ServiceConnection,MediaPlayer.OnCompletionListener{

    companion object{
       lateinit var musicListPA:ArrayList<Music>
       var songPosition:Int=0
//        var mediaPlayer=null as MediaPlayer?
        var isPlaying:Boolean=false
        var musicService:MusicService?=null
         @SuppressLint("StaticFieldLeak")
         lateinit var binding: ActivityPlayerBinding
         //repeat
         var repeat:Boolean=false
        //timer variables
        var min2:Boolean=false
        var min10:Boolean=false
        var min15:Boolean=false
        var min60:Boolean=false

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.coolPink)
        binding= ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //for starting service
        val intent= Intent(this,MusicService::class.java)
        bindService(intent,this, BIND_AUTO_CREATE)
        startService(intent)

        initializeLayout()
        //back button
        binding.backBtnPA.setOnClickListener{
            finish()
        }

        binding.playPauseBtnPA.setOnClickListener{
            if(isPlaying) pauseMusic()
            else playMusic()
        }
        binding.prevBtnPA.setOnClickListener(){
            prevNextSong(increment = false)
        }
        binding.nxtBtnPA.setOnClickListener(){
            prevNextSong(increment = true)

        }
        binding.seekBarPA.setOnSeekBarChangeListener(object :SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean){
                if(fromUser) musicService!!.mediaPlayer!!.seekTo(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) =Unit

            override fun onStopTrackingTouch(seekBar: SeekBar?)=Unit
        })

        //extrafeatures-repeat share timer
        binding.repeatBtnPA.setOnClickListener{
            if(!repeat){
                repeat=true
                binding.repeatBtnPA.setColorFilter(ContextCompat.getColor(this,R.color.purple_500))
            }else{
                repeat=false
                binding.repeatBtnPA.setColorFilter(ContextCompat.getColor(this,R.color.cool_pink))

            }
        }
        //equalizer
        binding.equalizerBtnPA.setOnClickListener{
            try{
                val eqIntent=Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL)
                eqIntent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, musicService!!.mediaPlayer!!.audioSessionId)
                eqIntent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME,baseContext.packageName)
                eqIntent.putExtra(AudioEffect.EXTRA_CONTENT_TYPE,AudioEffect.CONTENT_TYPE_MUSIC)
                startActivityForResult(eqIntent,10)
            }
            catch (e:Exception){
                Toast.makeText(this,"Equalizer Not Supported!!",Toast.LENGTH_SHORT).show()
            }
        }
        //timer
        binding.timerBtnPA.setOnClickListener { showBottomSheetDialog() }


     }

    private fun setLayout(){

        Glide.with(this)
            .load(musicListPA[songPosition].artUri)
            .apply(RequestOptions().placeholder(R.drawable.music_player_icon).centerCrop())
            .into(binding.crrSongImgPA )
        binding.crrSongNamePA.text= musicListPA[songPosition].title

        if(repeat){
            binding.repeatBtnPA.setColorFilter(ContextCompat.getColor(this,R.color.purple_500))

        }
    }

    private fun createMediaPlayer(){
       try {
           if(musicService!!.mediaPlayer==null) musicService!!.mediaPlayer=MediaPlayer()
           musicService!!.mediaPlayer!!.reset()
           musicService!!.mediaPlayer!!.setDataSource(musicListPA[songPosition].path)
           musicService!!.mediaPlayer!!.prepare()
           musicService!!.mediaPlayer!!.start()
           isPlaying=true
           //when playing pause icon to be displayed
           binding.playPauseBtnPA.setIconResource(R.drawable.pause_icon)
           musicService!!.showNotification(R.drawable.pause_icon)
               //Seekbar edits!
           binding.tvSeekBarStartPA.text= formatDuration(musicService!!.mediaPlayer!!.currentPosition.toLong())
           binding.tvSeekBarEndPA.text= formatDuration(musicService!!.mediaPlayer!!.duration.toLong())
           binding.seekBarPA.progress=0
           binding.seekBarPA.max= musicService!!.mediaPlayer!!.duration
           musicService!!.mediaPlayer!!.setOnCompletionListener (this)


       }catch (e:Exception){return}
    }

    private fun initializeLayout() {
        songPosition = intent.getIntExtra("index", 0)
        when (intent.getStringExtra("class")) {
            "MusicAdapter" -> {
                musicListPA = ArrayList()
                musicListPA.addAll(MainActivity.MusicListMA)
                setLayout()
            }
            "MainActivity"->{
                musicListPA= ArrayList()
                musicListPA.addAll(MainActivity.MusicListMA)
                musicListPA.shuffle()
                setLayout()
                //create media player method shifted to on create service
            }

        }
    }

    private fun playMusic(){
        binding.playPauseBtnPA.setIconResource(R.drawable.pause_icon)
        musicService!!.showNotification(R.drawable.pause_icon)
        isPlaying=true
        musicService!!.mediaPlayer!!.start()

    }

    private fun pauseMusic(){
        binding.playPauseBtnPA.setIconResource(R.drawable.play_icon)
        isPlaying=false
        musicService!!.showNotification(R.drawable.play_icon)

        musicService!!.mediaPlayer!!.pause()
    }

    //fn to skip to nxt song
    private fun prevNextSong(increment:Boolean){
        if(increment==true){
            setSongPosition(increment = true)
//           ++songPosition
            setLayout()
            createMediaPlayer()
        }else{
            setSongPosition(increment = false)

//            --songPosition
            setLayout()
            createMediaPlayer()

        }
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val binder = service as MusicService.MyBinder
        musicService=binder.currentService()
        createMediaPlayer()
        musicService!!.seekBarSetup()
    }

    override fun onServiceDisconnected(p0: ComponentName?) {
        musicService=null
    }

    override fun onCompletion(mp: MediaPlayer?) {
        //on song completion
        setSongPosition(increment = true)
        createMediaPlayer()
        try {
            setLayout()
        }catch (e:java.lang.Exception){
            return
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==10 || resultCode== RESULT_OK){
            return
        }
    }
    //bottomSheet timer
    private fun showBottomSheetDialog(){
        val dialog= BottomSheetDialog(this@PlayerActivity)
        dialog.setContentView(R.layout.bottom_sheet)
        dialog.show()


        dialog.findViewById<LinearLayout>(R.id.min_2)?.setOnClickListener{
            Toast.makeText(baseContext, "Music will Stop After 2 Minutes", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
        dialog.findViewById<LinearLayout>(R.id.min_10)?.setOnClickListener{
            Toast.makeText(baseContext, "Music will Stop After 10 Minutes", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
        dialog.findViewById<LinearLayout>(R.id.min_15)?.setOnClickListener{
            Toast.makeText(baseContext, "Music will Stop After 15 Minutes", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
        dialog.findViewById<LinearLayout>(R.id.min_60)?.setOnClickListener{
            Toast.makeText(baseContext, "Music will Stop After 60 Minutes", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
    }
}
