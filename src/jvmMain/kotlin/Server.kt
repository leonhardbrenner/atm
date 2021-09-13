import applications.MobileApplication
import applications.CoreApplication
import com.authzee.kotlinguice4.getInstance
import com.google.inject.Guice
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.serialization.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
    DatabaseFactory.init()
    val coreApplication = Guice.createInjector(CoreApplication.Module)
        .getInstance<CoreApplication>()
    val mobileApplication = Guice.createInjector(MobileApplication.Module)
        .getInstance<MobileApplication>()

    val port = System.getenv("PORT")?.toInt() ?: 9090
    embeddedServer(Netty, port) {

        install(ContentNegotiation) {
            json()
        }

        install(CORS) {
            method(HttpMethod.Get)
            method(HttpMethod.Post)
            method(HttpMethod.Delete)
            anyHost()
        }

        install(Compression) {
            gzip()
        }

        routing {

            get("/") {
                call.respondText(
                    this::class.java.classLoader.getResource("index.html")!!.readText(),
                    ContentType.Text.Html
                )
            }

            static("/") {
                resources("")
            }

            coreApplication.routesFrom(this)
            mobileApplication.routesFrom(this)

        }
    }.start(wait = true)
}
