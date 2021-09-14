## Atm implementation in Kolin

This project was forked from an open source template projects I wrote this year.

# Implementation

https://github.com/leonhardbrenner/atm/blob/main/src/jvmMain/kotlin/services/AtmService.kt from the bottom up:

  AtmSession provides a CLI
  
  AtmService coordinates authorization, ledger services and transaction dao.
  
  TransactionService records and reports history our transactions.
  
  LedgerService is where withdraw and deposit calculate margins and make an entry in the ledger and transaction tables within a transaction. **
  
  AuthenticationService - coordinates authorizationPin and authorizationToken daos

# Tests:

https://github.com/leonhardbrenner/atm/blob/main/src/jvmTest/kotlin/services/AtmServiceTest.kt

  The test are organized in the same order as the AtmService's classes and methods and use nomanclature <Service> - <method> - <sub test>

To run this from command line *
  ./gradlew run services.AtmServiceKt
  Note: I am using a postgres and have config. You will need these environment variable:
    DB_URL=jdbc:postgresql://localhost/test;DB_USER=test;DB_PASSWORD=test
  
# Solution starting from my template project:

As I mentioned I used a template project to write this. This is a summary of those steps.

  First, I forked:
    https://github.com/leonhardbrenner/grow
  
  Then I define my types:  
    https://github.com/leonhardbrenner/atm/blob/main/buildSrc/src/main/kotlin/models/Atm.kt

From the commandline I run:
  ./gradlew generate
  
This generates the following code for me:

  https://github.com/leonhardbrenner/atm/blob/main/src/commonMain/kotlin/generated/model/Atm.kt
  https://github.com/leonhardbrenner/atm/blob/main/src/commonMain/kotlin/generated/model/AtmDto.kt
  https://github.com/leonhardbrenner/atm/blob/main/src/jvmMain/kotlin/generated/model/db/AtmDb.kt
  https://github.com/leonhardbrenner/atm/blob/main/src/jvmMain/kotlin/generated/dao/AtmDao.kt

These classes represent the interfaces, dto, db code specific to my types, and daos with basic crud that are open to extending. I have generators that build the routes and client api for react Kotlin in the StoneSoup project as well.

# History
  
  I wrote this using a template project I have been working on.

  The history of that can be traced through these repos oldest to newest:
  
  https://github.com/leonhardbrenner/kitchensink - this was just getting some technologies integrated into a single stack in particular I was interested in using KotlinPoet to generate boilerplate for my types. Stuff like table(drop, create, load), Interfaces, Dtos, Daos, ...

  https://github.com/leonhardbrenner/stonesoup - this was the first public project to fork kitchensink it builds some tables and trees and uses them in a FE written in Kotlin as well.

  https://github.com/leonhardbrenner/grow - this forked stonesoup to remove all of the application code

Finally, this repo is forked from grow.

 *I am not sure this is correct. I ran the main for that class in IntelliJ and relied mostly on service tests
**There are several bugs in LedgerService.withdraw atm I will fix those but wanted to send my general design out.

 # Sticky points:
  To test the services I had to integrate mockito-kotlin which was failing till I found this:
  https://github.com/leonhardbrenner/atm/pull/3/commits/53e270a0b57c78e680d7b379a38b3ae43d269ab9#diff-9f49e963254a066f3fed7ea9bea122e4999e1af6079b129f1741093295ed1430
  in this commit:
  https://github.com/leonhardbrenner/atm/pull/3/commits/53e270a0b57c78e680d7b379a38b3ae43d269ab9
  It turns out that flag let's you use mockito to mock closed classes:(
