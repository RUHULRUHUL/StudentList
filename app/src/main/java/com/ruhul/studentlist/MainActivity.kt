package com.ruhul.studentlist

import android.annotation.SuppressLint
import android.os.AsyncTask
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.ruhul.studentlist.adapter.StudentAdapter
import com.ruhul.studentlist.callback.DeleteItemListener
import com.ruhul.studentlist.callback.UpdateItemListener
import com.ruhul.studentlist.databinding.ActivityMainBinding
import com.ruhul.studentlist.room.StudentDB

class MainActivity : AppCompatActivity(), StudentAdapter.StudentUpdate {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: StudentAdapter
    private lateinit var studentList: List<Student>
    private lateinit var studentDB: StudentDB


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initialize()
        studetnList()
        clickEvent()

    }

    private fun clickEvent() {
        binding.insertBtn.setOnClickListener {
            studentForm()
        }

        binding.searchStudentId.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(id: CharSequence?, p1: Int, p2: Int, p3: Int) {
                adapter.filter.filter(id)
            }

            override fun afterTextChanged(p0: Editable?) {
            }

        })


        binding.filterImg.setOnClickListener {
            filterAscToDesc()
        }

        binding.deleteImg.setOnClickListener {
            val builder1 = AlertDialog.Builder(this)
            //set title for alert dialog
            builder1.setTitle("Warning")
            //set message for alert dialog
            builder1.setMessage("Are you Sure All Data Clear")
            builder1.setIcon(android.R.drawable.ic_dialog_alert)

            //performing positive action
            builder1.setPositiveButton("Yes") { dialogInterface, which ->
                studentDB.studentDao().deleteAllStudent()
            }
            builder1.setNegativeButton("No") { dialogInterface, which ->
                dialogInterface.dismiss()
            }
            // Create the AlertDialog
            val alertDialog1: AlertDialog = builder1.create()
            // Set other dialog properties
            alertDialog1.setCancelable(false)
            alertDialog1.show()
        }

    }

    private fun initialize() {
        studentList = arrayListOf()
        studentDB = StudentDB.getInstance(this)
        binding.toolbarTitle.text = "StudentDB"

    }

    private fun studetnList() {
        getStudentList().observe(this) {
            adapter = StudentAdapter(it, this, this)
            val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
            binding.studentRV.layoutManager = layoutManager
            binding.studentRV.adapter = adapter

        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun filterAscToDesc() {
        val dialogBuilder = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.filter_alert, null)
        dialogBuilder.setView(dialogView)

        val ascRadioButton = dialogView.findViewById(R.id.ascBtn) as RadioButton
        val descRadioButton = dialogView.findViewById(R.id.descBtn) as RadioButton
        val filterButton = dialogView.findViewById(R.id.filterBtn) as Button
        val radioGroup = dialogView.findViewById(R.id.filterRadioGroup) as RadioGroup

        val alertDialog = dialogBuilder.create()
        alertDialog.show()


        filterButton.setOnClickListener {
            alertDialog.dismiss()
            val id = radioGroup.id
            if (id > 0) {
                if (descRadioButton.isChecked) {
                    studentDB.studentDao().getDescStudentList().observe(this, Observer {
                        Toast.makeText(this, "Descending List", Toast.LENGTH_SHORT).show()
                        adapter = StudentAdapter(it, this, this)
                        val layoutManager =
                            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
                        binding.studentRV.layoutManager = layoutManager
                        binding.studentRV.adapter = adapter
                    })

                } else {
                    studetnList()
                }
            }
        }

/*        adapter.deleteItemListener(deleteItemListener = object : DeleteItemListener {
            override fun itemDelete(student: Student, position: Int) {
                DeleteStudent().execute(student)
                adapter.notifyItemRemoved(position)
            }

        })

        adapter.updateItemListener(updateItemListener = object : UpdateItemListener {
            override fun studentUpdate(student: Student) {
                UpdateStudent().execute(student)
            }

        })*/

    }


    @SuppressLint("NotifyDataSetChanged")
    private fun studentForm() {
        val dialogBuilder = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.student_from, null)
        dialogBuilder.setView(dialogView)

        val nameEditText = dialogView.findViewById(R.id.nameET) as EditText
        val idEditText = dialogView.findViewById(R.id.idET) as EditText
        val submitButton = dialogView.findViewById(R.id.addButton) as Button
        val alertDialog = dialogBuilder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()


        submitButton.setOnClickListener {

            if (nameEditText.text.toString().isEmpty()) {
                nameEditText.error = "enter name"
            } else if (idEditText.text.toString().isEmpty()) {
                idEditText.error = "enter id"
            } else {

                val student = Student(
                    0,
                    idEditText.text.toString().trim().toInt(),
                    nameEditText.text.toString().trim()

                )
                InsertStudent().execute(student)
                Toast.makeText(this, "Student Save Information", Toast.LENGTH_SHORT).show()
                alertDialog.dismiss()
            }
        }

        adapter.deleteItemListener(deleteItemListener = object : DeleteItemListener {
            override fun itemDelete(student: Student, position: Int) {
                DeleteStudent().execute(student)
                adapter.notifyItemRemoved(position)
            }

        })

        adapter.updateItemListener(updateItemListener = object : UpdateItemListener {
            override fun studentUpdate(student: Student) {
                UpdateStudent().execute(student)
            }

        })


    }

    private fun getStudentList(): LiveData<List<Student>> {
        return studentDB.studentDao().getAllStudent()
    }

    override fun studentUpdate(student: Student, position: Int) {
        val dialogBuilder = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.student_from, null)
        dialogBuilder.setView(dialogView)

        val nameEditText = dialogView.findViewById(R.id.nameET) as AppCompatEditText
        val idEditText = dialogView.findViewById(R.id.idET) as EditText
        val submitButton = dialogView.findViewById(R.id.addButton) as Button

        nameEditText.setText(student.name.toString(), TextView.BufferType.EDITABLE)
        idEditText.setText(student.id.toString(), TextView.BufferType.EDITABLE)

        val alertDialog = dialogBuilder.create()
        alertDialog.show()

        submitButton.setOnClickListener {
            if (nameEditText.text.toString().isEmpty()) {
                nameEditText.error = "enter name"
            } else if (idEditText.text.toString().isEmpty()) {
                idEditText.error = "enter id"
            } else {

                val id = student.ranId

                val student = Student(
                    id,
                    idEditText.text.toString().trim().toInt(),
                    nameEditText.text.toString().trim()

                )

                // studentDB.studentDao().updateStudent(student)
                UpdateStudent().execute(student)
                alertDialog.dismiss()
            }
        }

    }


    @SuppressLint("StaticFieldLeak")
    inner class InsertStudent : AsyncTask<Student?, Void?, Void?>() {
        @Deprecated("Deprecated in Java")
        override fun doInBackground(vararg student: Student?): Void? {
            student[0]?.let { studentDB.studentDao().insertStudent(it) }
            return null
        }
    }

    @SuppressLint("StaticFieldLeak")
    inner class UpdateStudent :
        AsyncTask<Student?, Void?, Void?>() {
        override fun doInBackground(vararg student: Student?): Void? {
            student[0]?.let { studentDB.studentDao().updateStudent(it) }

            return null
        }
    }

    inner class DeleteStudent :
        AsyncTask<Student?, Void?, Void?>() {
        override fun doInBackground(vararg student: Student?): Void? {
            student[0]?.let { studentDB.studentDao().deleteStudent(it) }
            return null
        }

    }

    inner class DeleteAllStudent :
        AsyncTask<Void?, Void?, Void?>() {
        override fun doInBackground(vararg p0: Void?): Void? {
            studentDB.studentDao().deleteAllStudent()
            return null
        }

    }

}