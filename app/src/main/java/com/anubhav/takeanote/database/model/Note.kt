package com.anubhav.takeanote.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.anubhav.takeanote.R
import com.anubhav.takeanote.database.ItemViewModel
import java.io.Serializable

@Entity(tableName = "notesTable")
class Note(
    @ColumnInfo(name = "title") var noteTitle: String,
    @ColumnInfo(name = "description") var noteDescription: String,
    @ColumnInfo(name = "timestamp") var timeStamp: String
) : ItemViewModel, Serializable {

    // on below line we are specifying our key and
    // then auto generate as true and we are
    // specifying its initial value as 0
    @PrimaryKey(autoGenerate = true)
    var taskId = 0

    override var layoutId: Int = R.layout.note_rv_item

}