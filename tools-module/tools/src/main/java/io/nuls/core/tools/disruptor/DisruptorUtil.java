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
package io.nuls.core.tools.disruptor;

import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.EventHandlerGroup;
import com.lmax.disruptor.dsl.ProducerType;
import io.nuls.core.tools.log.Log;
import io.nuls.core.tools.param.AssertUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Niels
 * @date 2017/10/10
 */
public class DisruptorUtil<T extends DisruptorData> {

    private Lock locker = new ReentrantLock();

    private static final DisruptorUtil INSTANCE = new DisruptorUtil();
    private static final Map<String, Disruptor<DisruptorData>> DISRUPTOR_MAP = new HashMap<>();

    public static DisruptorUtil getInstance() {
        return INSTANCE;
    }

    private DisruptorUtil() {
    }

    private static final EventFactory EVENT_FACTORY = new EventFactory() {
        @Override
        public Object newInstance() {
            return new DisruptorData();
        }
    };

    /**
     * create a disruptor
     *
     * @param name           The title of the disruptor
     * @param ringBufferSize The size of ringBuffer
     */
    public Disruptor<DisruptorData> createDisruptor(String name, int ringBufferSize, ThreadFactory factory) {
        if (DISRUPTOR_MAP.keySet().contains(name)) {
            throw new RuntimeException("create disruptor faild,the name is repetitive!");
        }

        Disruptor<DisruptorData> disruptor = new Disruptor<DisruptorData>(EVENT_FACTORY,
                ringBufferSize, factory, ProducerType.MULTI,
                new BlockingWaitStrategy());
        disruptor.setDefaultExceptionHandler(new NulsExceptionHandler());
        //SleepingWaitStrategy
//        disruptor.handleEventsWith(new EventHandler<DisruptorData>() {
//            @Override
//            public void onEvent(DisruptorData DisruptorData, long l, boolean b) throws Exception {
//                Log.debug(DisruptorData.getData() + "");
//            }
//        });
        DISRUPTOR_MAP.put(name, disruptor);

        return disruptor;
    }

    /**
     * start a disruptor service
     */
    public void start(String name) {
        Disruptor<DisruptorData> disruptor = DISRUPTOR_MAP.get(name);
        AssertUtil.canNotEmpty(disruptor, "the disruptor is not exist!name:" + name);
        disruptor.start();
    }

    /**
     * start a disruptor service
     */
    public void shutdown(String name) {
        Disruptor<DisruptorData> disruptor = DISRUPTOR_MAP.get(name);
        AssertUtil.canNotEmpty(disruptor, "the disruptor is not exist!name:" + name);
        disruptor.shutdown();
    }


    /**
     * add the data obj to the disruptor named the field name
     */
    public void offer(String name, Object obj) {

        Disruptor<DisruptorData> disruptor = DISRUPTOR_MAP.get(name);
        AssertUtil.canNotEmpty(disruptor, "the disruptor is not exist!name:" + name);
        RingBuffer<DisruptorData> ringBuffer = disruptor.getRingBuffer();

//        locker.lock();
        try {
            //请求下一个事件序号；
            long sequence = ringBuffer.next();
            try {
                //获取该序号对应的事件对象；
                DisruptorData event = ringBuffer.get(sequence);
                event.setData(obj);
            } catch (Exception e) {
                Log.error(e);
            } finally {
                //发布事件；
                ringBuffer.publish(sequence);
            }
        } finally {
//            locker.unlock();
        }
    }

    /**
     * add some message to worker pool of the disruptor
     */
    public EventHandlerGroup<T> handleEventsWithWorkerPool(String name, WorkHandler<DisruptorData>... handler) {
        Disruptor disruptor = DISRUPTOR_MAP.get(name);
        AssertUtil.canNotEmpty(disruptor, "the disruptor is not exist!name:" + name);
        return disruptor.handleEventsWithWorkerPool(handler);
    }

    public EventHandlerGroup<T> handleEventWith(String name, EventHandler<T> eventHandler) {
        Disruptor disruptor = DISRUPTOR_MAP.get(name);
        AssertUtil.canNotEmpty(disruptor, "the disruptor is not exist!name:" + name);
        return disruptor.handleEventsWith(eventHandler);
    }

    public EventHandlerGroup<T> after(String name, EventHandler<T> eventHandler) {
        Disruptor disruptor = DISRUPTOR_MAP.get(name);
        AssertUtil.canNotEmpty(disruptor, "the disruptor is not exist!name:" + name);
        return disruptor.after(eventHandler);
    }
}
