package com.sksamuel.centurion.avro.decoders

import org.apache.avro.Schema
import java.time.Instant
import java.time.LocalTime
import kotlin.reflect.KClass
import kotlin.reflect.KType

/**
 * A [Decoder] typeclass is used to convert an Avro value, such as a [GenericRecord],
 * [SpecificRecord], [GenericFixed], [EnumSymbol], or a basic type, into a specified JVM type.
 *
 * For example, a [Decoder<String>] would convert an input such as a GenericFixed, byte array, or Utf8
 * into a plain JVM [String].
 *
 * Another example, a decoder for nullable types would handle null-based Unions.
 */
fun interface Decoder<T> {

   fun decode(schema: Schema): (Any?) -> T

   fun <U> map(fn: (T) -> U): Decoder<U> {
      val self = this
      return Decoder { schema ->
         { value -> fn(self.decode(schema).invoke(value)) }
      }
   }

   companion object {

      var useStrictPrimitiveDecoders = true

      fun decoderFor(type: KType): Decoder<*> {
         val decoder: Decoder<*> = when (val classifier = type.classifier) {
            String::class -> StringDecoder
            Boolean::class -> if (useStrictPrimitiveDecoders) StrictBooleanDecoder else BooleanDecoder
            Float::class -> if (useStrictPrimitiveDecoders) StrictFloatDecoder else FloatDecoder
            Double::class -> if (useStrictPrimitiveDecoders) StrictDoubleDecoder else DoubleDecoder
            Int::class -> if (useStrictPrimitiveDecoders) StrictIntDecoder else IntDecoder
            Long::class -> if (useStrictPrimitiveDecoders) StrictLongDecoder else LongDecoder
            List::class -> ListDecoder(decoderFor(type.arguments.first().type!!))
            LongArray::class -> LongArrayDecoder(if (useStrictPrimitiveDecoders) StrictLongDecoder else LongDecoder)
            IntArray::class -> IntArrayDecoder(if (useStrictPrimitiveDecoders) StrictIntDecoder else IntDecoder)
            Set::class -> SetDecoder(decoderFor(type.arguments.first().type!!))
            Map::class -> MapDecoder(decoderFor(type.arguments[1].type!!))
            LocalTime::class -> LocalTimeDecoder
            Instant::class -> InstantDecoder
            is KClass<*> -> if (classifier.java.isEnum) EnumDecoder(classifier as KClass<out Enum<*>>) else error("Unsupported type $type")
            else -> error("Unsupported type $type")
         }
         return if (type.isMarkedNullable) NullDecoder(decoder) else decoder
      }
   }
}
