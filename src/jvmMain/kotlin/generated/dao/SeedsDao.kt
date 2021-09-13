package generated.dao

import generated.model.Seeds
import generated.model.db.SeedsDb
import kotlin.Int
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update

class SeedsDao {
  class DetailedSeed {
    fun index() = SeedsDb.DetailedSeed.Table.selectAll().map {
       SeedsDb.DetailedSeed.select(it)
    }
    fun get(id: Int) = SeedsDb.DetailedSeed.Table.select { SeedsDb.DetailedSeed.Table.id.eq(id)
        }.map {
        SeedsDb.DetailedSeed.select(it)
    }.last()
    fun create(source: Seeds.DetailedSeed) = SeedsDb.DetailedSeed.Table.insertAndGetId {
        SeedsDb.DetailedSeed.insert(it, source)
    }.value
    fun update(source: Seeds.DetailedSeed) = SeedsDb.DetailedSeed.Table.update({
        SeedsDb.DetailedSeed.Table.id.eq(source.id) }) {
        SeedsDb.DetailedSeed.update(it, source)
    }
    fun destroy(id: Int) = SeedsDb.DetailedSeed.Table.deleteWhere { SeedsDb.DetailedSeed.Table.id eq
        id }        }
}
