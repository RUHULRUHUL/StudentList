package com.ruhul.studentlist

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.*
import com.ruhul.studentlist.adapter.StudentAdapter
import com.ruhul.studentlist.databinding.ActivityMainBinding
import com.ruhul.studentlist.receiver.SyncTimeReceiver
import com.ruhul.studentlist.room.StudentDB
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit


@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity(), StudentAdapter.StudentUpdate {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: StudentAdapter
    private lateinit var studentList: List<Student>
    private lateinit var studentDB: StudentDB


    private val logDebug = "SyncAdapterDebugTest"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initialize()
        studentList()
        clickEvent()

        //workManager use for schedule
        uploadDataWorkManager()

        //alarmSchedule use for Sync Adapter
        //setAlarmSchedule()

    }

    private fun setAlarmSchedule() {

        Log.d(logDebug, "call - setAlarmSchedule: ")
        val intent = Intent(this, SyncTimeReceiver::class.java)
        val pendingIntent =
            PendingIntent.getBroadcast(this.applicationContext, 234324243, intent, 0)

        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis(),
            1 * 60 * 1000,
            pendingIntent
        )

/*        getStudentList().observe(this) {
            if (it.isEmpty()) {
                alarmManager.cancel(pendingIntent)
                Toast.makeText(this, "alarm cancel", Toast.LENGTH_SHORT).show()
            }
        }*/

    }

    private fun uploadDataWorkManager() {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val periodicWorkRequest =
            PeriodicWorkRequestBuilder<FileUploadBackground>(15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()

        WorkManager.getInstance().enqueueUniquePeriodicWork(
            "FileUpload",
            ExistingPeriodicWorkPolicy.KEEP,
            periodicWorkRequest
        )

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
            builder1.setTitle("Warning")
            builder1.setMessage("Are you Sure All Data Clear")
            builder1.setIcon(android.R.drawable.ic_dialog_alert)
            builder1.setPositiveButton("Yes") { _, _ ->
                studentDB.studentDao().deleteAllStudent()
            }
            builder1.setNegativeButton("No") { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            val alertDialog1: AlertDialog = builder1.create()
            alertDialog1.setCancelable(false)
            alertDialog1.show()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initialize() {
        studentList = arrayListOf()
        studentDB = StudentDB.getInstance(this)
        binding.toolbarTitle.text = "StudentDB"

    }

    private fun studentList() {
        getStudentList().observe(this) {

            Log.d(
                "studentList",
                "call - Observer :getLocalStudents List size: " + it.size
            )

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
                    studentDB.studentDao().getDescStudentList().observe(this) {
                        Toast.makeText(this, "Descending List", Toast.LENGTH_SHORT).show()
                        adapter = StudentAdapter(it, this, this)
                        val layoutManager =
                            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
                        binding.studentRV.layoutManager = layoutManager
                        binding.studentRV.adapter = adapter
                    }

                } else {
                    studentList()
                }
            }
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
        alertDialog.setCanceledOnTouchOutside(true)
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

                val student = Student(
                    id,
                    idEditText.text.toString().trim().toInt(),
                    nameEditText.text.toString().trim()

                )

                UpdateStudent().execute(student)
                alertDialog.dismiss()
            }
        }

    }

    override fun onStudentDelete(student: Student, position: Int) {
        studentDB.studentDao().deleteStudent(student)
        adapter.notifyItemRemoved(position)
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
        @Deprecated("Deprecated in Java")
        override fun doInBackground(vararg student: Student?): Void? {
            student[0]?.let { studentDB.studentDao().updateStudent(it) }

            return null
        }
    }


}