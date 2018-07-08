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
package io.nuls.protocol.base.handler;

import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.model.NulsDigestData;
import io.nuls.kernel.model.Transaction;
import io.nuls.message.bus.handler.AbstractMessageHandler;
import io.nuls.network.model.Node;
import io.nuls.protocol.base.cache.TransactionDuplicateRemoval;
import io.nuls.protocol.message.TransactionMessage;
import io.nuls.protocol.service.TransactionService;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Niels
 */
public class TransactionMessageHandler extends AbstractMessageHandler<TransactionMessage> {

    private TransactionService transactionService = NulsContext.getServiceBean(TransactionService.class);

    @Override
    public void onMessage(TransactionMessage message, Node fromNode) {

        Transaction tx = message.getMsgBody();
        if (null == tx) {
            return;
        }
        if (tx.isSystemTx()) {
            return;
        }
        NulsDigestData hash = tx.getHash();
        boolean consains = TransactionDuplicateRemoval.FILTER.contains(hash.getDigestBytes());
        if (consains) {
            return;
        }
        TransactionDuplicateRemoval.FILTER.insert(hash.getDigestBytes());
        transactionService.newTx(tx);
    }

}
