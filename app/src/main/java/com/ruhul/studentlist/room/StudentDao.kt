package com.ruhul.studentlist.room

import androidx.lifecycle.LiveData
import androidx.room.*
import com.ruhul.studentlist.Student

@Dao
interface StudentDao {
    @Insert
    fun insertStudent(student: Student)

    @Update
    fun updateStudent(student: Student)

    @Delete
    fun deleteStudent(student: Student)

    @Query("delete from student")
    fun deleteAllStudent()

    @Query("select * from student order by id asc")
    fun getAllStudent(): LiveData<List<Student>>


    @Query("select * from student order by id desc")
    fun getDescStudentList(): LiveData<List<Student>>


    //For the Server Operation
    @Insert
    fun insertStudentServer(student: Student)

    @Query("select * from student order by id asc")
    fun getLocalStudents(): LiveData<List<Student>>


}