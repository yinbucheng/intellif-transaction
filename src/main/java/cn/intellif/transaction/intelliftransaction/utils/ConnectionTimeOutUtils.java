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
        protected boolean removeEldestEntry(Map.Entry eldest) {
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
                            successFlag.put(key, false);
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


        public static boolean successFlag(String key){
            synchronized (successFlag) {
                Boolean flag = successFlag.get(key);
                if (flag == null)
                    return true;
                return false;
            }
        }

        public static void removeSuccessFlag(String key){
            synchronized (successFlag) {
                successFlag.remove(key);
            }
        }
}
