package com.sksamuel.centurion.partitioners

import com.sksamuel.centurion.DatabaseName
import com.sksamuel.centurion.Partition
import com.sksamuel.centurion.TableName
import org.apache.hadoop.fs.FileSystem
import org.apache.hadoop.fs.Path
import org.apache.hadoop.hive.metastore.IMetaStoreClient

object StaticPartitioner : Partitioner {

  override fun path(dbName: DatabaseName,
                    tableName: TableName,
                    partition: Partition,
                    client: IMetaStoreClient,
                    fs: FileSystem): Path {
    return try {
      val p = client.getPartition(dbName.value, tableName.value, partition.parts.map { it.value })
      Path(p.sd.location)
    } catch (t: Throwable) {
      throw RuntimeException("Partition $partition does not exist and static partitioner requires upfront creation", t)
    }
  }
}
