package generators.dsl

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec

import schema.ManifestDsl
import java.io.File

//XXX - when you get back deal with this:
//-  ) : Seeds.DetailedSeed {
//+  ) : generated.model.DetailedSeed {
object DtoGeneratorDsl: GeneratorDsl {

    //TODO: Generate the top level elements as resources.
    override fun generate(namespace: ManifestDsl.Namespace) {
        val file = FileSpec.builder("generated.model", "${namespace.name}Dto")
            .addType(
                TypeSpec.interfaceBuilder("${namespace.name}Dto").apply {
                    namespace.complexTypes.values.forEach { generateComplexType(it) }
                }.build()
            ).build()
        val writer = File("$path/commonMain/kotlin")
        file.writeTo(writer)
    }

    fun TypeSpec.Builder.generateComplexType(type: ManifestDsl.Namespace.ComplexType): TypeSpec.Builder = addType(
        TypeSpec.classBuilder(type.name)
            .addAnnotation(ClassName("kotlinx.serialization", "Serializable"))
            .addModifiers(KModifier.DATA)
            .addSuperinterface(type.typeName)
            .primaryConstructor(
                FunSpec.constructorBuilder().apply {
                    type.elements.values.forEach { element ->
                        addParameter(element.name, element.type.typeName.copy(nullable = element.nullable))
                            .build()
                    }
                    type.links.values.forEach { link ->
                        addParameter(
                            link.name,
                            ClassName("generated.model", link.type.dotPath("Dto"))
                                .copy(nullable = true)
                        ).build()
                    }
                }.build()
            )
            .apply {
                type.elements.values.forEach { element ->
                    addProperty(
                        PropertySpec.builder(element.name, element.type.typeName.copy(nullable = element.nullable))
                            .addModifiers(listOf(KModifier.OVERRIDE))
                            .mutable(false)
                            .initializer(element.name)
                            .build()
                    )
                }
                type.links.values.forEach { link ->
                    addProperty(
                        PropertySpec.builder(
                            link.name,
                            ClassName("generated.model", link.type.dotPath("Dto"))
                                .copy(nullable = true)
                        )
                            .addModifiers(listOf(KModifier.OVERRIDE))
                            .mutable(false)
                            .initializer(link.name)
                            .build()
                    )
                }
            }
            .addType(
                TypeSpec.companionObjectBuilder().apply {
                    addProperty(
                        PropertySpec.builder("path", String::class)
                            .addModifiers(listOf(KModifier.CONST))
                            .initializer("\"${type.path}\"")
                            .build()
                    )
                    addFunction(
                        FunSpec.builder("create")
                            .addParameter("source", type.typeName)
                            //Look at CodeBlock.addArgument and you will see L stands for literal
                            .addCode(
                                "return %T(%L)",
                                ClassName("generated.model", type.dotPath("Dto")),
                                (type.elements.values.map { "source.${it.name}" }
                                        + type.links.values.map { "source.${it.name}?.let { ${it.type.dotPath("Dto")}.create(it) }" })
                                    .joinToString(", ")
                            )
                            .build()
                    )
                }.build()
            ).apply {
                //XXX - needed for fancy types which 'add_schedule' PR introduces as an issue because we need nesting.
                //type.types.values.forEach {
                //    generateType(it)
                //}
            }.build()
    )
}
