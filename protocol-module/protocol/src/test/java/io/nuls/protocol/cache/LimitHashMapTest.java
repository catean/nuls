/*
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package io.nuls.protocol.cache;

import io.nuls.cache.CacheMap;
import io.nuls.cache.LimitHashMap;
import io.nuls.kernel.model.NulsDigestData;
import io.nuls.kernel.model.Transaction;
import io.nuls.protocol.model.tx.TransferTransaction;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static junit.framework.TestCase.assertTrue;

/**
 * @author: Niels Wang
 * @date: 2018/7/6
 */
public class LimitHashMapTest {

    @Test
    public void test() throws IOException {
        LimitHashMap<NulsDigestData, Transaction> map = new LimitHashMap<>(200000);
        long use = 0;
        List<NulsDigestData> hashList = new ArrayList<>();
        for (int i = 0; i < 200000; i++) {
            Transaction transaction = new TransferTransaction();
            transaction.setTime(System.currentTimeMillis());
            transaction.setHash(NulsDigestData.calcDigestData(transaction.serializeForHash()));
            hashList.add(transaction.getHash());
            long start = System.nanoTime();
            map.put(transaction.getHash(), transaction);
            use += (System.nanoTime() - start);
        }
        System.out.println("插入20万条累计用时：" + use + "纳秒");
        long start = System.currentTimeMillis();
        for (int i = 0; i < 100000; i++) {
            map.getQueue().size();
        }
        System.out.println("queue size 100000次用时：" + (System.currentTimeMillis() - start) + "ms");
        start = System.currentTimeMillis();
        for (int i = 0; i < 100000; i++) {
            map.getMap().size();
        }
        System.out.println("map size 100000次用时：" + (System.currentTimeMillis() - start) + "ms");
        start = System.currentTimeMillis();
        for (NulsDigestData key : hashList) {
            map.get(key);
        }
        System.out.println("查询200000次用时：" + (System.currentTimeMillis() - start) + "ms");
        start = System.currentTimeMillis();
        for (NulsDigestData key : hashList) {
            map.containsKey(key);
        }
        System.out.println("判断是否包含200000次用时：" + (System.currentTimeMillis() - start) + "ms");
        start = System.currentTimeMillis();
        for (NulsDigestData key : hashList) {
            map.remove(key);
        }
        System.out.println("删除200000次用时：" + (System.currentTimeMillis() - start) + "ms");
        assertTrue(true);

    }

    @Test
    public void test1() throws IOException {
        CacheMap<NulsDigestData, Transaction> map = new CacheMap<>("a-test", 128, NulsDigestData.class, Transaction.class);
        long use = 0;
        List<NulsDigestData> hashList = new ArrayList<>();
        for (int i = 0; i < 200000; i++) {
            Transaction transaction = new TransferTransaction();
            transaction.setTime(System.currentTimeMillis());
            transaction.setHash(NulsDigestData.calcDigestData(transaction.serializeForHash()));
            hashList.add(transaction.getHash());
            long start = System.nanoTime();
            map.put(transaction.getHash(), transaction);
            use += (System.nanoTime() - start);
        }
        System.out.println("插入20万条累计用时：" + use + "纳秒");
        long start = System.currentTimeMillis();
        for (NulsDigestData key : hashList) {
            map.get(key);
        }
        System.out.println("查询200000次用时：" + (System.currentTimeMillis() - start) + "ms");
        start = System.currentTimeMillis();
        for (NulsDigestData key : hashList) {
            map.containsKey(key);
        }
        System.out.println("判断是否包含200000次用时：" + (System.currentTimeMillis() - start) + "ms");
        start = System.currentTimeMillis();
        for (NulsDigestData key : hashList) {
            map.remove(key);
        }
        System.out.println("删除200000次用时：" + (System.currentTimeMillis() - start) + "ms");
        assertTrue(true);

    }

    @Test
    public void testSet() {
        List<NulsDigestData> list = new ArrayList<>();
        for (int i = 0; i < 1000000; i++) {
            list.add(NulsDigestData.calcDigestData((i + "").getBytes()));
        }
        Set<NulsDigestData> set = new HashSet<>();
        long start = System.nanoTime();
        for (NulsDigestData hash : list) {
            set.add(hash);
        }
        System.out.println("放入set中100万条用时：" + (System.nanoTime() - start)/1000000 + "ms");
        Set<NulsDigestData> set2 = new HashSet<>();
        start = System.nanoTime();
        set2.addAll(set);
        System.out.println("addAll 100万条用时：" + (System.nanoTime() - start)/1000000 + "ms");
        start = System.nanoTime();
        for (NulsDigestData hash : list) {
            set2.remove(hash);
        }
        System.out.println("remove100万条用时：" + (System.nanoTime() - start)/1000000 + "ms");
        start = System.nanoTime();
        for (NulsDigestData hash : list) {
            set.contains(hash);
        }
        System.out.println("contains100万条用时：" + (System.nanoTime() - start)/1000000 + "ms");
        start = System.nanoTime();
        set.clear();
        System.out.println("clear100万条用时：" + (System.nanoTime() - start)/1000000 + "ms");

    }

}
