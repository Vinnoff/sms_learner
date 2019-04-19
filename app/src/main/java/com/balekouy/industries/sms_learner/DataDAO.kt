package com.balekouy.industries.sms_learner

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy.REPLACE
import android.arch.persistence.room.Query

@Dao
interface DataDAO {
    //0gram (c légé lol)
    @Insert(onConflict = REPLACE)
    fun insert(data0: DataZero)

    @Query("SELECT * FROM data0 order by `count` asc limit 3")
    fun getHint(): List<DataZero>

    @Query("SELECT * FROM data0 order by `count` desc")
    fun getMVP(): List<DataZero>

    @Query("SELECT * FROM data0 WHERE word1 LIKE :word order by `count` desc limit 3" )
    fun getWord(word: String): List<DataZero>

    //1gram
    @Insert(onConflict = REPLACE)
    fun insert(data1: DataUn)

    @Query("SELECT * FROM data1 WHERE word1 = :lastWord order by `count` desc limit 3")
    fun getHint(lastWord: String): List<DataUn>

    @Query("SELECT * FROM data;")
    fun getAll(): List<Data>

    //2gram
    @Insert(onConflict = REPLACE)
    fun insert(data: Data)

    @Query("SELECT * FROM data WHERE word1 = :beforeLastWord AND word2 = :lastWord order by `count` desc limit 3")
    fun getHint(beforeLastWord: String, lastWord: String): List<Data>
}