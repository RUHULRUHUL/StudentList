package com.ruhul.studentlist.callback

import com.ruhul.studentlist.Student

interface DeleteItemListener {

    fun  itemDelete(student: Student,position:Int)

}