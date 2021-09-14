package generated.dao

import generated.model.Atm
import generated.model.db.AtmDb
import kotlin.Int
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update

class AtmDao {
  class AuthorizationPin {
    fun index() = AtmDb.AuthorizationPin.Table.selectAll().map {
       AtmDb.AuthorizationPin.select(it)
    }
    fun get(id: Int) = AtmDb.AuthorizationPin.Table.select { AtmDb.AuthorizationPin.Table.id.eq(id)
        }.map {
        AtmDb.AuthorizationPin.select(it)
    }.last()
    fun create(source: Atm.AuthorizationPin) = AtmDb.AuthorizationPin.Table.insertAndGetId {
        AtmDb.AuthorizationPin.insert(it, source)
    }.value
    fun update(source: Atm.AuthorizationPin) = AtmDb.AuthorizationPin.Table.update({
        AtmDb.AuthorizationPin.Table.id.eq(source.id) }) {
        AtmDb.AuthorizationPin.update(it, source)
    }
    fun destroy(id: Int) = AtmDb.AuthorizationPin.Table.deleteWhere {
        AtmDb.AuthorizationPin.Table.id eq id }        }

  class AuthorizationToken {
    fun index() = AtmDb.AuthorizationToken.Table.selectAll().map {
       AtmDb.AuthorizationToken.select(it)
    }
    fun get(id: Int) = AtmDb.AuthorizationToken.Table.select {
        AtmDb.AuthorizationToken.Table.id.eq(id) }.map {
        AtmDb.AuthorizationToken.select(it)
    }.last()
    fun create(source: Atm.AuthorizationToken) = AtmDb.AuthorizationToken.Table.insertAndGetId {
        AtmDb.AuthorizationToken.insert(it, source)
    }.value
    fun update(source: Atm.AuthorizationToken) = AtmDb.AuthorizationToken.Table.update({
        AtmDb.AuthorizationToken.Table.id.eq(source.id) }) {
        AtmDb.AuthorizationToken.update(it, source)
    }
    fun destroy(id: Int) = AtmDb.AuthorizationToken.Table.deleteWhere {
        AtmDb.AuthorizationToken.Table.id eq id }        }

  class Ledger {
    fun index() = AtmDb.Ledger.Table.selectAll().map {
       AtmDb.Ledger.select(it)
    }
    fun get(id: Int) = AtmDb.Ledger.Table.select { AtmDb.Ledger.Table.id.eq(id) }.map {
        AtmDb.Ledger.select(it)
    }.last()
    fun create(source: Atm.Ledger) = AtmDb.Ledger.Table.insertAndGetId {
        AtmDb.Ledger.insert(it, source)
    }.value
    fun update(source: Atm.Ledger) = AtmDb.Ledger.Table.update({ AtmDb.Ledger.Table.id.eq(source.id)
        }) {
        AtmDb.Ledger.update(it, source)
    }
    fun destroy(id: Int) = AtmDb.Ledger.Table.deleteWhere { AtmDb.Ledger.Table.id eq id }        }
}
