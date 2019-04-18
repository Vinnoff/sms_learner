package com.balekouy.industries.sms_learner

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "data0")
data class DataZero(
    @PrimaryKey(autoGenerate = true) var id: Int?,
    @ColumnInfo(name = "word1") var word1: String,
    @ColumnInfo(name = "count") var count: Int
) {
    constructor() : this(null, "", 0)
}