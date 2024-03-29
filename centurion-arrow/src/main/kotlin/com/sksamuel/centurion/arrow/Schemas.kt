package com.sksamuel.centurion.arrow

import com.sksamuel.centurion.Schema
import org.apache.arrow.vector.types.FloatingPointPrecision
import org.apache.arrow.vector.types.TimeUnit
import org.apache.arrow.vector.types.pojo.ArrowType
import org.apache.arrow.vector.types.pojo.Field
import org.apache.arrow.vector.types.pojo.FieldType

object Schemas {

  fun fromArrow(schema: org.apache.arrow.vector.types.pojo.Schema): Schema {
    val fields = schema.fields.map { Schema.Field(it.name, fromArrow(it.type)) }
    return Schema.Struct("struct", fields)
  }

  fun fromArrow(arrow: ArrowType): Schema {
    return when (arrow) {
      is ArrowType.Utf8 -> Schema.Strings
      is ArrowType.Bool -> Schema.Booleans
      is ArrowType.Binary -> Schema.Bytes
      is ArrowType.Decimal -> Schema.Decimal(Schema.Precision(arrow.precision), Schema.Scale(arrow.scale))
      is ArrowType.Int -> when (arrow.bitWidth) {
        64 -> Schema.Int64
        32 -> Schema.Int32
        16 -> Schema.Int16
        8 -> Schema.Int8
        else -> error("Unsupported arrow bit width $arrow.bitWidth")
      }
      is ArrowType.FloatingPoint -> when (arrow.precision) {
        FloatingPointPrecision.HALF -> error("Unsupported floating point precision ${arrow.precision}")
        FloatingPointPrecision.SINGLE -> Schema.Float32
        FloatingPointPrecision.DOUBLE -> Schema.Float64
      }
      is ArrowType.Timestamp -> when (arrow.unit) {
        TimeUnit.MILLISECOND -> Schema.TimestampMillis
        TimeUnit.MICROSECOND -> Schema.TimestampMicros
        else -> error("Unsupported arrow time unit ${arrow.unit}")
      }
      is ArrowType.Map -> Schema.Map(Schema.Strings, Schema.Strings)
      else -> error("Unsupported arrow type $arrow")
    }
  }

  fun toArrowSchema(schema: Schema.Struct): org.apache.arrow.vector.types.pojo.Schema {
    val fields = schema.fields.map { Field(it.name, FieldType.nullable(toArrow(it.schema)), emptyList()) }
    return org.apache.arrow.vector.types.pojo.Schema(fields)
  }

  fun toArrow(schema: Schema): ArrowType {
    return when (schema) {
      Schema.Strings -> ArrowType.Utf8()
      Schema.Booleans -> ArrowType.Bool()
      Schema.Bytes -> ArrowType.Binary()
      Schema.Int64 -> ArrowType.Int(64, true)
      Schema.Int32 -> ArrowType.Int(32, true)
      Schema.Int16 -> ArrowType.Int(16, true)
      Schema.Int8 -> ArrowType.Int(8, true)
      Schema.Float64 -> ArrowType.FloatingPoint(FloatingPointPrecision.DOUBLE)
      Schema.Float32 -> ArrowType.FloatingPoint(FloatingPointPrecision.SINGLE)
      is Schema.Enum -> ArrowType.Utf8()
      is Schema.Decimal -> ArrowType.Decimal(schema.precision.value, schema.scale.value)
      Schema.TimestampMillis -> ArrowType.Timestamp(TimeUnit.MILLISECOND, "UTC")
      Schema.TimestampMicros -> ArrowType.Timestamp(TimeUnit.MICROSECOND, "UTC")
      is Schema.Varchar -> ArrowType.Utf8()
      is Schema.Map -> ArrowType.Map(false)
      else -> error("Unsupported schema $schema")
    }
  }
}
