package generators

import com.squareup.kotlinpoet.*
import java.io.File
import schema.Manifest

object RouteGenerator: Generator {

    override fun generate(namespace: Manifest.Namespace) {
        val typeSpec = TypeSpec.classBuilder("${namespace.name}Routing").apply {
            namespace.types.forEach { type ->
                generateRouting(type)
            }
        }
        val file = FileSpec.builder("generated.routing", "${namespace.name}Routing").apply {
            addImport("generated.dao", "${namespace.name}Dao")
            addImport("generated.model", "${namespace.name}Dto")
            addImport("org.jetbrains.exposed.sql.transactions", "transaction")
            addImport("io.ktor.application",
                "call")
            addImport("io.ktor.response",
                "respond")
            addImport("io.ktor.http",
                "HttpStatusCode", "Parameters")
            addImport("io.ktor.routing",
                "Routing", "route", "get", "post", "put", "delete")
            addType(typeSpec.build())
        }.build()
        val writer = File("$path/jvmMain/kotlin")
        file.writeTo(writer)
    }

    fun TypeSpec.Builder.generateRouting(type: Manifest.Namespace.Type): TypeSpec.Builder = addType(
        //Todo - generate: class ScheduleRouting @Inject constructor(val dao: SeedsDao.Schedule, val service: SeedsService) {
        TypeSpec.classBuilder(type.name).apply {
            primaryConstructor(
                //https://github.com/square/kotlinpoet/issues/515
                FunSpec.constructorBuilder().addAnnotation(ClassName("javax.inject", "Inject"))
                    .addParameter("dao", ClassName("generated.dao", "${type.namespace.name}Dao", type.name))
                    .addParameter("service", ClassName("services.${type.namespace.name.decapitalize()}", "${type.name}Service"))
                .build()
            )
            addProperty(
                PropertySpec.builder(
                "dao",
                ClassName("generated.dao", "${type.namespace.name}Dao", type.name)
            ).initializer("dao").build())
            addProperty(
                PropertySpec.builder(
                "service",
                ClassName("services.${type.namespace.name.decapitalize()}", "${type.name}Service")
            ).initializer("service").build())
            addFunction(type.unmarshal)
            addFunction(type.routes)
        }.build()
    )

    val Manifest.Namespace.Type.unmarshal
        get() = FunSpec
            .builder("unmarshal")
            .addParameter(
                ParameterSpec.builder(
                    "parameters",
                    ClassName("io.ktor.http", "Parameters")
                ).build()
            )
            .addCode("return %T(%L)\n",
                ClassName("generated.model", dotPath("Dto")), //${if (it.name == "id") "?: -1" else "
                //XXX - you must rewrite this!!!
                elements.map { "\nparameters[\"${it.name}\"]${
                    if (it.type.kType.toString()=="kotlin.Int") "?.toInt()" else ""
                }${if (it.type.nullable) "" else "${if (it.name == "id") "?: -1" else " ?: throw Exception(\"BadRequest\")"}"}" }.joinToString(", ")
            )
            .build()

    //XXX - horrible hack
    val moveString = """
            |    put("/{id}/move") {
            |        val id = call.parameters["id"]?.toInt()
            |            ?: return@put call.respond(HttpStatusCode.BadRequest)
            |        val parentId = call.parameters["parentId"]?.toInt()
            |            ?: return@put call.respond(HttpStatusCode.BadRequest)
            |        val response = transaction { service.move(id, parentId) }
            |        call.respond(response)
            |    }
    """.trimIndent()

    val Manifest.Namespace.Type.routes
        get() = FunSpec
            .builder("routes")
            .addParameter(
                ParameterSpec.builder(
                    "routing",
                    ClassName("io.ktor.routing", "Routing")
                ).build()
            )
            .addCode("""
            |return routing.route(${dotPath("Dto")}.path) {
            | 
            |    get {
            |        call.respond(transaction { service.index() })
            |    }
            |    
            |    post {
            |        call.respond(
            |            try {
            |                transaction { service.create(unmarshal(call.parameters)) }
            |            } catch (ex: Exception) {
            |                return@post call.respond(HttpStatusCode.BadRequest)
            |            }
            |        )
            |    }
            |
${if (name == "Chore") generators.RouteGenerator.moveString else ""}
            |    
            |    put("/{id}") {
            |        call.respond(
            |            try {
            |                transaction { service.update(unmarshal(call.parameters)) }
            |            } catch (ex: Exception) {
            |                return@put call.respond(HttpStatusCode.BadRequest)
            |            }
            |        )
            |    }
            |    
            |    delete("/{id}") {
            |        val id = call.parameters["id"]?.toInt() ?: error("Invalid delete request")
            |        transaction {
            |            service.destroy(id)
            |        }
            |        call.respond(HttpStatusCode.OK)
            |    }
            |}
            """.trimMargin() + "\n") //Todo - short form this and use it where formatting is Icky.
            .build()

}
