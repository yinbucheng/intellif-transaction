package cn.intellif.transaction.intelliftransaction.listener;

import cn.intellif.transaction.intelliftransaction.core.netty.NettyClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

@Component
public class IntellifTransactionStart implements CommandLineRunner,Ordered {

    private Logger logger = LoggerFactory.getLogger(IntellifTransactionStart.class);

    @Autowired
    private NettyClient nettyClient;


    @Override
    public void run(String... args) throws Exception {
        nettyClient.start();
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}