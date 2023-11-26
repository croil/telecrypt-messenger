package tele.crypt.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    version = 1,
    entities = [
        MessageEntity::class
    ]
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao
    companion object {
        private var instance: AppDatabase? = null
        fun getInstance(context: Context): AppDatabase {
            if (instance == null) {
                instance =
                    Room.databaseBuilder(context, AppDatabase::class.java, "database.db").build()
            }
            return instance as AppDatabase
        }

        fun closeDatabase() {
            instance?.close()
            instance = null
        }
    }
}