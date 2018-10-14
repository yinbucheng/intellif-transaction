package cn.intellif.transaction.intelliftransaction.listener;

import cn.intellif.transaction.intelliftransaction.core.netty.NettyClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class ServerListener implements ApplicationListener<ApplicationReadyEvent> {

    private Logger logger = LoggerFactory.getLogger(ServerListener.class);

    @Autowired
    private NettyClient nettyClient;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
         nettyClient.start();
    }

}