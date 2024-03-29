@file:Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")

package com.sksamuel.centurion.parquet

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.FileSystem
import org.apache.hadoop.fs.Path

class CountTest : FunSpec() {

  private val conf = Configuration()
  private val fs = FileSystem.getLocal(conf)

  init {
    test("count should return count for a single file") {
      val path = Path(javaClass.getResource("/userdata.parquet").toURI())
      Parquet.count(listOf(path), conf) shouldBe 1000L
    }

    test("count should return count for multiple files") {
      val path = Path(javaClass.getResource("/userdata.parquet").toURI())
      Parquet.count(listOf(path, path, path), conf) shouldBe 3000L
    }
  }
}
