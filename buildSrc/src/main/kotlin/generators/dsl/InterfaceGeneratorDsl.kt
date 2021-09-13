package generators.dsl

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import java.io.File
import schema.ManifestDsl

object InterfaceGeneratorDsl: GeneratorDsl {

    override fun generate(namespace: ManifestDsl.Namespace) {
        val typeSpec = TypeSpec.interfaceBuilder("${namespace.name}").apply {
            namespace.complexTypes.values.forEach { type ->
                generateType(type)
            }
        }
        val file = FileSpec.builder("generated.model", "${namespace.name}").apply {
            addType(typeSpec.build())
        }.build()
        val writer = File("$path/commonMain/kotlin")
        file.writeTo(writer)
    }

    fun TypeSpec.Builder.generateType(type: ManifestDsl.Namespace.ComplexType): TypeSpec.Builder
            = addType(
        TypeSpec.interfaceBuilder(type.name).apply {
            type.elements.values.forEach { element ->
                addProperty(
                    //Note that I have moved toward elements defining nullability making it XMLSchema like. Reconsider.
                    PropertySpec.builder(element.name, element.type.typeName.copy(nullable = element.nullable) )
                        .mutable(false)
                        .build()
                )
            }
            type.links.values.forEach { link ->
                addProperty(
                    //Note that I have moved toward elements defining nullability making it XMLSchema like. Reconsider.
                    PropertySpec.builder(link.name, link.type.typeName.copy(nullable = true) )
                        .mutable(false)
                        .build()
                )
            }

            //XXX - Needed for fancy thi
            //type.types.forEach { type ->
            //    generateType(type)
            //}
        }.build()
    )
}
