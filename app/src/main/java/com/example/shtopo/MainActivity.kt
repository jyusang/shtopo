package com.example.shtopo

import android.graphics.Rect
import android.os.Bundle
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
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
        setContentView(R.layout.activity_main)
        bindViews()
    }

    override fun onResume() {
        super.onResume()
        handleRefreshLocalBookmarks()
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (ev?.action == MotionEvent.ACTION_DOWN) {
            val v = currentFocus
            if (v is EditText) {
                val rect = Rect()
                v.getGlobalVisibleRect(rect)
                if (!rect.contains(ev.rawX.toInt(), ev.rawY.toInt())) {
                    v.clearFocus()
                    val imm = getSystemService(InputMethodManager::class.java)
                    imm.hideSoftInputFromWindow(v.windowToken, 0)
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    private val buttonSyncBookmarks get() = findViewById<Button>(R.id.main__button__sync_bookmarks)
    private val editApiBaseUrl get() = findViewById<EditText>(R.id.main__edit__api_base_url)
    private val labelLocalBookmarks get() = findViewById<TextView>(R.id.main__label__local_bookmarks)
    private val textLocalBookmarks get() = findViewById<TextView>(R.id.main__text__local_bookmarks)

    private fun bindViews() {
        handleInitEditApiBaseUrl()
        handleRefreshLocalBookmarks()

        editApiBaseUrl.setOnFocusChangeListener { view, isFocused ->
            if (!isFocused) {
                handleUpdateApiBaseUrlConfig(editApiBaseUrl.text.toString())
            }
        }
        buttonSyncBookmarks.setOnClickListener {
            handlePressSyncBookmarks()
        }
    }

    private fun updateEditApiBaseUrlAsReady(value: String) {
        CoroutineScope(Dispatchers.Main).launch {
            editApiBaseUrl.setText(value)
            editApiBaseUrl.isEnabled = true
            editApiBaseUrl.clearFocus()
        }
    }

    private fun updateTextLocalBookmarks(bookmarks: List<Bookmark>) {
        CoroutineScope(Dispatchers.Main).launch {
            val text = bookmarks
                .mapIndexed { index, bookmark -> "[${index}]: ${bookmark.url}" }
                .joinToString("\n")
            textLocalBookmarks.text = text
        }
    }

    private fun updateLabelLocalBookmarks(count: Int) {
        val text = getString(R.string.main__label__local_bookmarks__with_count, count)
        labelLocalBookmarks.text = text
    }

    private fun handleInitEditApiBaseUrl() {
        CoroutineScope(Dispatchers.Default).launch {
            val value = Utils.getConfig(db, ConfigName.API_BASE_URL)
            updateEditApiBaseUrlAsReady(value)
        }
    }

    private fun handleRefreshLocalBookmarks() {
        CoroutineScope(Dispatchers.Default).launch {
            val bookmarks = Utils.listBookmarks(db)
            updateTextLocalBookmarks(bookmarks)
            updateLabelLocalBookmarks(bookmarks.count())
        }
    }

    private fun handleUpdateApiBaseUrlConfig(value: String) {
        CoroutineScope(Dispatchers.Default).launch {
            Utils.setConfig(db, ConfigName.API_BASE_URL, value)
        }
    }

    private fun handlePressSyncBookmarks() {
        CoroutineScope(Dispatchers.Default).launch {
            Utils.syncBookmarks(db)
            val bookmarks = Utils.listBookmarks(db)
            updateTextLocalBookmarks(bookmarks)
            updateLabelLocalBookmarks(bookmarks.count())
        }
    }
}
