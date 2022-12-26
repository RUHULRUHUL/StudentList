package com.ruhul.studentlist.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.ruhul.studentlist.Student
import com.ruhul.studentlist.callback.DeleteItemListener
import com.ruhul.studentlist.databinding.StudentRowItemBinding
import com.ruhul.studentlist.room.StudentDB
import java.util.*

class NormalListAdapter(
    private val studentList: MutableList<Student>,
    private val context: Context,
    private val studentUpdateListener: StudentUpdateListener
) : RecyclerView.Adapter<NormalListAdapter.ViewHolder>(), Filterable {
    private var filterStudentList: MutableList<Student> = studentList


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            StudentRowItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val student = filterStudentList[holder.adapterPosition]
        holder.binding.userNameTxt.text = student.name
        holder.binding.userId.text = student.id.toString()

        holder.binding.deleteImg.setOnClickListener {
            filterStudentList.removeAt(holder.adapterPosition)
            notifyItemRemoved(holder.adapterPosition)
        }

        holder.binding.editInfo.setOnClickListener {
            studentUpdateListener.onItemUpdate(
                filterStudentList[holder.adapterPosition],
                holder.adapterPosition
            )
        }
    }

    public interface StudentUpdateListener {
        fun onItemUpdate(student: Student, position: Int)
    }


    override fun getItemCount(): Int {
        return filterStudentList.size
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
                filterStudentList = filterResults.values as MutableList<Student>
                notifyDataSetChanged()
            }
        }
    }
}

