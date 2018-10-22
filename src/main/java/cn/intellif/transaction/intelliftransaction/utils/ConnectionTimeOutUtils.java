package cn.intellif.transaction.intelliftransaction.utils;

import cn.intellif.transaction.intelliftransaction.core.TransactionConnUtils;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 连接超时工具
 */
public class ConnectionTimeOutUtils {

    public static Map<String,Boolean> successFlag = new LinkedHashMap<String,Boolean>(){
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Boolean> eldest) {
            return entrySet().size()>1000;
        }
    };


    public static void timeOut(int timeout,String key){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    LockUtils.initLock(key+"timeout");
                    LockUtils.getLock(key+"timeout").await(timeout);
                    TransactionConnUtils.rollback(key);
                    TransactionConnUtils.remveConnection(key);
                    synchronized (successFlag) {
                        successFlag.put(key, true);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }finally {
                    LockUtils.removeLock(key+"timeout");
                }
            }
        });
        thread.start();
    }

    public static boolean timeOut(String key){
        synchronized (successFlag) {
            Boolean flag = successFlag.get(key);
            if (flag == null)
                return false;
            return true;
        }
    }

    public static void removeTimeOutFlag(String key){
        synchronized (successFlag) {
            successFlag.remove(key);
        }
    }
}
