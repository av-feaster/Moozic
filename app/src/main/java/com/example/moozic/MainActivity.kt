package com.example.moozic

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.MediaStore.Audio.Media
import android.view.Menu
import android.view.MenuItem
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.moozic.databinding.ActivityMainBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var  toggle: ActionBarDrawerToggle
    private lateinit var musicAdapter: MusicAdapter
    companion object {
       lateinit var MusicListMA :ArrayList<Music>
        lateinit var musicListSearch:ArrayList<Music>
        var search:Boolean=false

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.coolPinkNav)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)//R.layout.activity_main
        //forNavDrawer
        toggle= ActionBarDrawerToggle(this,binding.root,R.string.open,R.string.close)
        binding.root.addDrawerListener(toggle)
        toggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        if(requestRuntimePermission()) initializeLayout()


        binding.shuffleBtn.setOnClickListener {
//            Toast.makeText(this@MainActivity,"ButtonClicked",Toast.LENGTH_SHORT).show()
            val intent = Intent(this@MainActivity, PlayerActivity::class.java)

            intent.putExtra("index",0)
            intent.putExtra("class","MainActivity")
            startActivity(intent)
        }
        binding.favBtn.setOnClickListener {
            val intent = Intent(this@MainActivity, FavoriteActivity::class.java)
            startActivity(intent)
        }
        binding.playlistBtn.setOnClickListener {
            val intent = Intent(this@MainActivity, PlaylistActivity::class.java)
            startActivity(intent)
        }
        binding.navView.setNavigationItemSelectedListener {
            when(it.itemId){
                R.id.navFeedback -> Toast.makeText(baseContext, "Feedback",Toast.LENGTH_SHORT).show()
                R.id.navSettings -> Toast.makeText(baseContext, "Settings",Toast.LENGTH_SHORT).show()
                R.id.navAbout -> Toast.makeText(baseContext, "About",Toast.LENGTH_SHORT).show()
                R.id.navExit -> {
                    val builder = MaterialAlertDialogBuilder(this)
                    builder.setTitle("Exit")
                        .setMessage("Close App?")
                        .setPositiveButton("Yes"){_,_ ->
                            if(PlayerActivity.musicService!=null) {
                                PlayerActivity.musicService!!.stopForeground(true)
                                PlayerActivity.musicService!!.mediaPlayer!!.release()
                                exitProcess(1)
                            }
                        }
                        .setNegativeButton("No"){dialog,_->
                            dialog.dismiss()
                        }
                    val customDialog = builder.create()
                    customDialog.show()
                    customDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED)
                    customDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.RED)
                }

            }
            true
        }
    }
        //For Requesting Permission
        private fun requestRuntimePermission():Boolean{
            if(ActivityCompat.checkSelfPermission(this,android.Manifest.permission.WRITE_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){
                //request if not true
                ActivityCompat.requestPermissions(this,arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),10)
                return false;
            }
            return true;
        }
       override fun onRequestPermissionsResult(requestCode:Int,permissions:Array<out String>,grantResults:IntArray
       ){
           super.onRequestPermissionsResult(requestCode, permissions, grantResults)
           if(requestCode==10) {
               if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                   Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
//                   MusicListMA=getAllAudio()
                   initializeLayout()
               } else {
                   ActivityCompat.requestPermissions(
                       this,
                       arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                       10
                   )
               }
           }

       }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(toggle.onOptionsItemSelected(item))
            return true
        return super.onOptionsItemSelected(item)
    }
    private fun showToast(msg: String) {
        Toast.makeText(this, "$msg", Toast.LENGTH_LONG).show()
    }
    private fun initializeLayout(){
//        requestRuntimePermission()
//        setTheme(R.style.coolPinkNav)
//        binding = ActivityMainBinding.inflate(layoutInflater)
//        setContentView(binding.root)//R.layout.activity_main
//        //forNavDrawer
//        toggle= ActionBarDrawerToggle(this,binding.root,R.string.open,R.string.close)
//        binding.root.addDrawerListener(toggle)
//        toggle.syncState()
//        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        MusicListMA = getAllAudio()
        binding.musicRV.setHasFixedSize(true)
        binding.musicRV.setItemViewCacheSize(13)
        binding.musicRV.layoutManager=LinearLayoutManager(this@MainActivity)
        musicAdapter= MusicAdapter(this@MainActivity, MusicListMA)
        binding.musicRV.adapter = musicAdapter
        binding.totalSong.text="My Music : "+musicAdapter.itemCount
    }
    @SuppressLint("Range")

    private fun getAllAudio():ArrayList<Music>{
        val tempList = ArrayList<Music>()
        val selection = Media.IS_MUSIC + " != 0"
        val projection = arrayOf(
            Media._ID, Media.TITLE, Media.ALBUM,
            Media.ARTIST, Media.DURATION, Media.DATE_ADDED,
            Media.DATA,MediaStore.Audio.Media.ALBUM_ID)
        //add a spsce before desc
        val cursor =this.contentResolver.query(
            Media.EXTERNAL_CONTENT_URI,projection,selection,null,
            Media.DATE_ADDED +" DESC", null)
        if(cursor!=null) {
            if(cursor.moveToFirst())
                do{
                    val titleC = cursor.getString(cursor.getColumnIndex(Media.TITLE))
                    val idC = cursor.getString(cursor.getColumnIndex(Media._ID))
                    val albumC = cursor.getString(cursor.getColumnIndex(Media.ALBUM))
                    val artistC = cursor.getString(cursor.getColumnIndex(Media.ARTIST))
                    val pathC = cursor.getString(cursor.getColumnIndex(Media.DATA))
                    val durationC = cursor.getLong(cursor.getColumnIndex(Media.DURATION))
                    val albumIDC= cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)).toString()
                    val uri = Uri.parse("content://media/external/audio/albumart")
                    val artUriC =Uri.withAppendedPath(uri,albumIDC).toString()
                    val music=Music(id=idC, title = titleC, album = albumC, artist = artistC,
                        path = pathC, duration = durationC, artUri = artUriC)
                    val file = File(music.path)
                    if(file.exists())
                        tempList.add(music)
                }while (cursor.moveToNext())
                cursor.close()
        }
        return tempList
    }

    override fun onDestroy() {
        super.onDestroy()
        if(!PlayerActivity.isPlaying&&PlayerActivity.musicService!=null){
           closeApplication()

        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.search_view_menu,menu)
        val searchView=menu?.findItem(R.id.searchView)?.actionView as SearchView
        searchView.setOnQueryTextListener(object:SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean=true

            override fun onQueryTextChange(newText: String?): Boolean {
                musicListSearch=ArrayList()
            if(newText!=null){
                val userInput=newText.lowercase()
                for(song in MusicListMA)
                    if(song.title.lowercase().contains(userInput)){
                        musicListSearch.add(song)
                    }
                    search=true
                    musicAdapter.updateMusicList(searchList = musicListSearch)

            }

                return true
            }


        })
        return super.onCreateOptionsMenu(menu)
    }
}