package com.anubhav.easynotes.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.anubhav.easynotes.R
import com.anubhav.easynotes.database.ItemViewModel
import org.threeten.bp.OffsetDateTime
import java.io.Serializable

@Entity(tableName = "notesTable")
class Note(
    @ColumnInfo(name = "title") var noteTitle: String,
    @ColumnInfo(name = "description") var noteDescription: String,
    @ColumnInfo(name = "timeStamp") var timeStamp: String,
    @ColumnInfo(name = "timeStampDate") var timeStampDate: OffsetDateTime,
    @ColumnInfo(name = "isFavorite") var isFavorite: Boolean = false
) : ItemViewModel, Serializable {

    // on below line we are specifying our key and
    // then auto generate as true and we are
    // specifying its initial value as 0
    @PrimaryKey(autoGenerate = true)
    var taskId = 0

    override var layoutId: Int = R.layout.note_rv_item
    override var viewType: Int = R.layout.note_rv_item

}

fun getEmptyItem(): Note {
    val note = Note("", "", "", OffsetDateTime.now(), false)
    note.layoutId = R.layout.item_not_found
    note.viewType = R.layout.item_not_found
    return note
}