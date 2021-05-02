import org.gradle.kotlin.dsl.DependencyHandlerScope

fun DependencyHandlerScope.defaultAndroidTestDependencies() {
    "androidTestImplementation"(Dependencies.Espresso.core)
    "androidTestImplementation"(Dependencies.Espresso.contrib)
    "androidTestImplementation"(Dependencies.AndroidTest.core)
    "androidTestImplementation"(Dependencies.AndroidTest.coreKtx)
    "androidTestImplementation"(Dependencies.AndroidTest.extJunit)
    "androidTestImplementation"(Dependencies.AndroidTest.runner)
    "androidTestImplementation"(Dependencies.AndroidTest.rules)
}