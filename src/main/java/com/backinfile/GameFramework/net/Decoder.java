package com.backinfile.GameFramework.net;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public class Decoder extends LengthFieldBasedFrameDecoder {
    public Decoder() {
        super(Integer.MAX_VALUE, 0, 4, -4, 0);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        ByteBuf buffs = (ByteBuf) super.decode(ctx, in);
        if (buffs == null)
            return null;
        byte[] decode = new byte[buffs.readableBytes()];
        buffs.readBytes(decode);
        buffs.release();
        return decode;
    }
}
