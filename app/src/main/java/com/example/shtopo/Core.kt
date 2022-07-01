package com.example.shtopo

import android.util.Log
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.http.Url

enum class ConfigName {
    API_ENDPOINT,
}

interface BookmarkService {

    @FormUrlEncoded
    @POST
    suspend fun saveBookmark(@Url endpoint: String, @Field("url") url: String): Any
}

object Utils {

    fun getConfig(db: AppDatabase, name: ConfigName): String {
        return (db.configDao().getByName(name.name))?.value ?: ""
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

    suspend fun syncBookmark(service: BookmarkService, endpoint: String, url: String) {
        service.saveBookmark(endpoint, url)
    }

    suspend fun syncBookmarks(db: AppDatabase) {
        val apiEndpoint = getConfig(db, ConfigName.API_ENDPOINT)
        val service = buildBookmarkService()
        val bookmarks = listBookmarks(db)
        for (bookmark in bookmarks) {
            try {
                syncBookmark(service, apiEndpoint, bookmark.url)
            } catch (e: Exception) {
                Log.e(null, "Failed to sync bookmark '${bookmark.url}': ${e}")
                Log.i(null, "Break and wait for next sync")
                break
            }
            deleteBookmark(db, bookmark)
        }
    }

    private fun buildBookmarkService(): BookmarkService {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://0.0.0.0/") // Dummy
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create(BookmarkService::class.java)
    }
}
