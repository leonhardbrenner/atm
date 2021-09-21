package services.atm

import generated.dao.AtmDao
import generated.model.AtmDto
import generated.model.db.AtmDb
import model.*
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*
import javax.inject.Inject

class AuthorizationPinDao: AtmDao.AuthorizationPin {
    fun getByAccountId(accountId: AccountId) = AtmDb.AuthorizationPin.Table.select {
        AtmDb.AuthorizationPin.Table.accountId.eq(accountId)
    }.map {
        AtmDb.AuthorizationPin.select(it)
    }.let {
        if (it.isEmpty()) throw Exception("Account not found")
        it.last()
    }
}

class AuthorizationTokenDao: AtmDao.AuthorizationToken {
    fun getByToken(token: Token) = AtmDb.AuthorizationToken.Table.select {
        AtmDb.AuthorizationToken.Table.token.eq(token)
    }.map {
        AtmDb.AuthorizationToken.select(it)
    }.let {
        if (it.isEmpty()) throw Exception("Token not found")
        it.last()
    }

    fun destroyByToken(token: Token) = AtmDb.AuthorizationToken.Table.deleteWhere {
        AtmDb.AuthorizationToken.Table.token eq token }
}

class AuthorizationService @Inject constructor(
    val authorizationPinDao: AuthorizationPinDao,
    val authorizationTokenDao: AuthorizationTokenDao
) {
    /**
     * This should lookup the account hashed_pin and compare against hash(pin) and return a token
     */
    fun verifyPin(accountId: AccountId, pin: Pin): Token = transaction {
        if (pin == authorizationPinDao.getByAccountId(accountId).pin) { //Todo - hash(pin)
            val token = createToken()
            authorizationTokenDao.create(AtmDto.AuthorizationToken(-1, accountId, token, now() + lifespan))
            token
        } else {
            throw Exception("Invalid Pin")
        }
    }

    /**
     * This should lookup the account hashed_pin and compare against hash(pin) and return a token
     */
    fun verifyToken(accountId: AccountId, token: Token): AccountId = transaction { //Todo - do this with token
        authorizationTokenDao.getByToken(token)?.let { result ->
            val now = now()
            if (now > result.expiration)
                throw Exception("Token has expired.")
            //Todo - update the expiration
            authorizationTokenDao.update(result.copy(expiration = now + lifespan))
            if (accountId != result.accountId)
                throw Exception("Authorization token does not belong to this account.")
            result.accountId
        }
    }

    /**
     * This should lookup the account token and make sure that it is not expired.
     */
    fun endSession(accountId: AccountId, token: Token) = transaction { //Todo - do this with token
        authorizationTokenDao.getByToken(token)?.let { result ->
            if (accountId != result.accountId)
                throw Exception("Authorization token does not belong to this account.")
            authorizationTokenDao.destroyByToken(token)
        }
    }
}

const val lifespan = 120000

fun createToken() = UUID.randomUUID().toString()

fun now() = System.currentTimeMillis()
