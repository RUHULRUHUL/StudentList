package com.ruhul.studentlist.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.ruhul.studentlist.Student
import com.ruhul.studentlist.callback.DeleteItemListener
import com.ruhul.studentlist.callback.UpdateItemListener
import com.ruhul.studentlist.databinding.StudentRowItemBinding
import com.ruhul.studentlist.room.StudentDB
import java.util.*

class StudentAdapter(
    private val studentList: List<Student>,
    private val context: Context,
    private val studentUpdate: StudentUpdate
) : RecyclerView.Adapter<StudentAdapter.ViewHolder>(), Filterable {
    private var filterStudentList: List<Student> = studentList


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            StudentRowItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val student = filterStudentList[position]
        holder.binding.userNameTxt.text = student.name
        holder.binding.userId.text = student.id.toString()

        holder.binding.deleteImg.setOnClickListener {
            studentUpdate.onStudentDelete(filterStudentList[position], position)
        }

        holder.binding.editInfo.setOnClickListener {
            studentUpdate.studentUpdate(
                filterStudentList[position],
                position
            )
        }

    }

    override fun getItemCount(): Int {
        return filterStudentList.size
    }

    interface StudentUpdate {
        fun studentUpdate(student: Student, position: Int)
        fun onStudentDelete(student: Student, position: Int)

    }

    class ViewHolder(val binding: StudentRowItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val charString = charSequence.toString()
                filterStudentList = if (charString.isEmpty()) {
                    studentList
                } else {
                    val filteredList: MutableList<Student> = ArrayList<Student>()
                    for (student in studentList) {
                        if (student.id.toString()
                                .contains(charString.lowercase(Locale.getDefault()))
                        ) {
                            filteredList.add(student)
                        }
                    }
                    filteredList
                }
                val filterResults = FilterResults()
                filterResults.values = filterStudentList
                return filterResults
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
                filterStudentList = filterResults.values as List<Student>
                notifyDataSetChanged()
            }
        }
    }

}