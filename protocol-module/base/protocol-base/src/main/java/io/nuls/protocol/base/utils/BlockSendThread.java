///*
// * MIT License
// *
// * Copyright (c) 2017-2018 nuls.io
// *
// * Permission is hereby granted, free of charge, to any person obtaining a copy
// * of this software and associated documentation files (the "Software"), to deal
// * in the Software without restriction, including without limitation the rights
// * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// * copies of the Software, and to permit persons to whom the Software is
// * furnished to do so, subject to the following conditions:
// *
// * The above copyright notice and this permission notice shall be included in all
// * copies or substantial portions of the Software.
// *
// * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// * SOFTWARE.
// *
// */
//
//package io.nuls.protocol.base.utils;
//
//import io.nuls.core.tools.log.Log;
//import io.nuls.kernel.context.NulsContext;
//import io.nuls.kernel.func.TimeService;
//import io.nuls.kernel.model.Block;
//import io.nuls.kernel.model.NulsDigestData;
//import io.nuls.kernel.model.Result;
//import io.nuls.message.bus.service.MessageBusService;
//import io.nuls.network.model.Node;
//import io.nuls.protocol.constant.MessageDataType;
//import io.nuls.protocol.message.BlockMessage;
//import io.nuls.protocol.message.GetBlockMessage;
//import io.nuls.protocol.message.NotFoundMessage;
//import io.nuls.protocol.model.GetBlocksByHashParam;
//import io.nuls.protocol.model.NotFound;
//import io.nuls.protocol.service.BlockService;
//
//import java.util.concurrent.BlockingQueue;
//import java.util.concurrent.LinkedBlockingDeque;
//
///**
// * @author: Niels Wang
// */
//public class BlockSendThread implements Runnable {
//
//    private static final int MAX_SIZE = 1000;
//    private BlockService blockService = NulsContext.getServiceBean(BlockService.class);
//    private MessageBusService messageBusService = NulsContext.getServiceBean(MessageBusService.class);
//
//    private static BlockingQueue<BlockRequest> queue = new LinkedBlockingDeque<>(1024);
//
//    /**
//     *
//     */
//    @Override
//    public void run() {
//        while (true) {
//            try {
//                BlockRequest request = queue.take();
//                if ((TimeService.currentTimeMillis() - request.getTime()) >= 30000L) {
//                    continue;
//                }
//                execute(request);
//            } catch (Exception e) {
//                Log.error(e);
//            }
//        }
//    }
//
//    private void execute(BlockRequest request) {
//        GetBlockMessage message = request.getMessage();
//        Node fromNode = request.getNode();
//        GetBlocksByHashParam param = message.getMsgBody();
//        if (param.getSize() > MAX_SIZE) {
//            return;
//        }
//        if (param.getSize() == 1) {
//            Block block = null;
//            Result<Block> result = this.blockService.getBlock(param.getStartHash());
//            if (result.isFailed()) {
//                sendNotFound(param.getStartHash(), fromNode);
//                return;
//            }
//            block = result.getData();
//            sendBlock(block, fromNode);
//            return;
//        }
//        Block chainStartBlock = null;
//        Result<Block> blockResult = this.blockService.getBlock(param.getStartHash());
//        if (blockResult.isFailed()) {
//            sendNotFound(param.getStartHash(), fromNode);
//            return;
//        } else {
//            chainStartBlock = blockResult.getData();
//        }
//        Block chainEndBlock = null;
//        blockResult = this.blockService.getBlock(param.getEndHash());
//        if (blockResult.isFailed()) {
//            sendNotFound(param.getEndHash(), fromNode);
//            return;
//        } else {
//            chainEndBlock = blockResult.getData();
//        }
//        if (chainEndBlock.getHeader().getHeight() < chainStartBlock.getHeader().getHeight()) {
//            return;
//        }
//        long end = param.getStart() + param.getSize() - 1;
//        if (chainStartBlock.getHeader().getHeight() > param.getStart() || chainEndBlock.getHeader().getHeight() < end) {
//            sendNotFound(param.getStartHash(), fromNode);
//            return;
//        }
//
//        Block block = chainEndBlock;
//        while (true) {
//            this.sendBlock(block, fromNode);
//            if (block.getHeader().getHash().equals(chainStartBlock.getHeader().getHash())) {
//                break;
//            }
//            if (block.getHeader().getPreHash().equals(chainStartBlock.getHeader().getHash())) {
//                block = chainStartBlock;
//                continue;
//            }
//            block = blockService.getBlock(block.getHeader().getPreHash()).getData();
//        }
//    }
//
//    private void sendNotFound(NulsDigestData hash, Node node) {
//        NotFoundMessage event = new NotFoundMessage();
//        NotFound data = new NotFound(MessageDataType.BLOCK, hash);
//        event.setMsgBody(data);
//        Result result = this.messageBusService.sendToNode(event, node, true);
//        if (result.isFailed()) {
//            Log.warn("send not found failed:" + node.getId() + ", hash:" + hash);
//        }
//    }
//
//    private void sendBlock(Block block, Node fromNode) {
//        if (null == block) {
//            Log.warn("there is a null block");
//            return;
//        }
//        BlockMessage blockMessage = new BlockMessage();
//        blockMessage.setMsgBody(block);
//        Result result = this.messageBusService.sendToNode(blockMessage, fromNode, true);
//        if (result.isFailed()) {
//            Log.warn("send block failed:" + fromNode.getId() + ",height:" + block.getHeader().getHeight());
//        }
//    }
//
//    public static void offer(GetBlockMessage request, Node fromNode) {
//        queue.offer(new BlockRequest(request, fromNode));
//    }
//}
