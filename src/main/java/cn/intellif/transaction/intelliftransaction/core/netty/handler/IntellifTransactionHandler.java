package cn.intellif.transaction.intelliftransaction.core.netty.handler;

import cn.intellif.transaction.intelliftransaction.aware.ApplicationContextAwareUtils;
import cn.intellif.transaction.intelliftransaction.core.TransactionConnUtils;
import cn.intellif.transaction.intelliftransaction.core.netty.NettyClient;
import cn.intellif.transaction.intelliftransaction.core.netty.entity.NettyEntity;
import cn.intellif.transaction.intelliftransaction.core.netty.protocol.ProtocolUtils;
import cn.intellif.transaction.intelliftransaction.utils.SocketManager;
import com.alibaba.fastjson.JSON;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@ChannelHandler.Sharable
public class IntellifTransactionHandler extends ChannelInboundHandlerAdapter{

    private Logger logger = LoggerFactory.getLogger(IntellifTransactionHandler.class);

    private Executor threadPool = Executors.newFixedThreadPool(30);



    public IntellifTransactionHandler() {
    }


    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
        String content = (String) msg;
        final NettyEntity entity = JSON.parseObject(content,NettyEntity.class);
        if(entity.getStatus()!=NettyEntity.PONG){
            logger.info("获取到服务器返回的信息:" + msg);
        }
        threadPool.execute(new Runnable() {
            @Override
            public void run() {
                handleMsg(entity);
            }
        });
    }

    /**
     * 处理接收到的消息
     * @param nettyEntity
     */
    private void handleMsg(final  NettyEntity nettyEntity){
        String key = nettyEntity.getKey();
        int state = nettyEntity.getStatus();
        if(state!=NettyEntity.PONG){
            logger.info(">>>>>>>>>>>>>>>>>>>>>>接收服务端数据:"+nettyEntity.toString());
        }
        if(!key.equals("")) {
            if (state == NettyEntity.COMMIT) {
                TransactionConnUtils.commit(key);
                TransactionConnUtils.release(key);
            }
            if (state == NettyEntity.ROLLBACK) {
                TransactionConnUtils.rollback(key);
                TransactionConnUtils.release(key);
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>客户端出现问题:"+ctx);
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        logger.info(">>>>>>>>>>>>>>和客户端连接已经断开  -->" + ctx);
        SocketManager.getInstance().setNetState(false);
        //链接断开,重新连接
        ApplicationContextAwareUtils.getBean(NettyClient.class).restart();
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        SocketManager.getInstance().setContext(ctx);
        SocketManager.getInstance().setNetState(true);
        //告诉服务端
        SocketManager.getInstance().sendMsg(ProtocolUtils.ping());
    }


    /**
     * 当客户端的所有ChannelHandler中4s内没有write事件，则会触发userEventTriggered方法
     *
     * @param ctx  管道
     * @param evt  状态
     * @throws Exception 异常数据
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        //心跳配置
        if (IdleStateEvent.class.isAssignableFrom(evt.getClass())) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE) {
                //表示已经多久没有收到数据了
            } else if (event.state() == IdleState.WRITER_IDLE) {
                //表示已经多久没有发送数据了
                SocketManager.getInstance().sendMsg(ProtocolUtils.ping());
            } else if (event.state() == IdleState.ALL_IDLE) {
                //表示已经多久既没有收到也没有发送数据了
            }
        }
    }

}
