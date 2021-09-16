## Atm implementation in Kolin

# Implementation

https://github.com/leonhardbrenner/atm/blob/main/src/jvmMain/kotlin/services/AtmService.kt from the bottom up:

  AtmSession provides a CLI
  
  AtmService coordinates authorization, ledger services and transaction dao.
  
  TransactionService records and reports history our transactions.
  
  LedgerService is where withdraw and deposit calculate margins and make an entry in the ledger and transaction tables within a transaction. **
  
  AuthenticationService - coordinates authorizationPin and authorizationToken daos
  
  For an explanation of how the boilerplate is created see section 'Solution starting from my template project'

# Tests:

  https://github.com/leonhardbrenner/atm/blob/main/src/jvmTest/kotlin/services/AtmServiceTest.kt

  The test are organized in the same order as the AtmService's classes and methods and use nomanclature: `<Service> - <method> - <sub test>`

# Running
  Note: you will need a postgres database in my case I use test/test@test.
  Note: not positive this is correct I run from IntelliJ there is a little green arrow next to main at the bottom of AtmServiceKt.
  Note: I am not reading from stdin I just feed it from a list of strings for now.
  
    ./gradlew run services.AtmServiceKt
  
  Note: You will get an error and need these environment variable.
  
    DB_URL=jdbc:postgresql://localhost/test;DB_USER=test;DB_PASSWORD=test

### Note: this is not fully implemented there are a list of follow up PRs key points they are fixing:

  https://github.com/leonhardbrenner/atm/pull/4 - now merged and these bullets are handled.
  
  add machine so ledger service can know how much money is in the machine I am assuming the ATM keeps the machine ledger centralized so it can know the balance in case of theft.

  return and handle messages and exceptions to be dispayed. Probably just a toString on a data class.
    
  implement the withdraw logic in the LedgerService
  
  https://github.com/leonhardbrenner/atm/pull/5 - not created yet
  
  last pr handled the messaging by returning a receipts now I need to do this for deposit and such.
  
  last pr implemented the withraw logic and stubbed the test. I will fill in the tests here.


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

This is how generate all of that boiler plate:
  https://github.com/leonhardbrenner/atm/blob/75fc4cfc4efc4696f5075f13adbf712da32cbb2f/build.gradle.kts#L152
  https://github.com/leonhardbrenner/atm/blob/main/buildSrc/src/main/kotlin/ModelGenerator.kt

All of the generators are variations of this:

  https://github.com/leonhardbrenner/atm/blob/main/buildSrc/src/main/kotlin/generators/InterfaceGenerator.kt
  
My dtos and other presentations of the data implement the interface allowing me to supply any adapter that implements the type to a Dto.create. Another nice trick is I can use 'by' for my resources like this:

  https://github.com/leonhardbrenner/stonesoup/blob/19273e2b94c3b6e7f5ad7266f1bf450233e33d65/src/commonMain/kotlin/models/SeedsResource.kt#L10

Finally, I can write my services. In this case I have extended some of my Daos:

  https://github.com/leonhardbrenner/atm/blob/main/src/jvmTest/kotlin/services/AtmServiceTest.kt
  https://github.com/leonhardbrenner/atm/blob/main/src/jvmMain/kotlin/services/AtmService.kt

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
  
  Solution found here:
  https://antonioleiva.com/mockito-2-kotlin
