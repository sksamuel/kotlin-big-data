package com.sksamuel.centurion.avro.encoders

import org.apache.avro.Schema
import java.math.BigDecimal
import kotlin.reflect.KClass
import kotlin.reflect.KType

/**
 * An [Encoder] typeclass encodes a JVM value of type T into a value suitable
 * for use with Avro.
 *
 * For example, an encoder could encode a String as an instance of [Utf8],
 * or it could encode it as an instance of [GenericFixed].
 *
 * Some encoders use the schema to determine the encoding function to return. For example, strings
 * can be encoded as [UTF8]s, [GenericFixed]s, [ByteBuffers] or [java.lang.String]s.
 * Therefore, the [Encoder<String>] typeclass instances uses the schema to select which of these
 * implementations to use.
 *
 * Other types may not require the schema at all. For example, the default [Encoder<Int>] always
 * returns a java.lang.Integer regardless of any schema input.
 */
fun interface Encoder<T> {

   companion object {

      /**
       * Returns an [Encoder] that encodes by simply returning the input value.
       */
      fun <T : Any> identity(): Encoder<T> = Encoder { _, value -> value }

      fun encoderFor(type: KType): Encoder<*> {
         val encoder: Encoder<*> = when (val classifier = type.classifier) {
            String::class -> StringEncoder
            Boolean::class -> BooleanEncoder
            Float::class -> FloatEncoder
            Double::class -> DoubleEncoder
            Int::class -> IntEncoder
            Long::class -> LongEncoder
            BigDecimal::class -> BigDecimalStringEncoder
            Set::class -> GenericArraySetEncoder(encoderFor(type.arguments.first().type!!))
            List::class -> GenericArrayListEncoder(encoderFor(type.arguments.first().type!!))
            is KClass<*> -> if (classifier.java.isEnum) EnumEncoder<Enum<*>>() else error("Unsupported type $type")
            else -> error("Unsupported type $type")
         }
         return if (type.isMarkedNullable) NullEncoder(encoder) else encoder
      }
   }

   fun encode(schema: Schema, value: T): Any?

   /**
    * Returns an [Encoder<U>] by applying a function [fn] that maps a [U]
    * to an [T], before encoding as an [T] using this encoder.
    */
   fun <U> contraMap(fn: (U) -> T): Encoder<U> {
      val self = this
      return Encoder { schema, value -> self.encode(schema, fn(value)) }
   }
}
