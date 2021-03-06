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
  open interface AuthorizationPin {
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

  open interface AuthorizationToken {
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

  open interface Ledger {
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

  open interface Machine {
    fun index() = AtmDb.Machine.Table.selectAll().map {
       AtmDb.Machine.select(it)
    }
    fun get(id: Int) = AtmDb.Machine.Table.select { AtmDb.Machine.Table.id.eq(id) }.map {
        AtmDb.Machine.select(it)
    }.last()
    fun create(source: Atm.Machine) = AtmDb.Machine.Table.insertAndGetId {
        AtmDb.Machine.insert(it, source)
    }.value
    fun update(source: Atm.Machine) = AtmDb.Machine.Table.update({
        AtmDb.Machine.Table.id.eq(source.id) }) {
        AtmDb.Machine.update(it, source)
    }
    fun destroy(id: Int) = AtmDb.Machine.Table.deleteWhere { AtmDb.Machine.Table.id eq id }        }

  open interface Transaction {
    fun index() = AtmDb.Transaction.Table.selectAll().map {
       AtmDb.Transaction.select(it)
    }
    fun get(id: Int) = AtmDb.Transaction.Table.select { AtmDb.Transaction.Table.id.eq(id) }.map {
        AtmDb.Transaction.select(it)
    }.last()
    fun create(source: Atm.Transaction) = AtmDb.Transaction.Table.insertAndGetId {
        AtmDb.Transaction.insert(it, source)
    }.value
    fun update(source: Atm.Transaction) = AtmDb.Transaction.Table.update({
        AtmDb.Transaction.Table.id.eq(source.id) }) {
        AtmDb.Transaction.update(it, source)
    }
    fun destroy(id: Int) = AtmDb.Transaction.Table.deleteWhere { AtmDb.Transaction.Table.id eq id } 
              }
}
