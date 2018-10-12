package cn.intellif.transaction.intelliftransaction.core.netty;

import cn.intellif.transaction.intelliftransaction.core.netty.handler.IntellifTransactionHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class NettyClient implements DisposableBean{

    private static Logger logger = LoggerFactory.getLogger(NettyClient.class);
    private EventLoopGroup workerGroup;
    private volatile  boolean isStarting = false;
    @Value("${intellif.txmanger.port}")
    private Integer port;
    @Value("${intellif.txmanager.host}")
    private String host;
    @Value("${intellif.txmanager.heart}")
    private Integer heart;

    /**
     * 重启
     */
    public void restart(){
        isStarting = false;
        start();
    }

    /**
     * 启动
     */
    public void start(){
        if (isStarting) {
            return;
        }
        isStarting = true;

        workerGroup = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(workerGroup);
            b.channel(NioSocketChannel.class);
            b.option(ChannelOption.SO_KEEPALIVE, true);
            b.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast("timeout", new IdleStateHandler(10, 5, 20, TimeUnit.SECONDS));
                    ch.pipeline().addLast(new StringEncoder());
                    ch.pipeline().addLast(new LineBasedFrameDecoder(1024));
                    ch.pipeline().addLast(new StringDecoder());
                    ch.pipeline().addLast(new IntellifTransactionHandler());
                }
            });

            ChannelFuture future = b.connect(host, port);
            logger.info(">>>>>>>>>>>>>>>>>>>>>>>客户端已经启动 host:" + host + ",port:" + port);
            future.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    if (!channelFuture.isSuccess()) {
                        channelFuture.channel().eventLoop().schedule(new Runnable() {
                            @Override
                            public void run() {
                                isStarting = false;
                                start();
                            }
                        }, 5, TimeUnit.SECONDS);
                    }
                }
            });
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage());
            isStarting =false;
            if(workerGroup!=null){
                workerGroup.shutdownGracefully();
            }
        }
    }

    @Override
    public void destroy() throws Exception {
        if(workerGroup!=null){
            workerGroup.shutdownGracefully();
        }
    }
}
