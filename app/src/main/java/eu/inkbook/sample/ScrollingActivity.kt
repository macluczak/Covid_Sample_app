package eu.inkbook.sample

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.View.*
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import eu.inkbook.sample.adapter.EntityAdapter
import eu.inkbook.sample.viewmodel.DataViewModel


class ScrollingActivity : AppCompatActivity() {
    private lateinit var viewModel: DataViewModel
    private lateinit var listView: RecyclerView
    private lateinit var progressBar: ProgressBar


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        check()
        setContentView(R.layout.activity_scrolling)
        setSupportActionBar(findViewById(R.id.toolbar))

        //Declarations
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.up_float_quick)
        listView = findViewById(R.id.recyclerList)
        progressBar = findViewById(R.id.progress)

        //Adding ViewModel to view
        viewModel = ViewModelProvider
            .AndroidViewModelFactory
            .getInstance(application)
            .create(DataViewModel::class.java)

        //implementation of the ChangeListener in EditText to track the search term
        findViewById<EditText>(R.id.search).addTextChangedListener {
            viewModel.searchByWord(it.toString())
        }

        //ViewModel, the data load function, the Higher Order function returns
        // information about the data loading process that is in progress/completed
        viewModel.loadEntities {
            if (it) {
                progressBar.visibility = VISIBLE
            } else {
                progressBar.visibility = INVISIBLE
                listView.startAnimation(fadeIn)
            }
        }

        //implementation of the LiveData observer,
        // refreshing the view and maintaining the state after the rotation of the device
        viewModel.listOfData.observe(this) {
            lifecycleScope.launchWhenStarted {
                val customAdapter = viewModel.listOfData.value?.let { EntityAdapter(it) }
                listView.adapter = customAdapter
                listView.layoutManager = LinearLayoutManager(this@ScrollingActivity)
            }
        }

        //using fab, the function of downloading data from the Internet is called,
        //the Higher Order function returns information about the data loading process
        // that is in progress/completed
        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener {
            viewModel.download(
                this@ScrollingActivity.baseContext,
                "https://opendata.ecdc.europa.eu/covid19/casedistribution/csv",
                "Pobieram CSV") {
                if (it) {
                    progressBar.visibility = VISIBLE
                } else {
                    progressBar.visibility = INVISIBLE
                    listView.startAnimation(fadeIn)
                }
            }
        }

    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_scrolling, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.


//      improved reference to application settings
        return when (item.itemId) {
            R.id.action_settings -> {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.parse("package:" + application.packageName)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun check(): Boolean {
        val permissions = arrayOf(
            Manifest.permission.INTERNET,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE)
        var result: Int
        val listPermissionsNeeded: MutableList<String> = ArrayList()
        for (p in permissions) {
            result = ContextCompat.checkSelfPermission(this, p)
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p)
            }
        }
        if (listPermissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toTypedArray(), 100)
            return false
        }
        return true
    }
}