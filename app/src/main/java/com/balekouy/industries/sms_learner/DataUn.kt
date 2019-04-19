package com.balekouy.industries.sms_learner

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "data1")
data class DataUn(
    @PrimaryKey(autoGenerate = true) var id: Int?,
    @ColumnInfo(name = "word1") var word1: String,
    @ColumnInfo(name = "word2") var word2: String,
    @ColumnInfo(name = "count") var count: Int
) {
    constructor() : this(null, "", "", 0)
}