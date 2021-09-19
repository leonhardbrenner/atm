package applications

import com.google.inject.AbstractModule
import generated.routers.AtmRouter
import javax.inject.Inject
import io.ktor.routing.*

class CoreApplication @Inject constructor(
    val atmRouter: AtmRouter,
    ) {

    fun routesFrom(routing: Routing) {
        atmRouter.routes(routing)
    }

    object Module : AbstractModule() {

        override fun configure() {
            //Stonesoup PR 10 removed the rest database and all MongoDB wiring.
            //bind(CoroutineDatabase::class.java).toInstance(database())
        }

    }

}
