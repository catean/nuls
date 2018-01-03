package io.nuls.core.event;

import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.core.chain.entity.NulsDigestData;
import io.nuls.core.chain.entity.NulsSignData;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.crypto.Utils;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.io.NulsOutputStreamBuffer;

import java.io.IOException;

/**
 * @author Niels
 * @date 2017/11/7
 */
public class EventHeader extends BaseNulsData {
    private static final int EVENT_HEADER_LENGHT = 4;
    private short moduleId;
    private short eventType;

    public EventHeader(short moduleId, short eventType, byte[] extend) {
        this.moduleId = moduleId;
        this.eventType = eventType;
    }

    public EventHeader(short moduleId, short eventType) {
        this(moduleId, eventType, null);
    }

    public EventHeader(short eventType) {
        this((short) (0), eventType);
    }

    public EventHeader() {
        this((short) 0, (short) 0);
    }

    @Override
    public int size() {
        return EVENT_HEADER_LENGHT;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        byte[] header = new byte[EVENT_HEADER_LENGHT];
        Utils.int16ToByteArrayLE(moduleId, header, 0);
        Utils.int16ToByteArrayLE(eventType, header, 2);
        stream.write(header);
    }

    public short getEventType() {
        return eventType;
    }

    public void setEventType(short eventType) {
        this.eventType = eventType;
    }

    public short getModuleId() {
        return moduleId;
    }

    public void setModuleId(short moduleId) {
        this.moduleId = moduleId;
    }

    @Override
    public void parse(NulsByteBuffer buffer) throws NulsException {
        this.moduleId = buffer.readInt16LE();
        this.eventType = buffer.readInt16LE();
    }
}