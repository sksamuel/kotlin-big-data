package com.sksamuel.centurion.avro.decoders

import com.sksamuel.centurion.avro.encoders.Wine
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.apache.avro.Schema
import org.apache.avro.SchemaBuilder
import org.apache.avro.generic.GenericData
import org.apache.avro.util.Utf8

class SpecificRecordDecoderTest : FunSpec({

   test("basic record decoder") {
      data class Foo(val a: String, val b: Boolean)

      val schema = SchemaBuilder.record("Foo").fields().requiredString("a").requiredBoolean("b").endRecord()

      val record = GenericData.Record(schema)
      record.put("a", Utf8("hello"))
      record.put("b", true)

      SpecificRecordDecoder(Foo::class, schema).decode(schema, record) shouldBe Foo("hello", true)
   }

   test("enums") {
      data class Foo(val wine: Wine)

      val wineSchema = Schema.createEnum("Wine", null, null, listOf("Shiraz", "Malbec"))
      val schema = SchemaBuilder.record("Foo").fields()
         .name("wine").type(wineSchema).noDefault()
         .endRecord()

      val record = GenericData.Record(schema)
      record.put("wine", GenericData.get().createEnum("Shiraz", wineSchema))

      SpecificRecordDecoder<Foo>(Foo::class, schema).decode(schema, record) shouldBe Foo(Wine.Shiraz)
   }

   test("nulls") {
      data class Foo(val a: String?, val b: String)

      val schema = SchemaBuilder.record("Foo").fields()
         .optionalString("a")
         .requiredString("b")
         .endRecord()

      val record = GenericData.Record(schema)
      record.put("a", null)
      record.put("b", Utf8("hello"))

      SpecificRecordDecoder<Foo>(Foo::class, schema).decode(schema, record) shouldBe Foo(null, "hello")
   }

   test("sets") {
      data class Foo(val set1: Set<Int>, val set2: Set<Long?>, val set3: Set<Wine>)

      val enum = SchemaBuilder.enumeration("Wine").symbols("Malbec", "Shiraz")
      val schema = SchemaBuilder.record("Foo").fields()
         .name("set1").type().array().items().intType().noDefault()
         .name("set2").type().array().items().type(SchemaBuilder.nullable().longType()).noDefault()
         .name("set3").type().array().items().type(enum).noDefault()
         .endRecord()

      val record = GenericData.Record(schema)
      record.put("set1", listOf(1, 2))
      record.put("set2", listOf(1, null, 2))
      record.put(
         "set3",
         listOf(
            GenericData.get().createEnum("Shiraz", enum),
            GenericData.get().createEnum("Malbec", enum),
         )
      )

      SpecificRecordDecoder<Foo>(Foo::class, schema).decode(schema, record) shouldBe Foo(
         setOf(1, 2),
         setOf(1L, null, 2L),
         setOf(Wine.Shiraz, Wine.Malbec),
      )
   }

   test("list") {
      data class Foo(val list1: List<Int>, val list2: List<Long?>, val list3: List<Wine>)

      val enum = SchemaBuilder.enumeration("Wine").symbols("Malbec", "Shiraz")
      val schema = SchemaBuilder.record("Foo").fields()
         .name("list1").type().array().items().intType().noDefault()
         .name("list2").type().array().items().type(SchemaBuilder.nullable().longType()).noDefault()
         .name("list3").type().array().items().type(enum).noDefault()
         .endRecord()

      val record = GenericData.Record(schema)
      record.put("list1", listOf(1, 2))
      record.put("list2", listOf(1, null, 2))
      record.put(
         "list3",
         listOf(
            GenericData.get().createEnum("Shiraz", enum),
            GenericData.get().createEnum("Malbec", enum),
         )
      )

      SpecificRecordDecoder<Foo>(Foo::class, schema).decode(schema, record) shouldBe Foo(
         listOf(1, 2),
         listOf(1L, null, 2L),
         listOf(Wine.Shiraz, Wine.Malbec),
      )
   }
})