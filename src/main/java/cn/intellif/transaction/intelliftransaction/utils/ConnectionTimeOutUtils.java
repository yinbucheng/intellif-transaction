package cn.intellif.transaction.intelliftransaction.utils;

import cn.intellif.transaction.intelliftransaction.core.IntellifConnetion;
import cn.intellif.transaction.intelliftransaction.core.TransactionConnUtils;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * 连接超时工具
 */
public class ConnectionTimeOutUtils {

    private static Executor executor = Executors.newCachedThreadPool();

    public static void timeOut(IntellifConnetion connetion,int timeout,String key){
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(timeout*1000);
                    if(!connetion.isClosed()){
                        connetion.realRollback();
                        connetion.realClose();
                    }
                    TransactionConnUtils.remveConnection(key);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
