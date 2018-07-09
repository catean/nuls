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

package io.nuls.protocol.base.cache;

import io.nuls.kernel.model.NulsDigestData;
import io.nuls.kernel.thread.manager.TaskManager;
import io.nuls.protocol.base.utils.filter.InventoryFilter;

import java.util.HashSet;
import java.util.Set;

/**
 * 用于接收交易去重
 *
 * @author: Niels Wang
 * @date: 2018/7/8
 */
public class TransactionDuplicateRemoval {

    private static InventoryFilter FILTER = new InventoryFilter( 1000000);

    public static boolean mightContain(NulsDigestData hash) {
        return FILTER.contains(hash.getDigestBytes());
    }

    public static void insert(NulsDigestData hash) {
        FILTER.insert(hash.getDigestBytes());
    }
}
