plugins{
    `kotlin-dsl`
}

//https://docs.gradle.org/6.3/userguide/kotlin_dsl.html#sec:kotlin-dsl_plugin
kotlinDslPluginOptions {
    experimentalWarning.set(false)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("com.squareup:kotlinpoet:1.5.0")
    implementation(kotlin("test-junit"))
}
