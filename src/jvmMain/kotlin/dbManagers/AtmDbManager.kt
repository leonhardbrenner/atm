package dbManagers

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import generated.model.db.AtmDb
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import jsonLoaders.AtmJsonLoaders
import org.jetbrains.exposed.sql.insertAndGetId
import java.io.File

object AtmDBManager {
    val kMapper = ObjectMapper().registerModule(KotlinModule())

    fun drop() = transaction {
        SchemaUtils.drop(AtmDb.AuthorizationPin.Table)
        SchemaUtils.drop(AtmDb.AuthorizationToken.Table)
        SchemaUtils.drop(AtmDb.Ledger.Table)
        SchemaUtils.drop(AtmDb.Transaction.Table)
        SchemaUtils.drop(AtmDb.Machine.Table)
    }
    fun create() = transaction {
        SchemaUtils.create(AtmDb.AuthorizationPin.Table)
        SchemaUtils.create(AtmDb.AuthorizationToken.Table)
        SchemaUtils.create(AtmDb.Ledger.Table)
        SchemaUtils.create(AtmDb.Transaction.Table)
        SchemaUtils.create(AtmDb.Machine.Table)
    }
    fun populate() = transaction {
        val jsonLoaders = AtmJsonLoaders(kMapper)
        jsonLoaders.authorizationPin.forEach { source ->
            AtmDb.AuthorizationPin.Table.insertAndGetId {
                AtmDb.AuthorizationPin.insert(it, source)
            }
        }
        jsonLoaders.ledger.forEach { source ->
            AtmDb.Ledger.Table.insertAndGetId {
                AtmDb.Ledger.insert(it, source)
            }
        }
        jsonLoaders.machine.forEach { source ->
            AtmDb.Machine.Table.insertAndGetId {
                AtmDb.Machine.insert(it, source)
            }
        }
    }
}
