package net.perfectdreams.commands.dsl

import kotlin.reflect.KClass

// You can use reified type parameters to get a KClass for the generic type but you cannot get (as of Kotlin 1.1) a KType.
// However, you can still check to see if the generic type is nullable (nullable reified type : Kotlin) with null is T.
// https://stackoverflow.com/questions/45766885/kotlin-check-to-see-if-the-generic-parameter-is-optional-or-not
data class OptionallyNullableType(
		val clazz: KClass<*>,
		val isMarkedNullable: Boolean
)