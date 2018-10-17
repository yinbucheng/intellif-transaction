package cn.intellif.transaction.intelliftransaction.utils;

import cn.intellif.transaction.intelliftransaction.core.IntellifConnetion;
import cn.intellif.transaction.intelliftransaction.core.TransactionConnUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;

/**
 * 连接超时工具
 */
public class ConnectionTimeOutUtils {


    public static void timeOut(int timeout,String key){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    LockUtils.initLock(key+"timeout");
                    LockUtils.getLock(key+"timeout").await(timeout);
                    TransactionConnUtils.rollback(key);
                    TransactionConnUtils.remveConnection(key);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }
}
