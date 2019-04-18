package com.balekouy.industries.sms_learner

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy.REPLACE
import android.arch.persistence.room.Query

@Dao
interface DataDAO {
    @Query("SELECT * FROM data;")
    fun getAll(): List<Data>

    @Insert(onConflict = REPLACE)
    fun insert(data: Data)

    @Query("SELECT * FROM data WHERE word1 = :beforeLastWord AND word2 = :lastWord order by `index` asc limit 3")
    fun getHint(beforeLastWord: String, lastWord: String): List<Data>

    @Query("SELECT * FROM data WHERE word1 = :lastWord order by `index` asc limit 3")
    fun getHint(lastWord: String): List<Data>

    @Query("SELECT * FROM data order by `index` asc limit 3")
    fun getHint(): List<Data>

    @Insert(onConflict = REPLACE)
    fun insert(data0: DataZero)

    @Query("SELECT * FROM data0 order by `count` desc limit 1000")
    fun getMVP(): List<DataZero>
}