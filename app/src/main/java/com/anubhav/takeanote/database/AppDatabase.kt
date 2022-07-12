package com.anubhav.takeanote.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.anubhav.takeanote.database.model.Note
import com.anubhav.takeanote.database.model.NoteDao

@Database(entities = [Note::class], version = 2, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {

    companion object {
        // Singleton prevents multiple
        // instances of database opening at the same time.
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "main_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }

    }

    abstract fun getNoteDao(): NoteDao

}