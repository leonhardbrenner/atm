package dbManagers

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import generated.model.db.SeedsDb
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import jsonLoaders.SeedsJsonLoaders
import org.jetbrains.exposed.sql.insertAndGetId
import java.io.File

fun resource(path: String) = File(ClassLoader.getSystemResource(path).file)
fun resourceText(path: String) = resource(path).readText()

object SeedsDBManager {
    val kMapper = ObjectMapper().registerModule(KotlinModule())

    fun drop() = transaction {
        SchemaUtils.drop(SeedsDb.DetailedSeed.Table)
    }
    fun create() = transaction {
        SchemaUtils.create(SeedsDb.DetailedSeed.Table)
    }
    fun populate() = transaction {
        val jsonLoaders = SeedsJsonLoaders(kMapper)
        jsonLoaders.detailedSeeds.forEach { source ->
            SeedsDb.DetailedSeed.Table.insertAndGetId {
                SeedsDb.DetailedSeed.insert(it, source)
            }
        }
    }
}
