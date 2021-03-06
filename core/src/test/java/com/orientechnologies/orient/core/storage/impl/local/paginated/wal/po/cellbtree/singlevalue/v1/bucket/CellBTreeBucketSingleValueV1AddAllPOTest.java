package com.orientechnologies.orient.core.storage.impl.local.paginated.wal.po.cellbtree.singlevalue.v1.bucket;

import com.orientechnologies.common.directmemory.OByteBufferPool;
import com.orientechnologies.common.directmemory.OPointer;
import com.orientechnologies.common.serialization.types.OByteSerializer;
import com.orientechnologies.common.serialization.types.OIntegerSerializer;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.storage.cache.OCacheEntry;
import com.orientechnologies.orient.core.storage.cache.OCacheEntryImpl;
import com.orientechnologies.orient.core.storage.cache.OCachePointer;
import com.orientechnologies.orient.core.storage.impl.local.paginated.wal.OOperationUnitId;
import com.orientechnologies.orient.core.storage.impl.local.paginated.wal.po.PageOperationRecord;
import com.orientechnologies.orient.core.storage.index.sbtree.singlevalue.v1.CellBTreeBucketSingleValueV1;
import org.junit.Assert;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public class CellBTreeBucketSingleValueV1AddAllPOTest {
  @Test
  public void testRedo() {
    final int pageSize = 64 * 1024;
    final OByteBufferPool byteBufferPool = new OByteBufferPool(pageSize);
    try {
      final OPointer pointer = byteBufferPool.acquireDirect(false);
      final OCachePointer cachePointer = new OCachePointer(pointer, byteBufferPool, 0, 0);
      final OCacheEntry entry = new OCacheEntryImpl(0, 0, cachePointer);

      CellBTreeBucketSingleValueV1<Byte> bucket = new CellBTreeBucketSingleValueV1<>(entry);
      bucket.init(true);

      bucket.addLeafEntry(0, new byte[] { 0 }, serializeRid(new ORecordId(0, 0)));
      bucket.addLeafEntry(1, new byte[] { 1 }, serializeRid(new ORecordId(1, 1)));
      bucket.addLeafEntry(2, new byte[] { 2 }, serializeRid(new ORecordId(2, 2)));

      final List<byte[]> rawEntries = new ArrayList<>();
      for (int i = 0; i < 3; i++) {
        rawEntries.add(bucket.getRawEntry(i, false, OByteSerializer.INSTANCE));
      }
      bucket.shrink(0, false, OByteSerializer.INSTANCE);

      entry.clearPageOperations();

      final OPointer restoredPointer = byteBufferPool.acquireDirect(false);
      final OCachePointer restoredCachePointer = new OCachePointer(restoredPointer, byteBufferPool, 0, 0);
      final OCacheEntry restoredCacheEntry = new OCacheEntryImpl(0, 0, restoredCachePointer);

      final ByteBuffer originalBuffer = cachePointer.getBufferDuplicate();
      final ByteBuffer restoredBuffer = restoredCachePointer.getBufferDuplicate();

      Assert.assertNotNull(originalBuffer);
      Assert.assertNotNull(restoredBuffer);

      restoredBuffer.put(originalBuffer);

      bucket.addAll(rawEntries, false, OByteSerializer.INSTANCE);

      final List<PageOperationRecord> operations = entry.getPageOperations();
      Assert.assertEquals(1, operations.size());

      Assert.assertTrue(operations.get(0) instanceof CellBTreeBucketSingleValueV1AddAllPO);

      final CellBTreeBucketSingleValueV1AddAllPO pageOperation = (CellBTreeBucketSingleValueV1AddAllPO) operations.get(0);

      CellBTreeBucketSingleValueV1<Byte> restoredBucket = new CellBTreeBucketSingleValueV1<>(restoredCacheEntry);
      Assert.assertEquals(0, restoredBucket.size());

      pageOperation.redo(restoredCacheEntry);

      Assert.assertEquals(3, restoredBucket.size());

      Assert.assertEquals(new ORecordId(0, 0), restoredBucket.getValue(0, null, OByteSerializer.INSTANCE));
      Assert.assertEquals(new ORecordId(1, 1), restoredBucket.getValue(1, null, OByteSerializer.INSTANCE));
      Assert.assertEquals(new ORecordId(2, 2), restoredBucket.getValue(2, null, OByteSerializer.INSTANCE));

      byteBufferPool.release(pointer);
      byteBufferPool.release(restoredPointer);
    } finally {
      byteBufferPool.clear();
    }
  }

  @Test
  public void testUndo() {
    final int pageSize = 64 * 1024;

    final OByteBufferPool byteBufferPool = new OByteBufferPool(pageSize);
    try {
      final OPointer pointer = byteBufferPool.acquireDirect(false);
      final OCachePointer cachePointer = new OCachePointer(pointer, byteBufferPool, 0, 0);
      final OCacheEntry entry = new OCacheEntryImpl(0, 0, cachePointer);

      CellBTreeBucketSingleValueV1<Byte> bucket = new CellBTreeBucketSingleValueV1<>(entry);
      bucket.init(true);

      bucket.addLeafEntry(0, new byte[] { 0 }, serializeRid(new ORecordId(0, 0)));
      bucket.addLeafEntry(1, new byte[] { 1 }, serializeRid(new ORecordId(1, 1)));
      bucket.addLeafEntry(2, new byte[] { 2 }, serializeRid(new ORecordId(2, 2)));

      final List<byte[]> rawEntries = new ArrayList<>();
      for (int i = 0; i < 3; i++) {
        rawEntries.add(bucket.getRawEntry(i, false, OByteSerializer.INSTANCE));
      }

      bucket.shrink(0, false, OByteSerializer.INSTANCE);

      entry.clearPageOperations();

      bucket.addAll(rawEntries, false, OByteSerializer.INSTANCE);

      final List<PageOperationRecord> operations = entry.getPageOperations();
      Assert.assertEquals(1, operations.size());

      Assert.assertTrue(operations.get(0) instanceof CellBTreeBucketSingleValueV1AddAllPO);

      final CellBTreeBucketSingleValueV1AddAllPO pageOperation = (CellBTreeBucketSingleValueV1AddAllPO) operations.get(0);

      final CellBTreeBucketSingleValueV1<Byte> restoredBucket = new CellBTreeBucketSingleValueV1<>(entry);

      Assert.assertEquals(3, restoredBucket.size());

      Assert.assertEquals(new ORecordId(0, 0), restoredBucket.getValue(0, null, OByteSerializer.INSTANCE));
      Assert.assertEquals(new ORecordId(1, 1), restoredBucket.getValue(1, null, OByteSerializer.INSTANCE));
      Assert.assertEquals(new ORecordId(2, 2), restoredBucket.getValue(2, null, OByteSerializer.INSTANCE));

      pageOperation.undo(entry);

      Assert.assertEquals(0, restoredBucket.size());

      byteBufferPool.release(pointer);
    } finally {
      byteBufferPool.clear();
    }
  }

  @Test
  public void testSerialization() {
    OOperationUnitId operationUnitId = OOperationUnitId.generateId();

    final List<byte[]> rawRecords = new ArrayList<>();
    rawRecords.add(new byte[] { 4, 2 });
    rawRecords.add(new byte[] { 2, 4 });

    CellBTreeBucketSingleValueV1AddAllPO operation = new CellBTreeBucketSingleValueV1AddAllPO(3, rawRecords, true,
        OIntegerSerializer.INSTANCE);

    operation.setFileId(42);
    operation.setPageIndex(24);
    operation.setOperationUnitId(operationUnitId);

    final int serializedSize = operation.serializedSize();
    final byte[] stream = new byte[serializedSize + 1];
    int pos = operation.toStream(stream, 1);

    Assert.assertEquals(serializedSize + 1, pos);

    CellBTreeBucketSingleValueV1AddAllPO restoredOperation = new CellBTreeBucketSingleValueV1AddAllPO();
    restoredOperation.fromStream(stream, 1);

    Assert.assertEquals(42, restoredOperation.getFileId());
    Assert.assertEquals(24, restoredOperation.getPageIndex());
    Assert.assertEquals(operationUnitId, restoredOperation.getOperationUnitId());

    Assert.assertEquals(2, restoredOperation.getRawRecords().size());
    for (int i = 0; i < 2; i++) {
      final byte[] record = rawRecords.get(i);
      final byte[] storedRecord = restoredOperation.getRawRecords().get(i);

      Assert.assertArrayEquals(record, storedRecord);
    }

    Assert.assertTrue(restoredOperation.isEncrypted());
    Assert.assertSame(OIntegerSerializer.INSTANCE, restoredOperation.getKeySerializer());
    Assert.assertEquals(3, restoredOperation.getPrevSize());
  }

  private byte[] serializeRid(ORID rid) {
    final ByteBuffer buffer = ByteBuffer.allocate(10).order(ByteOrder.nativeOrder());
    buffer.putShort((short) rid.getClusterId());
    buffer.putLong(rid.getClusterPosition());

    return buffer.array();
  }
}
