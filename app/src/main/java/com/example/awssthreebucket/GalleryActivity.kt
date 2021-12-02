package com.example.awssthreebucket

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amplifyframework.core.Amplify
import com.amplifyframework.storage.options.StorageDownloadFileOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
class GalleryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)

        val filelisttv=findViewById<TextView>(R.id.FileListTV)
        val filedwnbtn=findViewById<Button>(R.id.FileDwnBtn)
        val progressBar=findViewById<ProgressBar>(R.id.progressBar)
        Amplify.Storage.list(
            "",
            { result ->
                GlobalScope.launch(Dispatchers.IO) {
                    val filesTxt = arrayListOf<String>()
                    val files = arrayListOf<String>()
                    result.items.forEach { item ->
                        Log.d("MyAmplifyApp", "Item: " + item.key)
                        filesTxt += item.key +" ${(item.size).div(1000)}KB"
                        files += item.key
                    }
                 withContext(Dispatchers.Main){
                       filelisttv.text = (filesTxt.toString()).replace("]","").replace("[","")
                       filelisttv.visibility = View.VISIBLE

                        filedwnbtn.visibility = View.VISIBLE

                        filedwnbtn.setOnClickListener {
                            progressBar.visibility = View.VISIBLE
                            GlobalScope.launch(Dispatchers.IO){
                                downloadfiles(files)
                            }

                        }
                    }

                }



            },
            { error -> Log.e("MyAmplifyApp", "List failure", error) }
        )




    }

    fun deletefiles(file: S3File, pos: Int, list: ArrayList<S3File>){

        println(file)
        println(pos)
        Log.d("predelete", list.toString())

        suspend fun delfile(): String {
            return suspendCoroutine { continuation ->
                Amplify.Storage.remove(
                    file.origin,
                    { result -> Log.d("MyAmplifyApp", "Successfully removed: " + result.key)
                        continuation.resume("success")


                    },
                    { error -> Log.e("MyAmplifyApp", "Remove failure", error)

                    }
                )
            }
        }

        GlobalScope.launch(Dispatchers.IO){
            val del = delfile()
            if(del == "success"){
                Log.d("delete", del)
                withContext(Dispatchers.Main){
                    list.removeAt(pos)
                   // populaterv(list)
                }
            }
        }


    }






    private fun populaterv(list: ArrayList<S3File>){
        val filenametv=findViewById<TextView>(R.id.FileNameTV)
        val filerecyclerview=findViewById<RecyclerView>(R.id.FileRecyclerView)
        val progressBar=findViewById<ProgressBar>(R.id.progressBar)
        Log.d("downloadlist", list.toString())
        if (filenametv.visibility == View.VISIBLE){
            filenametv.visibility = View.GONE
        }

        if(progressBar.visibility == View.VISIBLE){
            progressBar.visibility = View.GONE
        }


        filerecyclerview.apply {
            layoutManager = GridLayoutManager(this@GalleryActivity,3)
            adapter = PictureAdapter(this@GalleryActivity, list, this@GalleryActivity)
            (adapter as PictureAdapter).notifyDataSetChanged()
        }
    }

    fun downloadprogress(file: String){

        val filenametv=findViewById<TextView>(R.id.FileNameTV)
        filenametv.visibility = View.VISIBLE
        filenametv.text = file//"Downloaded$file"

    }

    private  fun downloadfiles(file: ArrayList<String>)  {

        val downloadFolder = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)

        val filepaths = arrayListOf<S3File>()

        file.forEach { item-> val randomNumber = (1000..9999).random()
            Thread.sleep(500)
            Amplify.Storage.downloadFile(
                item,
                File("$downloadFolder/download$randomNumber.jpg"),
                StorageDownloadFileOptions.defaultInstance(),
                { progress ->
                    Log.d("MyAmplifyApp", "Fraction completed: ${progress.fractionCompleted}")
                },
                { result -> Log.d("MyAmplifyApp", "Successfully downloaded: ${result.file.name} Path: ${result.file.absolutePath}")
                  //  downloadprogress(result.getFile().name)

                    val fileobj = S3File(
                        path = result.file.absolutePath,
                        key = result.file.name, // downloaded filename
                        origin = item, //original filename
                    )
                    filepaths += fileobj
                    println("filelists${filepaths.size} ${file.size}")
                    if(filepaths.size == file.size){
                        populaterv(filepaths)
                    }
                },
                { error -> Log.d("MyAmplifyApp", "Download Failure", error) }
            )

        }


    }
}