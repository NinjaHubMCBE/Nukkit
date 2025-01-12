package cn.nukkit.network.protocol;

import cn.nukkit.Server;
import cn.nukkit.network.Network;
import cn.nukkit.utils.Binary;
import cn.nukkit.utils.BinaryStream;
import com.nukkitx.network.raknet.RakNetPriority;
import com.nukkitx.network.raknet.RakNetReliability;

/**
 * author: MagicDroidX
 * Nukkit Project
 */
public abstract class DataPacket extends BinaryStream implements Cloneable {

    public volatile boolean isEncoded = false;
    private int channel = 0;

    public static final RakNetReliability DEFAULT_RELIABILITY = RakNetReliability.RELIABLE_ORDERED;
    public static final RakNetPriority DEFAULT_PRIORITY = RakNetPriority.MEDIUM;

    public RakNetReliability reliability = DEFAULT_RELIABILITY;
    public RakNetPriority priority = DEFAULT_PRIORITY;

    public abstract byte pid();

    public abstract void decode();

    public abstract void encode();

    public final void tryEncode() {
        if (!this.isEncoded) {
            this.isEncoded = true;
            this.encode();
        }
    }

    @Override
    public DataPacket reset() {
        super.reset();
        this.putUnsignedVarInt(this.pid() & 0xff);
        return this;
    }

    public void setChannel(int channel) {
        this.channel = channel;
    }

    public int getChannel() {
        return channel;
    }

    public DataPacket clean() {
        this.setBuffer(null);
        this.setOffset(0);
        this.isEncoded = false;
        return this;
    }

    @Override
    public DataPacket clone() {
        try {
            return (DataPacket) super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    public BatchPacket compress() {
        return compress(Server.getInstance().networkCompressionLevel);
    }

    public BatchPacket compress(int level) {
        BatchPacket batch = new BatchPacket();
        byte[][] batchPayload = new byte[2][];
        byte[] buf = getBuffer();
        batchPayload[0] = Binary.writeUnsignedVarInt(buf.length);
        batchPayload[1] = buf;
        try {
            batch.payload = Network.deflateRaw(batchPayload, level);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return batch;
    }
}
