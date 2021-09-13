package generated.model.db

import generated.model.Flat
import generated.model.FlatDto
import kotlin.Boolean
import kotlin.Double
import kotlin.Int
import kotlin.Long
import kotlin.String
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

object FlatDb {
  object A {
    fun create(source: ResultRow) = FlatDto.A(source[Table.id].value, source[Table.boolean],
        source[Table.int], source[Table.long], source[Table.double], source[Table.string])
    fun fetchAll() = transaction { with (Table) { selectAll().map { create(it) } } }
    object Table : IntIdTable("A") {
      val boolean: Column<Boolean> = bool("boolean")

      val int: Column<Int> = integer("int")

      val long: Column<Long> = long("long")

      val double: Column<Double> = double("double")

      val string: Column<String> = text("string")
    }

    class Entity(
      id: EntityID<Int>
    ) : IntEntity(id) {
      var boolean: Boolean by Table.boolean

      var int: Int by Table.int

      var long: Long by Table.long

      var double: Double by Table.double

      var string: String by Table.string

      companion object : IntEntityClass<Entity>(Table)
    }
  }
}
