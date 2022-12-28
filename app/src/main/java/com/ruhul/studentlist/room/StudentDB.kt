package com.ruhul.studentlist.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.ruhul.studentlist.Student

@Database(entities = [Student::class], version = 1, exportSchema = false)
abstract class StudentDB : RoomDatabase() {

    abstract fun studentDao(): StudentDao

    companion object {
        @Volatile
        private var Instance: StudentDB? = null

        fun getInstance(context: Context): StudentDB {
            if (Instance == null) {
                synchronized(this) {
                    Instance = Room.databaseBuilder(
                        context,
                        StudentDB::class.java,
                        "studentDB"
                    )
                        .fallbackToDestructiveMigration()
                        .allowMainThreadQueries()
                        .build()
                }
            }

            return Instance!!
        }
    }


}