package dev.reader.ldx

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.*
import kotlinx.coroutines.flow.first

private val Context.readerPreferences by preferencesDataStore("reader-preferences")
@Entity(tableName = "books")
data class BookRecord(@PrimaryKey val id: String, val path: String, val title: String, val language: String, val committedLocator: String? = null)
@Dao
interface BookDao {
    @Query("SELECT * FROM books ORDER BY title") suspend fun all(): List<BookRecord>
    @Query("SELECT * FROM books WHERE id = :id") suspend fun get(id: String): BookRecord?
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun insert(book: BookRecord)
    @Query("UPDATE books SET committedLocator = :locator WHERE id = :id") suspend fun commit(id: String, locator: String)
}
@Database(entities = [BookRecord::class, SliderReturnRecord::class], version = 2, exportSchema = false)
abstract class ReaderDatabase : RoomDatabase() { abstract fun books(): BookDao; abstract fun slider(): SliderReturnDao }
class ReaderStorage(private val context: Context) {
    val database = Room.databaseBuilder(context, ReaderDatabase::class.java, "reader.db").addMigrations(object : androidx.room.migration.Migration(1, 2) {
        override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) { db.execSQL("CREATE TABLE IF NOT EXISTS slider_return (bookId TEXT NOT NULL PRIMARY KEY, locatorJson TEXT NOT NULL)") }
    }).build()
    val books = database.books()
    private val last = stringPreferencesKey("last-book")
    private val font = doublePreferencesKey("font-size")
    private val margins = doublePreferencesKey("page-margins")
    suspend fun lastBook(): String? = context.readerPreferences.data.first()[last]
    suspend fun opened(id: String) { context.readerPreferences.edit { it[last] = id } }
    suspend fun typography(): Pair<Double, Double> = context.readerPreferences.data.first().let { (it[font] ?: 100.0) to (it[margins] ?: 1.0) }
    suspend fun typography(size: Double, margin: Double) { context.readerPreferences.edit { it[font] = size; it[margins] = margin } }
}
