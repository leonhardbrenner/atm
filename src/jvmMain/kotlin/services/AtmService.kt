package services
//
//import com.authzee.kotlinguice4.getInstance
//import com.google.inject.AbstractModule
//import com.google.inject.Guice
//import generated.dao.AtmDao
//import org.jetbrains.exposed.sql.transactions.transaction
//import javax.inject.Inject
//
//typealias AccountId = String
//typealias Amount = Double
//typealias Pin = String
//typealias Token = String
//
//class AuthorizationService @Inject constructor(
//    val authorizationPin: AtmDao.AuthorizationPin,
//    val authorizationToken: AtmDao.AuthorizationToken
//) {
//    /**
//     * This should lookup the account hashed_pin and compare against hash(pin) and return a token
//     */
//    fun verifyPin(accountId: AccountId, pin: Pin): Token = "4321"
//
//    /**
//     * This should lookup the account token and make sure that it is not expired.
//     */
//    fun verifyToken(token: Token): AccountId = "AccountId"
//
//    /**
//     * This should lookup the account token and make sure that it is not expired.
//     */
//    fun endSession(token: Token) = true
//}
//
//data class WithdrawResponse(val amountDispensed: Amount, val currentBalance: Amount)
//data class DepositResponse(val currentBalance: Amount)
//
//class LedgerService @Inject constructor(val ledgerDao: AtmDao.Ledger) {
//
//    inner class Account(val accountId: AccountId) {
//
//        fun withdraw(amount: Amount): WithdrawResponse {
//            if (amount > balance)
//                throw Exception("")
//            return WithdrawResponse(40.00, 289.33)
//        }
//
//        fun deposit(amount: Amount): DepositResponse {
//            //Todo - first perform the transaction
//            transaction {
//                //ledgerDao
//            }
//            return DepositResponse(balance + amount)
//        }
//
//        val balance get(): Double = transaction { ledgerDao.get(2).balance }
//    }
//}
//
//class Atm @Inject constructor(
//    val authorizationService: AuthorizationService,
//    val ledgerService: LedgerService
//) {
//
//    object Module : AbstractModule() {
//        override fun configure() {
//            //bind(CoroutineDatabase::class.java).toInstance(database())
//        }
//
//    }
//
//    data class Result(val message: String, val amount: Amount)
//
//    fun login(accountId: AccountId, pin: Pin) =
//        authorizationService.verifyPin(accountId, pin)
//
//    /**
//     * Removes value from the authorized account. The machine only contains $20 bills, so the withdrawal amount must be a multiple of 20.
//     * withdraw <value>
//     *
//     * If account has not been overdrawn, returns balance after withdrawal in the format:
//     *      Amount dispensed: $<x>
//     *      Current balance: <balance>
//     *
//     * If the account has been overdrawn with this transaction, removes a further $5 from their account, and returns:
//     *      Amount dispensed: $<x>
//     *      You have been charged an overdraft fee of $5. Current balance: <balance>
//     *
//     * The machine can’t dispense more money than it contains. If in the above two scenarios the machine contains less money than was
//     * requested, the withdrawal amount should be adjusted to be the amount in the machine and this should be prepended to the return value:
//     *      Unable to dispense full amount requested at this time.
//     *
//     * If instead there is no money in the machine, the return value should be this and only this:
//     *      Unable to process your withdrawal at this time.
//     *
//     * If the account is already overdrawn, do not perform any checks against the available money in the machine, do not process the withdrawal,
//     * and return only this:
//     *      Your account is overdrawn! You may not make withdrawals at this time.
//     *
//     */
//    fun withdraw(token: Token, amount: Amount) {
//        val accountId = authorizationService.verifyToken(token)
//        ledgerService.Account(accountId).withdraw(amount)
//    }
//
//    /**
//     * Adds value to the authorized account. The deposited amount does not need to be a multiple of 20.
//     *      deposit <value>
//     * Returns the account’s balance after deposit is made in the format:
//     *      Current balance: <balance>
//     */
//    fun deposit(token: Token, amount: Amount) {
//        val accountId = authorizationService.verifyToken(token)
//
//    }
//
//    fun balance(token: Token): Double {
//        val accountId = authorizationService.verifyToken(token)
//        return ledgerService.Account(accountId).balance
//    }
//
//    fun history(token: Token) {
//        val accountId = authorizationService.verifyToken(token)
//
//    }
//
//    fun logout(token: Token) {
//        val accountId = authorizationService.endSession(token)
//    }
//
//}
//
//fun main(args:Array<String>) {
//    DatabaseFactory.init()
//    val atm = Guice.createInjector(Atm.Module).getInstance<Atm>()
//    val accountId = "Len's Account"
//    val pin = "4321"
//    atm.authorizationService.verifyPin(accountId, pin).let { token ->
//        println(token)
//        println(atm.balance(token))
//        atm.withdraw(token, 22.33)
//        atm.deposit(token, 200.00)
//        atm.history(token)
//        atm.logout(accountId)
//        //atm.authorizationService.verifyToken(accountId, token) //This should not be available
//
//    }
//}
