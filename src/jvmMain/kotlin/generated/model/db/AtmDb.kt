package generated.model.db

import generated.model.Atm
import generated.model.Atm.AuthorizationPin
import generated.model.Atm.AuthorizationToken
import generated.model.Atm.Ledger
import generated.model.Atm.Machine
import generated.model.Atm.Transaction
import generated.model.AtmDto
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
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.statements.UpdateStatement
import org.jetbrains.exposed.sql.transactions.transaction

object AtmDb {
  object AuthorizationPin {
    fun select(source: ResultRow) = AtmDto.AuthorizationPin(source[Table.id].value,
        source[Table.accountId], source[Table.pin])
    fun insert(it: InsertStatement<EntityID<Int>>, source: Atm.AuthorizationPin) {
      it[Table.accountId] = source.accountId
      it[Table.pin] = source.pin
    }

    fun update(it: UpdateStatement, source: Atm.AuthorizationPin) {
      it[Table.accountId] = source.accountId
      it[Table.pin] = source.pin
    }

    object Table : IntIdTable("AuthorizationPin") {
      val accountId: Column<String> = text("accountId")

      val pin: Column<String> = text("pin")
    }

    class Entity(
      id: EntityID<Int>
    ) : IntEntity(id) {
      var accountId: String by Table.accountId

      var pin: String by Table.pin

      companion object : IntEntityClass<Entity>(Table)
    }
  }

  object AuthorizationToken {
    fun select(source: ResultRow) = AtmDto.AuthorizationToken(source[Table.id].value,
        source[Table.accountId], source[Table.token], source[Table.expiration])
    fun insert(it: InsertStatement<EntityID<Int>>, source: Atm.AuthorizationToken) {
      it[Table.accountId] = source.accountId
      it[Table.token] = source.token
      it[Table.expiration] = source.expiration
    }

    fun update(it: UpdateStatement, source: Atm.AuthorizationToken) {
      it[Table.accountId] = source.accountId
      it[Table.token] = source.token
      it[Table.expiration] = source.expiration
    }

    object Table : IntIdTable("AuthorizationToken") {
      val accountId: Column<String> = text("accountId")

      val token: Column<String> = text("token")

      val expiration: Column<Long> = long("expiration")
    }

    class Entity(
      id: EntityID<Int>
    ) : IntEntity(id) {
      var accountId: String by Table.accountId

      var token: String by Table.token

      var expiration: Long by Table.expiration

      companion object : IntEntityClass<Entity>(Table)
    }
  }

  object Ledger {
    fun select(source: ResultRow) = AtmDto.Ledger(source[Table.id].value, source[Table.accountId],
        source[Table.balance])
    fun insert(it: InsertStatement<EntityID<Int>>, source: Atm.Ledger) {
      it[Table.accountId] = source.accountId
      it[Table.balance] = source.balance
    }

    fun update(it: UpdateStatement, source: Atm.Ledger) {
      it[Table.accountId] = source.accountId
      it[Table.balance] = source.balance
    }

    object Table : IntIdTable("Ledger") {
      val accountId: Column<String> = text("accountId")

      val balance: Column<Double> = double("balance")
    }

    class Entity(
      id: EntityID<Int>
    ) : IntEntity(id) {
      var accountId: String by Table.accountId

      var balance: Double by Table.balance

      companion object : IntEntityClass<Entity>(Table)
    }
  }

  object Machine {
    fun select(source: ResultRow) = AtmDto.Machine(source[Table.id].value,
        source[Table.serialNumber], source[Table.balance])
    fun insert(it: InsertStatement<EntityID<Int>>, source: Atm.Machine) {
      it[Table.serialNumber] = source.serialNumber
      it[Table.balance] = source.balance
    }

    fun update(it: UpdateStatement, source: Atm.Machine) {
      it[Table.serialNumber] = source.serialNumber
      it[Table.balance] = source.balance
    }

    object Table : IntIdTable("Machine") {
      val serialNumber: Column<String> = text("serialNumber")

      val balance: Column<Double> = double("balance")
    }

    class Entity(
      id: EntityID<Int>
    ) : IntEntity(id) {
      var serialNumber: String by Table.serialNumber

      var balance: Double by Table.balance

      companion object : IntEntityClass<Entity>(Table)
    }
  }

  object Transaction {
    fun select(source: ResultRow) = AtmDto.Transaction(source[Table.id].value,
        source[Table.accountId], source[Table.timestamp], source[Table.amount],
        source[Table.balance])
    fun insert(it: InsertStatement<EntityID<Int>>, source: Atm.Transaction) {
      it[Table.accountId] = source.accountId
      it[Table.timestamp] = source.timestamp
      it[Table.amount] = source.amount
      it[Table.balance] = source.balance
    }

    fun update(it: UpdateStatement, source: Atm.Transaction) {
      it[Table.accountId] = source.accountId
      it[Table.timestamp] = source.timestamp
      it[Table.amount] = source.amount
      it[Table.balance] = source.balance
    }

    object Table : IntIdTable("Transaction") {
      val accountId: Column<String> = text("accountId")

      val timestamp: Column<Long> = long("timestamp")

      val amount: Column<Double> = double("amount")

      val balance: Column<Double> = double("balance")
    }

    class Entity(
      id: EntityID<Int>
    ) : IntEntity(id) {
      var accountId: String by Table.accountId

      var timestamp: Long by Table.timestamp

      var amount: Double by Table.amount

      var balance: Double by Table.balance

      companion object : IntEntityClass<Entity>(Table)
    }
  }
}
