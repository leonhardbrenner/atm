package generators

import com.squareup.kotlinpoet.*
import java.io.File
import schema.Manifest

/*
Interesting find - This shows how to make properties in functions and how those parameters are templated.
    https://stackoverflow.com/questions/50077695/how-should-i-add-property-within-a-function-in-kotlinpoet
    https://github.com/square/kotlinpoet/blob/1262b3a512c276fb2ffee39a7830c1307440df4b/src/main/java/com/squareup/kotlinpoet/CodeBlock.kt#L30
*/
object DaoGenerator: Generator {

    override fun generate(namespace: Manifest.Namespace) {
        val typeSpec = TypeSpec.classBuilder("${namespace.name}Dao").apply {
            namespace.types.forEach { type ->
                generateType(type)
                //addProperty(
                //    PropertySpec.builder(
                //        type.name.decapitalize(),
                //        ClassName("", type.dotPath("Dao"))
                //    )
                //        .build()
                //)
            }
        }
        val file = FileSpec.builder("generated.dao", "${namespace.name}Dao")
            .addImport("generated.model", "${namespace.name}")
            .addImport("generated.model.db", "${namespace.name}Db")
            .addImport("org.jetbrains.exposed.sql",
                "deleteWhere", "insertAndGetId", "select", "selectAll", "update")
            .apply {
                addType(typeSpec.build())
            }.build()
        val writer = File("$path/jvmMain/kotlin")
        file.writeTo(writer)
    }

    fun TypeSpec.Builder.generateType(type: Manifest.Namespace.Type): TypeSpec.Builder
    = addType(
        TypeSpec.classBuilder(type.name).apply {
            addFunction(type.index)
            addFunction(type.get)
            addFunction(type.create)
            addFunction(type.update)
            addFunction(type.destroy)
        }.build()
    )

    val Manifest.Namespace.Type.index get() = FunSpec
        .builder("index")
        .addCode(
            """
            return ${dotPath("Db")}.Table.selectAll().map {
               ${dotPath("Db")}.select(it)
            }
            """.trimIndent()
        )
        .build()

    val Manifest.Namespace.Type.get get() = FunSpec
        .builder("get")
        .addParameter("id", Int::class)
        .addCode(
            """
            return ${dotPath("Db")}.Table.select { ${dotPath("Db")}.Table.id.eq(id) }.map {
                ${dotPath("Db")}.select(it)
            }.last()
            """.trimIndent()
        )
        .build()

    val Manifest.Namespace.Type.create get() = FunSpec
        .builder("create")
        .addParameter("source", ClassName(namespace.name, name))
        .addCode(
            """
            return ${dotPath("Db")}.Table.insertAndGetId {
                ${dotPath("Db")}.insert(it, source)
            }.value
            """.trimIndent()
        )
        .build()

    val Manifest.Namespace.Type.update get() = FunSpec
        .builder("update")
        .addParameter("source", ClassName(namespace.name, name))
        .addCode(
            """
            return ${dotPath("Db")}.Table.update({ ${dotPath("Db")}.Table.id.eq(source.id) }) {
                ${dotPath("Db")}.update(it, source)
            }
            """.trimIndent()
        )
        .build()

    val Manifest.Namespace.Type.destroy get() = FunSpec
        .builder("destroy")
        .addParameter("id", Int::class)
        .addCode(
            """
            return ${dotPath("Db")}.Table.deleteWhere { ${dotPath("Db")}.Table.id eq id }        
            """.trimIndent()
        )
        .build()

}
