package com.example.shtopo

import android.util.Log
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

enum class ConfigName {
    API_BASE_URL,
}

interface BookmarkService {

    @FormUrlEncoded
    @POST("/")
    suspend fun saveBookmark(@Field("url") url: String): Any
}

object Utils {

    fun getConfig(db: AppDatabase, name: ConfigName): String {
        return db.configDao().getByName(name.name).value
    }

    fun setConfig(db: AppDatabase, name: ConfigName, value: String) {
        return db.configDao().set(Config(name.name, value))
    }

    fun listBookmarks(db: AppDatabase): List<Bookmark> {
        return db.bookmarkDao().getAll()
    }

    fun saveBookmark(db: AppDatabase, bookmark: Bookmark) {
        db.bookmarkDao().insertAll(bookmark)
    }

    fun deleteBookmark(db: AppDatabase, bookmark: Bookmark) {
        db.bookmarkDao().delete(bookmark)
    }

    suspend fun syncBookmark(service: BookmarkService, url: String) {
        val res = service.saveBookmark(url)
    }

    suspend fun syncBookmarks(db: AppDatabase) {
        val apiBaseUrl = getConfig(db, ConfigName.API_BASE_URL)
        val service = buildBookmarkService(apiBaseUrl)
        val bookmarks = listBookmarks(db)
        for (bookmark in bookmarks) {
            try {
                syncBookmark(service, bookmark.url)
            } catch (e: Exception) {
                Log.e(null, "Failed to sync bookmark '${bookmark.url}': ${e}")
                Log.i(null, "Break and wait for next sync")
                break
            }
            deleteBookmark(db, bookmark)
        }
    }

    private fun buildBookmarkService(baseUrl: String): BookmarkService {
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create(BookmarkService::class.java)
    }
}
