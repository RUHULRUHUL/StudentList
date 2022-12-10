package com.ruhul.studentlist.callback

import com.ruhul.studentlist.Student

interface UpdateItemListener {
    fun studentUpdate(student: Student)
}