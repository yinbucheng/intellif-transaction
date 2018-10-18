package cn.intellif.transaction.intelliftransaction.utils;

import cn.intellif.transaction.intelliftransaction.core.TransactionConnUtils;

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
                }finally {
                    LockUtils.removeLock(key+"timeout");
                }
            }
        });
        thread.start();
    }
}
