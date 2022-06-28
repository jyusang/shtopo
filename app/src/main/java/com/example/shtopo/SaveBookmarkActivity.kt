package com.example.shtopo

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SaveBookmarkActivity : AppCompatActivity() {

    private var _db: AppDatabase? = null
    private val db: AppDatabase
        get() {
            if (_db == null) {
                _db = Room.databaseBuilder(
                    applicationContext,
                    AppDatabase::class.java,
                    "app-database"
                ).build()
            }
            return _db!!
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_save_bookmark)
        intent.getStringExtra(Intent.EXTRA_TEXT)?.let {
            handleSendText(it)
        }
    }

    private fun handleSendText(text: String) {
        CoroutineScope(Dispatchers.Default).launch {
            Utils.saveBookmark(db, Bookmark(url = text))
            Utils.syncBookmarks(db)
            updateStatusLabelAsSaved()
            delay(1000)
            finish()
        }
    }

    private fun updateStatusLabelAsSaved() {
        CoroutineScope(Dispatchers.Main).launch {
            findViewById<TextView>(R.id.save_bookmark__status_label)
                .setText(R.string.save_bookmark__label__saved)
        }
    }
}
