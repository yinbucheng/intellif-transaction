package cn.intellif.transaction.intelliftransaction.listener;

import cn.intellif.transaction.intelliftransaction.core.netty.NettyClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.EmbeddedServletContainerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class ServerListener implements ApplicationListener<EmbeddedServletContainerInitializedEvent> {

    private Logger logger = LoggerFactory.getLogger(ServerListener.class);

    private int serverPort;
    @Autowired
    private NettyClient nettyClient;

    @Override
    public void onApplicationEvent(EmbeddedServletContainerInitializedEvent event) {
        this.serverPort = event.getEmbeddedServletContainer().getPort();
         nettyClient.start();
    }

    public int getPort() {
        return this.serverPort;
    }
}