package cn.intellif.transaction.intelliftransaction.utils;

import io.netty.channel.ChannelHandlerContext;

/**
 * 和txmanager通信的类
 */
public class SocketManager {
    private static SocketManager socketManager = new SocketManager();
    private ChannelHandlerContext context;
    private volatile  boolean netState = false;

    private SocketManager(){}

    public static SocketManager getInstance(){
        return socketManager;
    }

    public void setContext(ChannelHandlerContext context){
        this.context = context;
    }

    public void sendMsg(String msg){
        if(!netState)
            return;
        context.writeAndFlush(msg);
    }

    public boolean isNetState() {
        return netState;
    }

    public void setNetState(boolean netState) {
        this.netState = netState;
    }
}
