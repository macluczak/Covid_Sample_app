package eu.inkbook.sample.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import eu.inkbook.sample.data.DataEntity
import eu.inkbook.sample.utils.ImportCsvHelper
import kotlinx.coroutines.*
import java.io.File

//Implementation of viewModel as part of the use of the mvvm architecture pattern
class DataViewModel(application: Application) : AndroidViewModel(application) {
    private val directOfData: File
    private val fileOfData: File
    private val importer: ImportCsvHelper
    private val downloadManager: DownloadManager
    private var repository: ArrayList<DataEntity>
    private var searchState: String

    //LiveData declaration
    //MutableLiveData instance should not be exposed publicly accessible due to data security
    private val _listOfData = MutableLiveData<ArrayList<DataEntity>>()
    val listOfData: LiveData<ArrayList<DataEntity>>
        get() = _listOfData

    init {
        downloadManager = application.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        directOfData = File(application.getExternalFilesDir(null), DataEntity.dir)
        fileOfData = File(directOfData.path, DataEntity.title + DataEntity.extension)
        importer = ImportCsvHelper()
        repository = ArrayList()
        searchState = ""

    }

    // I/O operations are blocking operations, they should be called in the suspend function returning Deferred when calling Async
    private suspend fun importEntities(file: File = fileOfData): ArrayList<DataEntity> =
        importer.save(file).await()

    //function loading data into LiveData and repository, due to async call .await() uses coroutine to collect data
    fun loadEntities(file: File = fileOfData, progress: (Boolean) -> Unit) {

        viewModelScope.launch {
            progress(true)
            repository =
                if (repository.isNotEmpty()) repository else if (checkDoesFileExists()) importEntities(
                    file) else ArrayList()

            _listOfData.postValue(if (searchState.isEmpty()) repository else repository.filter {
                "${it.countriesAndTerritories + it.continentExp + it.geoId} ${it.day}/${it.month}/${it.year}".contains(
                    searchState)
            } as ArrayList<DataEntity>?)

            progress(false)
        }
    }

    //function returns boolean depending on the sum of download statuses (Running, Pause, Pending)
    private fun isDownloading(): Boolean {

        return (downloadManager.query(DownloadManager.Query()
            .setFilterByStatus(DownloadManager.STATUS_RUNNING)).count
                + downloadManager.query(DownloadManager.Query()
            .setFilterByStatus(DownloadManager.STATUS_PAUSED)).count
                + downloadManager.query(DownloadManager.Query()
            .setFilterByStatus(DownloadManager.STATUS_PENDING)).count) >= 1
    }

    //Operation to delete a given file
    //I/O operations should be triggered in coroutine along with Dispatcher.IO due to their blocking nature
    private fun deleteFile(file: File = fileOfData): Job =
        CoroutineScope(Dispatchers.IO).launch { file.delete() }

    fun searchByWord(word: String) {
        searchState = word
        if (repository.isNotEmpty()) {
            _listOfData.postValue(repository.filter {
                "${it.countriesAndTerritories + it.continentExp + it.geoId} ${it.day}/${it.month}/${it.year}".contains(
                    searchState)
            } as ArrayList<DataEntity>?)
        }
    }

    private fun checkDoesFileExists(): Boolean {
        if (!directOfData.isDirectory) {
            directOfData.mkdirs()
            Log.i("Path", "/${DataEntity.dir} has been created!")
        } else {
            Log.i("Path", "/${DataEntity.dir} already exists!")
            return File(directOfData.path).list()?.contains(DataEntity.title + DataEntity.extension)
                ?: false

        }
        return false
    }

    fun download(
        baseActivity: Context,
        url: String?,
        title: String?,
        progress: (Boolean) -> Unit,
    ): Long {

        if (!isOnline(baseActivity)) {
            Toast.makeText(baseActivity, "Internet required!", Toast.LENGTH_SHORT).show()
            return -1L
        }


        val downloadReference: Long = if (!isDownloading()) {
            val uri = Uri.parse(url)
            val request = DownloadManager.Request(uri).apply {

                setDestinationInExternalFilesDir(
                    baseActivity, DataEntity.dir, DataEntity.title + DataEntity.extension
                )
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setTitle(title)
                progress(true)

            }
            Toast.makeText(baseActivity, "Start downloading..", Toast.LENGTH_SHORT).show()
            if (checkDoesFileExists()) deleteFile(fileOfData)
            downloadManager.enqueue(request)

        } else {
            Toast.makeText(baseActivity, "Wait for download!", Toast.LENGTH_SHORT).show()
            -1L
        }

        Log.i("Start Download", downloadReference.toString())

        if (downloadReference != -1L) {
            val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)


            val downloadReceiver: BroadcastReceiver = object : BroadcastReceiver() {
                @SuppressLint("Range")
                override fun onReceive(context: Context, intent: Intent) {

                    progress(false)

                    val downloadId =
                        intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                    Toast.makeText(baseActivity, "Download Complete!", Toast.LENGTH_SHORT).show()

                    if (downloadId == -1L) return

                    // query download status
                    val cursor: Cursor =
                        downloadManager.query(DownloadManager.Query().setFilterById(downloadId))
                    if (cursor.moveToFirst()) {
                        val status: Int =
                            cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                        if (status == DownloadManager.STATUS_SUCCESSFUL) {
                            Log.i("Download Successful", downloadReference.toString())


                            // download is successful
                            val uri: String =
                                cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))

                            val file = File(Uri.parse(uri).path.toString())

                            loadEntities(file) { }

                            baseActivity.unregisterReceiver(this)
                        } else {
                            // download is assumed cancelled

                        }
                    } else {
                        // download is assumed cancelled
                    }
                }
            }

            baseActivity.registerReceiver(downloadReceiver, filter)
        }
        return downloadReference

    }

    private fun isOnline(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        if (capabilities != null) {
            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                Log.i("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
                return true
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
                return true
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                Log.i("Internet", "NetworkCapabilities.TRANSPORT_ETHERNET")
                return true
            }
        }
        return false
    }


}