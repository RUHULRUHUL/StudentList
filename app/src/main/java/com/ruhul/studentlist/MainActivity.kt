package com.ruhul.studentlist

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.ruhul.studentlist.adapter.StudentAdapter
import com.ruhul.studentlist.databinding.ActivityMainBinding
import com.ruhul.studentlist.model.post.Post
import com.ruhul.studentlist.model.post.PostResponse
import com.ruhul.studentlist.network.RetrofitClient
import com.ruhul.studentlist.room.StudentDB
import com.ruhul.studentlist.sync.syncAdapter.SyncAdapter
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity(), StudentAdapter.StudentUpdate {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: StudentAdapter
    private lateinit var studentList: List<Student>
    private lateinit var studentDB: StudentDB
    private lateinit var syncAdapter: SyncAdapter
    private val logDebug = "SyncAdapterDebugTest"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        syncAdapter = SyncAdapter(this, true)


        initialize()
        studentList()
        clickEvent()
        //PeriodicWiseTimeUpload()

    }

    private fun PeriodicWiseTimeUpload() {
        //5 minutes
        val timer = object : CountDownTimer(300 * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() {
                Toast.makeText(this@MainActivity, "Start Upload..", Toast.LENGTH_SHORT).show()

                getStudentList().observe(this@MainActivity) {
                    if (it.isNotEmpty()) {
                        uploadData(it)
                    }

                }
            }
        }
        timer.start()
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


    private fun uploadData(students: List<Student>) {
        Log.d(logDebug, "call -: StartNotification ...")
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
        val builder = NotificationCompat.Builder(this, "CHANNEL_ID")
            .setSmallIcon(R.drawable.ic_baseline_cloud_upload_24)
            .setContentTitle("Data Uploading")
            .setContentText("upload ..")
            .setOngoing(false)
            .setAllowSystemGeneratedContextualActions(false)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        for (i in students.indices) {
            val student = students[i]
            val post = Post(student.id, student.name)
            builder.setProgress(students.size, i, false)
            notificationManager.notify(2, builder.build())
            //delay(5000)
            RetrofitClient.getApiServices()
                .postData(post)
                .enqueue(object : Callback<PostResponse?> {
                    override fun onResponse(
                        call: Call<PostResponse?>,
                        response: Response<PostResponse?>
                    ) {
                        if (response.isSuccessful) {
                            Log.d(logDebug, "call -: RetrofitClient onResponse ...")

                            studentDB.studentDao().deleteStudent(students[i])

                        }
                    }

                    override fun onFailure(call: Call<PostResponse?>, t: Throwable) {
                        Log.d(logDebug, "call -: RetrofitClient onFailure ...")

                    }
                })
        }
        builder.setProgress(0, 0, false)
        builder.clearActions()
        builder.setAutoCancel(true)
        builder.setContentTitle("sync adapter")
        builder.setContentText("file upload completed")
        builder.setContentIntent(null)
        notificationManager.notify(2, builder.build())

    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name: CharSequence = "Notification Channel"
            val description = "this is for Test Notification"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("CHANNEL_ID", name, importance)
            channel.description = description
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }


    @SuppressLint("NotifyDataSetChanged")
    private fun filterAscToDesc() {
        val dialogBuilder = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.filter_alert, null)
        dialogBuilder.setView(dialogView)

        //val ascRadioButton = dialogView.findViewById(R.id.ascBtn) as RadioButton
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

/*    inner class DeleteStudent :
        AsyncTask<Student?, Void?, Void?>() {
        override fun doInBackground(vararg student: Student?): Void? {
            student[0]?.let { studentDB.studentDao().deleteStudent(it) }
            return null
        }

    }*/

/*    inner class DeleteAllStudent :
        AsyncTask<Void?, Void?, Void?>() {
        @Deprecated("Deprecated in Java")
        override fun doInBackground(vararg p0: Void?): Void? {
            studentDB.studentDao().deleteAllStudent()
            return null
        }

    }*/

}