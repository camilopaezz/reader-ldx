package dev.reader.ldx

import android.content.Context
import androidx.room.*
import org.json.JSONObject
import org.readium.r2.shared.publication.Locator

/** Durable publication-relative ranges, independent of reading-position commitment. */
@Entity(tableName = "annotations", indices = [Index("bookId")])
data class AnnotationRecord(
    @PrimaryKey val id: String,
    val bookId: String,
    val kind: String,
    val locatorJson: String,
    val chapter: String,
    val excerpt: String,
    val color: String = "Yellow",
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    fun locator(): Locator = requireNotNull(Locator.fromJSON(JSONObject(locatorJson)))
}
@Dao interface AnnotationDao {
    @Query("SELECT * FROM annotations WHERE bookId = :bookId ORDER BY createdAt")
    suspend fun all(bookId: String): List<AnnotationRecord>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun save(record: AnnotationRecord)
    @Query("DELETE FROM annotations WHERE id = :id") suspend fun remove(id: String)
}
@Database(entities = [AnnotationRecord::class], version = 1, exportSchema = false)
abstract class AnnotationDatabase : RoomDatabase() { abstract fun annotations(): AnnotationDao }
class AnnotationStore(context: Context) {
    // A separate Room database keeps this slice's migration independent from dictionary import.
    val database = Room.databaseBuilder(context, AnnotationDatabase::class.java, "annotations.db").build()
    val records = database.annotations()
}
