package services

import model.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*
import javax.inject.Inject
import services.atm.*

class AtmService @Inject constructor(
    val authorizationService: AuthorizationService,
    val ledgerService: LedgerService,
    val transactionDao: TransactionDao
) {

    fun authorize(accountId: AccountId, pin: Pin): Response =
        try {
            Response(token = authorizationService.verifyPin(accountId, pin))
        } catch (ex: Exception) {
            Response(authorizationError = ex.message!!)
        }

    fun logout(accountId: AccountId, token: Token) = transaction {
        authorizationService.endSession(accountId, token)
    }

    fun balance(accountId: AccountId, token: Token): Response =
        try {
            authorizationService.verifyToken(accountId, token)
            ledgerService.balance(accountId)
        } catch (ex: Exception) {
            Response(authorizationError = ex.message!!)
        }

    fun withdraw(accountId: AccountId, token: Token, amount: Amount) =
        try {
            authorizationService.verifyToken(accountId, token)
            ledgerService.withdraw(accountId, amount)
        } catch (ex: Exception) {
            Response(authorizationError = ex.message!!)
        }


    fun deposit(accountId: AccountId, token: Token, amount: Amount) =
        try {
            authorizationService.verifyToken(accountId, token)
            ledgerService.deposit(accountId, amount)
        } catch (ex: Exception) {
            Response(authorizationError = ex.message!!)
        }


    fun history(accountId: AccountId, token: Token) =
        try {
            authorizationService.verifyToken(accountId, token)
            transaction { //Todo - Move to a service
                Response(
                    history = transactionDao.getByAccountId(accountId).map {
                        Transaction(it, formatTimestamp(it.timestamp))
                    }
                )
            }
        } catch (ex: Exception) {
            Response(authorizationError = ex.message!!)
        }

}

fun formatTimestamp(timestamp: Long) = with(Calendar.getInstance(Locale.ENGLISH)) {
    setTimeInMillis(timestamp)
    "%04d-%02d-%02d %02d:%02d:%02d".format(
        get(Calendar.YEAR), get(Calendar.MONTH), get(Calendar.DAY_OF_MONTH),
        get(Calendar.HOUR), get(Calendar.MINUTE), get(Calendar.SECOND)
    )
}

