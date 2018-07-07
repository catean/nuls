/*
 * *
 *  * MIT License
 *  *
 *  * Copyright (c) 2017-2018 nuls.io
 *  *
 *  * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  * of this software and associated documentation files (the "Software"), to deal
 *  * in the Software without restriction, including without limitation the rights
 *  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  * copies of the Software, and to permit persons to whom the Software is
 *  * furnished to do so, subject to the following conditions:
 *  *
 *  * The above copyright notice and this permission notice shall be included in all
 *  * copies or substantial portions of the Software.
 *  *
 *  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  * SOFTWARE.
 *
 */

package io.nuls.consensus.poc.provider;

import io.nuls.consensus.poc.constant.BlockContainerStatus;
import io.nuls.consensus.poc.container.BlockContainer;
import io.nuls.core.tools.log.BlockLog;
import io.nuls.kernel.context.NulsContext;
import io.nuls.protocol.service.DownloadService;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * @author ln
 */
public class BlockQueueProvider {

    private final static BlockQueueProvider INSTANCE = new BlockQueueProvider();

    private Queue<BlockContainer> blockQueue;
    private Queue<BlockContainer> downloadBlockQueue;

    private DownloadService downloadService;

    private boolean downloadBlockQueueHasDestory;

    private BlockQueueProvider() {
        blockQueue = new LinkedBlockingDeque<>();
        downloadBlockQueue = new LinkedBlockingDeque<>();
        initDownloadQueue();
    }

    public static BlockQueueProvider getInstance() {
        return INSTANCE;
    }

    public boolean put(BlockContainer blockContainer) {

        boolean receive = (blockContainer.getStatus() == BlockContainerStatus.RECEIVED);

        if (receive) {
            int status = BlockContainerStatus.RECEIVED;
            checkDownloadService();
            if (!downloadService.isDownloadSuccess().isSuccess()) {
                status = BlockContainerStatus.DOWNLOADING;
            }
            blockContainer.setStatus(status);
            BlockLog.debug("=========================================Before blockQueue offer blockContainer: " + blockContainer.toString());
            blockQueue.offer(blockContainer);
        } else {
            if (downloadBlockQueueHasDestory) {
                initDownloadQueue();
            }
            downloadBlockQueue.offer(blockContainer);
        }
        return true;
    }

    private void checkDownloadService() {
        if (null == downloadService) {
            downloadService = NulsContext.getServiceBean(DownloadService.class);
        }
    }

    public BlockContainer get() {

        BlockContainer blockContainer = null;

        //check can destory the download queue
        if (!downloadBlockQueueHasDestory) {
            BlockLog.debug("=========================================111");
            blockContainer = downloadBlockQueue.poll();
        }
        checkDownloadService();
        boolean hasDownloadSuccess = downloadService.isDownloadSuccess().isSuccess();
        if (blockContainer == null && hasDownloadSuccess && !downloadBlockQueueHasDestory) {
            BlockLog.debug("=========================================222");
            downloadBlockQueueHasDestory = true;
            if (blockContainer == null) {
                blockContainer = blockQueue.poll();
            }
        } else if (hasDownloadSuccess && blockContainer == null) {
            BlockLog.debug("=========================================333");
            blockContainer = blockQueue.poll();
        }
        BlockLog.debug("=========================================444 blockContainer: " + (blockContainer == null ? blockContainer :blockContainer.toString()));
        return blockContainer;
    }

    public long size() {
        long size = blockQueue.size();

        if (!downloadBlockQueueHasDestory) {
            size += downloadBlockQueue.size();
        }

        return size;
    }

    public void clear() {
        blockQueue.clear();
        downloadBlockQueue.clear();
        downloadBlockQueueHasDestory = false;
    }

    private void initDownloadQueue() {
        downloadBlockQueueHasDestory = false;
    }
}
