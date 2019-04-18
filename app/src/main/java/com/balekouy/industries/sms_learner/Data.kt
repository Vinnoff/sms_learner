package com.balekouy.industries.sms_learner

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "data")
data class Data(
    @PrimaryKey(autoGenerate = true) var id: Int?,
    @ColumnInfo(name = "word1") var word1: String,
    @ColumnInfo(name = "word2") var word2: String,
    @ColumnInfo(name = "word3") var word3: String,
    @ColumnInfo(name = "count") var count: Int,
    @ColumnInfo(name = "index") var index: Int
) {
    constructor() : this(null, "", "", "", 0, 0)
}
