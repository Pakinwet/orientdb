package com.orientechnologies.common.directmemory;

import com.orientechnologies.orient.core.config.OGlobalConfiguration;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.nio.ByteBuffer;

public class ODirectMemoryAllocatorTest {
  @BeforeClass
  public static void beforeClass() {
    OGlobalConfiguration.DIRECT_MEMORY_TRACK_MODE.setValue(true);
  }

  @AfterClass
  public static void afterClass() {
    OGlobalConfiguration.DIRECT_MEMORY_TRACK_MODE.setValue(false);
  }

  @Test
  public void testAllocateDeallocate() {
    final ODirectMemoryAllocator directMemoryAllocator = new ODirectMemoryAllocator();
    final OPointer pointer = directMemoryAllocator.allocate(42, -1, false);
    Assert.assertNotNull(pointer);

    Assert.assertEquals(42, directMemoryAllocator.getMemoryConsumption());

    final ByteBuffer buffer = pointer.getNativeByteBuffer();
    Assert.assertEquals(42, buffer.capacity());
    directMemoryAllocator.deallocate(pointer, false);

    Assert.assertEquals(0, directMemoryAllocator.getMemoryConsumption());
  }

  @Test
  public void testNegativeOrZeroIsPassedToAllocate() {
    final ODirectMemoryAllocator directMemoryAllocator = new ODirectMemoryAllocator();
    try {
      directMemoryAllocator.allocate(0, -1, false);
      Assert.fail();
    } catch (IllegalArgumentException e) {
      Assert.assertTrue(true);
    }

    try {
      directMemoryAllocator.allocate(-1, -1, false);
      Assert.fail();
    } catch (IllegalArgumentException e) {
      Assert.assertTrue(true);
    }
  }

  @Test
  public void testNullValueIsPassedToDeallocate() {
    final ODirectMemoryAllocator directMemoryAllocator = new ODirectMemoryAllocator();
    try {
      directMemoryAllocator.deallocate(null, false);
      Assert.fail();
    } catch (IllegalArgumentException e) {
      Assert.assertTrue(true);
    }
  }

  @Test
  public void testFillPointerWithZeros() {
    final ODirectMemoryAllocator directMemoryAllocator = new ODirectMemoryAllocator();
    final OPointer pointer = directMemoryAllocator.allocate(256, -1, false);
    pointer.clear();

    final ByteBuffer buffer = pointer.getNativeByteBuffer();
    while (buffer.position() < buffer.capacity()) {
      Assert.assertEquals(0, buffer.get());
    }
    directMemoryAllocator.deallocate(pointer, false);
  }
}
