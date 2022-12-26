package com.ruhul.studentlist

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatEditText
import androidx.recyclerview.widget.LinearLayoutManager
import com.ruhul.studentlist.adapter.NormalListAdapter
import com.ruhul.studentlist.adapter.StudentAdapter
import com.ruhul.studentlist.databinding.ActivityMainBinding
import com.ruhul.studentlist.room.StudentDB

class NormalListActivity : AppCompatActivity(), NormalListAdapter.StudentUpdateListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: NormalListAdapter
    private lateinit var studentList: MutableList<Student>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_normal_list)
        initialize()
        clickEvent()
        setAdapter()
        
    }

    private fun setAdapter() {
        adapter = NormalListAdapter(studentList, this,this)
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.studentRV.layoutManager = layoutManager
        binding.studentRV.adapter = adapter
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

        }

        binding.deleteImg.setOnClickListener {
            val builder1 = AlertDialog.Builder(this)
            builder1.setTitle("Warning")
            builder1.setMessage("Are you Sure All Data Clear")
            builder1.setIcon(android.R.drawable.ic_dialog_alert)
            builder1.setPositiveButton("Yes") { _, _ ->
                studentList.clear()
            }
            builder1.setNegativeButton("No") { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            val alertDialog1: AlertDialog = builder1.create()
            alertDialog1.setCancelable(false)
            alertDialog1.show()
        }
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
                studentList.add(student)
                adapter.notifyDataSetChanged()


                Toast.makeText(this, "Student Save Information", Toast.LENGTH_SHORT).show()
                alertDialog.dismiss()
            }
        }

    }

    @SuppressLint("SetTextI18n")
    private fun initialize() {
        studentList = mutableListOf()
        binding.toolbarTitle.text = "StudentDB"

    }

    override fun onItemUpdate(student: Student, position: Int) {

        val dialogBuilder = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.student_from, null)
        dialogBuilder.setView(dialogView)

        val nameEditText = dialogView.findViewById(R.id.nameET) as AppCompatEditText
        val idEditText = dialogView.findViewById(R.id.idET) as EditText
        val submitButton = dialogView.findViewById(R.id.addButton) as Button

        nameEditText.setText(student.name, TextView.BufferType.EDITABLE)
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

                val student: Student = Student(
                    id,
                    idEditText.text.toString().trim().toInt(),
                    nameEditText.text.toString().trim()

                )

                studentList.add(position, student)
                adapter.notifyItemChanged(position)
                alertDialog.dismiss()
            }
        }

    }


}