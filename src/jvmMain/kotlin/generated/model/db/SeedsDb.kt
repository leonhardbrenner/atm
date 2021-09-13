package generated.model.db

import generated.model.Seeds
import generated.model.Seeds.DetailedSeed
import generated.model.SeedsDto
import kotlin.Int
import kotlin.String
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.statements.UpdateStatement
import org.jetbrains.exposed.sql.transactions.transaction

object SeedsDb {
  object DetailedSeed {
    fun select(source: ResultRow) = SeedsDto.DetailedSeed(source[Table.id].value,
        source[Table.companyId], source[Table.seedId], source[Table.name], source[Table.maturity],
        source[Table.secondaryName], source[Table.description], source[Table.image],
        source[Table.link])
    fun insert(it: InsertStatement<EntityID<Int>>, source: Seeds.DetailedSeed) {
      it[Table.companyId] = source.companyId
      it[Table.seedId] = source.seedId
      it[Table.name] = source.name
      it[Table.maturity] = source.maturity
      it[Table.secondaryName] = source.secondaryName
      it[Table.description] = source.description
      it[Table.image] = source.image
      it[Table.link] = source.link
    }

    fun update(it: UpdateStatement, source: Seeds.DetailedSeed) {
      it[Table.companyId] = source.companyId
      it[Table.seedId] = source.seedId
      it[Table.name] = source.name
      it[Table.maturity] = source.maturity
      it[Table.secondaryName] = source.secondaryName
      it[Table.description] = source.description
      it[Table.image] = source.image
      it[Table.link] = source.link
    }

    object Table : IntIdTable("DetailedSeed") {
      val companyId: Column<String> = text("companyId")

      val seedId: Column<String> = text("seedId")

      val name: Column<String> = text("name")

      val maturity: Column<String?> = text("maturity").nullable()

      val secondaryName: Column<String?> = text("secondaryName").nullable()

      val description: Column<String?> = text("description").nullable()

      val image: Column<String?> = text("image").nullable()

      val link: Column<String?> = text("link").nullable()
    }

    class Entity(
      id: EntityID<Int>
    ) : IntEntity(id) {
      var companyId: String by Table.companyId

      var seedId: String by Table.seedId

      var name: String by Table.name

      var maturity: String? by Table.maturity

      var secondaryName: String? by Table.secondaryName

      var description: String? by Table.description

      var image: String? by Table.image

      var link: String? by Table.link

      companion object : IntEntityClass<Entity>(Table)
    }
  }
}
