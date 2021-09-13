package schema

import com.squareup.kotlinpoet.ClassName
import kotlin.reflect.KClass

/**
 * Represents column set join types.
 */
//TODO - find a nice reference for what this means.
enum class JoinType { INNER, LEFT, RIGHT, FULL, CROSS }

//https://www.restapitutorial.com/lessons/httpmethods.html
enum class Method { POST, GET, PUT, PATCH, DELETE }

class ManifestDsl(builder: ManifestDsl.() -> Unit) {

    val namespaces = linkedMapOf<String, Namespace>()

    init {
        builder()
    }

    operator fun invoke(builder: ManifestDsl.() -> Unit) {
        builder()
    }

    abstract class Type {
        abstract val name: String
        abstract val typeName: ClassName
    }

    /**
     *
     */
    inner class Namespace(val name: String, builder: Namespace.() -> Unit = {}) {
        val namespace = this
        val types = linkedMapOf<String, Type>()
        val simpleTypes = linkedMapOf<String, SimpleType>()
        val complexTypes = linkedMapOf<String, ComplexType>()
        val resources = linkedMapOf<Type, Resource>()
        operator fun get(name: String) = types[name]!!
        init {
            namespaces[name] = this
            builder()
        }
        operator fun invoke(builder: Namespace.() -> Unit) {
            builder()
        }
        override fun toString() = name

        /**
         *
         */
        inner class ComplexType(
            override val name: String,
            val isTable: Boolean = false, //Todo - flush this out
            builder: ComplexType.() -> Unit = {}
        ) : Type() {
            val parent: ComplexType? = null
            val elements = linkedMapOf<String, Element>()
            val links = linkedMapOf<String, Link>()
            init {
                types[name] = this
                complexTypes[name] = this
                builder()
            }
            operator fun invoke(builder: ComplexType.() -> Unit) {
                builder()
            }
            override val typeName get() = ClassName(namespace.name, name)

            val path: String = if (parent==null)
                "/${namespace.name}/$name"
            else
                "${parent.path}/$name"

            fun dotPath(aspect: String = ""): String = if (parent==null)
                "${namespace.name}$aspect.$name"
            else
                "${parent.dotPath(aspect)}.$name"

            val packageName = namespace.name

            /**
             *
             */
            inner class Element(
                val name: String,
                val type: Type,
                var minOccurs: Int = 1, var maxOccurs: Int = 1,
                var default: Any? = null,
                val isAttribute: Boolean = false,
                builder: Element.() -> Unit = {}
            ) {
                val nullable get() = (minOccurs == 0 && maxOccurs == 1)
                init {
                    elements[name] = this
                    builder()
                }
                operator fun invoke(builder: Element.() -> Unit) {
                    builder()
                }
                val dbName = name//.toLowerCase()
            }

            /**
             *
             */
            inner class Link(
                val name: String,
                val type: ComplexType,
                val joinType: JoinType,
                //Todo - in order to fully support the links I will need to handle the BY clause. For now I write that
                //  manually.
                builder: Link.() -> Unit = {}
            ) {
                val table = type
                init {
                    links[name] = this
                    builder()
                }
                operator fun invoke(builder: Link.() -> Unit) {
                    builder()
                }
            }

        }

        /**
         *
         */
        inner class SimpleType(
            override val name: String,
            val kClass: KClass<*>, //builtIn["int"].kClass.qualifiedName
            builder: SimpleType.() -> Unit = {}
        ) : Type() {
            init {
                types[name] = this
                simpleTypes[name] = this
                builder()
            }
            fun invoke(builder: SimpleType.() -> Unit) {
                builder()
            }
            override val typeName get() = ClassName("kotlin", kClass.simpleName.toString())

        }

        /**
         *
         */
        inner class Resource(
            val type: Type,
            builder: Resource.() -> Unit = {}
        ) {
            var method: Method? = Method.GET
            val parameters = linkedMapOf<String, Resource.Parameter>()
            var returnType: Resource.ReturnType? = null
            init {
                resources[type] = this
                builder()
            }
            operator fun invoke(builder: Resource.() -> Unit) {
                builder()
            }
            fun post(builder: Resource.() -> Unit = {}) {
                method = Method.POST
                builder()
            }
            fun get(builder: Resource.() -> Unit = {}) {
                method = Method.GET
                builder()
            }
            fun put(builder: Resource.() -> Unit = {}) {
                method = Method.PUT
                builder()
            }
            fun patch(builder: Resource.() -> Unit = {}) {
                method = Method.PATCH
                builder()
            }
            fun delete(builder: Resource.() -> Unit = {}) {
                method = Method.DELETE
                builder()
            }

            /**
             *
             */
            inner class Parameter(
                val name: String,
                val type: Type,
                var default: Any? = null,
                builder: Parameter.() -> Unit = {}
            ) {
                init {
                    parameters[name] = this
                    builder()
                }
                operator fun invoke(builder: Parameter.() -> Unit) {
                    builder()
                }
            }

            /**
             *
             */
            inner class ReturnType(
                val type: Type,
                builder: ReturnType.() -> Unit = {}
            ) {
                init {
                    returnType = this
                    builder()
                }
                operator fun invoke(builder: ReturnType.() -> Unit) {
                    builder()
                }
            }
        }
    }

}
