## Atm implementation in Kolin

This project was forked from an open source template projects I wrote this year.

# History
I wrote this using a template project I have been working on. The history of that can be traced through these repos oldest to newest:
  https://github.com/leonhardbrenner/kitchensink - this was just getting some technologies integrated into a single stack in particular I was interested in using KotlinPoet to generate boilerplate for my types. Stuff like table(drop, create, load), Interfaces, Dtos, Daos, ...

https://github.com/leonhardbrenner/stonesoup - this was the first public project to fork kitchensink it builds some tables and trees and uses them in a FE written in Kotlin as well.

https://github.com/leonhardbrenner/grow - this forked stonesoup to remove all of the application code
Finally, this repo is forked from grow.

# Solution starting from my template project:

To begin with I define my types like this
https://github.com/leonhardbrenner/atm/blob/main/buildSrc/src/main/kotlin/models/Atm.kt

From the commandline I run:
  ./gradlew generate
  
This generates the following code for me:
  https://github.com/leonhardbrenner/atm/blob/main/src/commonMain/kotlin/generated/model/Atm.kt
  https://github.com/leonhardbrenner/atm/blob/main/src/commonMain/kotlin/generated/model/AtmDto.kt
  https://github.com/leonhardbrenner/atm/blob/main/src/jvmMain/kotlin/generated/model/db/AtmDb.kt
  https://github.com/leonhardbrenner/atm/blob/main/src/jvmMain/kotlin/generated/dao/AtmDao.kt

These classes represent the interfaces, dto, db code specific to my types, and daos with basic crud that are open to extending. I have generators that build the routes and client api for react Kotlin in the StoneSoup project as well.

I will stop boring you now and get to my implementation:

Implementation https://github.com/leonhardbrenner/atm/blob/main/src/jvmMain/kotlin/services/AtmService.kt from the bottom up:
  AtmSession provides a CLI
  AtmService coordinates authorization, ledger services and transaction dao.
  TransactionService records and reports history our transactions.
  LedgerService is where withdraw and deposit calculate margins and make an entry in the ledger and transaction tables within a transaction. **
  AuthenticationService - coordinates authorizationPin and authorizationToken daos

Tests: https://github.com/leonhardbrenner/atm/blob/main/src/jvmTest/kotlin/services/AtmServiceTest.kt
   The test are organized in the same order as the AtmService's classes and methods and use nomanclature <Service> - <method> - <sub test>

To run this from command line:
  ./gradlew run services.AtmServiceKt *
  
 *I am not sure this is correct. I ran the main for that class in IntelliJ and relied mostly on service tests
**There are several bugs in LedgerService.withdraw atm I will fix those but wanted to send my general design out.

  
