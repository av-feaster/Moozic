package com.example.moozic


import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.moozic.databinding.MusicViewBinding


class MusicAdapter(private val context: Context,private var musicList:ArrayList<Music>):RecyclerView.Adapter<MusicAdapter.MyHolder>(){
    class MyHolder(binding: MusicViewBinding):RecyclerView.ViewHolder(binding.root) {

        val title=binding.songNameMV
        val album = binding.songAlbumMV
        val image = binding.imageMV
        val duration = binding.songDurationMV
        val root=binding.root

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {

        return MyHolder(MusicViewBinding.inflate(LayoutInflater.from(context),parent,false))
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {

        holder.title.text=musicList[position].title
        holder.album.text=musicList[position].album
        holder.duration.text= formatDuration(musicList[position].duration)
        Glide.with(context)
            .load(musicList[position].artUri)
            .apply(RequestOptions().placeholder(R.drawable.music_player_icon).centerCrop())
            .into(holder.image )
        holder.root.setOnClickListener{
            when{
                MainActivity.search->sendIntent(ref="MusicAdapterSearch",posi=position)
                else->sendIntent(ref="MusicAdapter",posi=position)

            }
        }

    }



    override fun getItemCount(): Int {

        return musicList.size
    }

    //update musict list
    fun updateMusicList(searchList:ArrayList<Music>){
        musicList= ArrayList()
        musicList.addAll(searchList)
        notifyDataSetChanged()
    }
    private fun sendIntent(ref:String,posi: Int){
        val intent= Intent(context,PlayerActivity::class.java)
        intent.putExtra("index",posi)
        intent.putExtra("class",ref)
        ContextCompat.startActivity(context,intent,null)

    }
}