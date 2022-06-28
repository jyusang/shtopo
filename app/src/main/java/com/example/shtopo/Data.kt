package com.example.shtopo

import androidx.room.*

@Entity(tableName = "configs")
data class Config(

    @PrimaryKey
    val name: String,

    @ColumnInfo(name = "value")
    val value: String,
)

@Dao
interface ConfigDao {

    @Query("SELECT * FROM configs WHERE name = :name")
    fun getByName(name: String): Config

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun set(config: Config)

    @Delete
    fun delete(config: Config)
}

@Entity(tableName = "bookmarks")
data class Bookmark(

    @PrimaryKey(autoGenerate = true)
    val uid: Int = 0,

    @ColumnInfo(name = "url")
    val url: String,
)

@Dao
interface BookmarkDao {

    @Query("SELECT * FROM bookmarks")
    fun getAll(): List<Bookmark>

    @Insert
    fun insertAll(vararg bookmarks: Bookmark)

    @Delete
    fun delete(bookmark: Bookmark)
}

@Database(
    entities = [
        Config::class,
        Bookmark::class,
    ],
    version = 1,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun configDao(): ConfigDao
    abstract fun bookmarkDao(): BookmarkDao
}
