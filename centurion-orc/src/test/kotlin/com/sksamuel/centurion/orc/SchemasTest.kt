package com.sksamuel.centurion.orc

import com.sksamuel.centurion.Schema
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.apache.orc.TypeDescription

class SchemasTest : FunSpec({

  test("varchars") {
    val type = TypeDescription.createVarchar().withMaxLength(33)
    val schema = Schema.Varchar(33)
    Schemas.fromOrc(type) shouldBe schema
    Schemas.toOrc(schema) shouldBe type
  }

  test("numbers") {
    val type = TypeDescription.createStruct()
      .addField("byte", TypeDescription.createByte())
      .addField("short", TypeDescription.createShort())
      .addField("int", TypeDescription.createInt())
      .addField("long", TypeDescription.createLong())
      .addField("float", TypeDescription.createFloat())
      .addField("double", TypeDescription.createDouble())

    val schema = Schema.Struct(
      "struct",
      Schema.Field("byte", Schema.Int8),
      Schema.Field("short", Schema.Int16),
      Schema.Field("int", Schema.Int32),
      Schema.Field("long", Schema.Int64),
      Schema.Field("float", Schema.Float32),
      Schema.Field("double", Schema.Float64),
    )

    Schemas.fromOrc(type) shouldBe schema
    Schemas.toOrc(schema) shouldBe type
  }

  test("structs") {

    val type = TypeDescription.createStruct()
      .addField("array", TypeDescription.createList(TypeDescription.createBoolean()))
      .addField("binary", TypeDescription.createBinary())
      .addField("boolean", TypeDescription.createBoolean())
      .addField("string", TypeDescription.createString())
      .addField(
        "struct",
        TypeDescription.createStruct()
          .addField("a", TypeDescription.createLong())
          .addField("b", TypeDescription.createString())
      )

    val schema = Schema.Struct(
      "struct",
      Schema.Field("array", Schema.Array(Schema.Booleans)),
      Schema.Field("binary", Schema.Bytes),
      Schema.Field("boolean", Schema.Booleans),
      Schema.Field("string", Schema.Strings),
      Schema.Field(
        "struct",
        Schema.Struct(
          "struct",
          Schema.Field("a", Schema.Int64),
          Schema.Field("b", Schema.Strings)
        )
      )
    )

    Schemas.fromOrc(type) shouldBe schema
    Schemas.toOrc(schema) shouldBe type
  }

  test("enums") {
    Schemas.toOrc(Schema.Enum("enum", "malbec", "shiraz")) shouldBe TypeDescription.createString()
  }

  test("decimals") {
    Schemas.toOrc(Schema.Decimal(Schema.Precision(5), Schema.Scale(2))) shouldBe
      TypeDescription.createDecimal().withScale(2).withPrecision(5)

    Schemas.fromOrc(TypeDescription.createDecimal().withScale(2).withPrecision(5)) shouldBe
      Schema.Decimal(Schema.Precision(5), Schema.Scale(2))
  }

  test("timestamp millis") {
    Schemas.toOrc(Schema.TimestampMillis) shouldBe TypeDescription.createTimestamp()
    Schemas.fromOrc(TypeDescription.createTimestamp()) shouldBe Schema.TimestampMillis
  }

  test("maps") {
    val type = TypeDescription.createMap(TypeDescription.createBoolean(), TypeDescription.createLong())
    val schema = Schema.Map(Schema.Booleans, Schema.Int64)
    Schemas.fromOrc(type) shouldBe schema
    Schemas.toOrc(schema) shouldBe type
  }
})
