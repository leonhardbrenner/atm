package generators.dsl

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec

import java.io.File
import schema.ManifestDsl

object BuilderGeneratorDsl: GeneratorDsl {

    override fun generate(namespace: ManifestDsl.Namespace) {
        val file = FileSpec.builder("generated.model", "${namespace.name}Builder")
            .addType(
                TypeSpec.interfaceBuilder("${namespace.name}Builder").apply {
                    namespace.complexTypes.values.forEach { type ->
                        val typeSpec = TypeSpec.classBuilder(type.name)
                            .primaryConstructor(
                                FunSpec.constructorBuilder().apply {
                                    type.elements.values.forEach { element ->
                                        addParameter(element.name, element.type.typeName.copy(nullable = true)).build()
                                    }
                                    type.links.values.forEach { link ->
                                        addParameter(
                                            link.name,
                                            link.type.typeName.copy(nullable = true)
                                            //ClassName("generated.model", link.type.dotPath("Dto"))
                                            //    .copy(nullable = true)
                                        ).build()
                                    }
                                }.build()
                            )
                            .apply {
                                type.elements.values.forEach { element ->
                                    addProperty(
                                        //TODO - make extension function
                                        PropertySpec.builder(
                                            element.name,
                                            element.type.typeName.copy(nullable = true)
                                        ).mutable(true)
                                            .initializer(element.name).build()
                                    )
                                }
                                type.links.values.forEach { link ->
                                    addProperty(
                                        //TODO - make extension function
                                        PropertySpec.builder(
                                            link.name,
                                            link.type.typeName.copy(nullable = true)
                                        ).mutable(true)
                                            .initializer(link.name).build()
                                    )
                                }
                            }
                            .addFunction( //Make this a lambda receiver
                                FunSpec.builder("build")
                                    .returns(ClassName("generated.model", "${type.packageName}.${type.name}"))
                                    .addCode(
                                        "return %L(\n%L\n)",
                                        "${type.packageName}Dto.${type.name}",
                                        (type.elements.values.map { if (it.nullable) it.name else "${it.name} ?: throw IllegalArgumentException(\"${it.name} is not nullable\")" }
                                                + type.links.values.map { "${it.name}?.let { ${it.type.dotPath("Dto")}.create(it) }" })
                                            .joinToString(",\n")
                                    )
                                    .build()
                            )

                            .build()
                        addType(typeSpec)
                    }
                }.build()
            ).build()
        val writer = File("$path/commonMain/kotlin")
        file.writeTo(writer)
    }

}
