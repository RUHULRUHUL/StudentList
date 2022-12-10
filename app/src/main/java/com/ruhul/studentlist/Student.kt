package com.ruhul.studentlist

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Student")
data class Student(

    @PrimaryKey(autoGenerate = true)
    val ranId: Long,

    @ColumnInfo(name = "id")
    val id: Int,

    @ColumnInfo(name = "title")
    val name: String,


    )