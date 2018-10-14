package cn.intellif.transaction.intelliftransaction.core;

import cn.intellif.transaction.intelliftransaction.utils.LockUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class TransactionConnUtils {
    private static LinkedHashMap<String,IntellifConnetion> cache = new LinkedHashMap<String,IntellifConnetion>(){
        @Override
        protected boolean removeEldestEntry(Map.Entry eldest) {
            return size()>200;
        }
    };
    private static Logger logger = LoggerFactory.getLogger(TransactionConnUtils.class);

    private static ThreadLocal<String> keys = new ThreadLocal<>();

    private static volatile  int currentThread = 0;

    private static volatile int maxThread = 30;


    private static final String LOCK ="CURRENT_THREAD";

    public static boolean canAcessConn(){
        return currentThread<maxThread;
    }




    public static void increateConn(){
        synchronized (LOCK){
            currentThread++;
        }
    }

    public static void reduceConn(){
        synchronized (LOCK){
            currentThread--;
        }
    }


    public static boolean keyIsNotEmpty(){
         String key = keys.get();
         if(key==null||key.equals(""))
             return false;
         return true;
    }


    public static synchronized void remveConnection(String key){
        cache.remove(key);
    }

    /**
     * 初始化唯一表示
     */
    public static void intKey(){
        String key = UUID.randomUUID().toString()+System.currentTimeMillis();
        keys.set(key);
    }

    /**
     * 设置唯一标示
     * @param token
     */
    public static void initKey(String token){
        keys.set(token);
    }

    /**
     * 获取当前线程的改造数据库连接对象
     * @return
     */
    public static IntellifConnetion getConnection(){
        return cache.get(getKey());
    }

    public static String getKey(){
        String key =  keys.get();
        if(key==null){
            key ="";
        }
        return key;
    }

    /**
     * 初始化连接
     * @param connetion
     */
    public synchronized static void initConn(IntellifConnetion connetion){
        cache.put(getKey(),connetion);
    }


    /**
     * 提交
     */
    public  static void commit(String key){
        synchronized (key) {
            IntellifConnetion intellifConnetion = cache.get(key);
            try {
                if (intellifConnetion != null && !intellifConnetion.isClosed()) {
                    intellifConnetion.realCommit();
                    intellifConnetion.realClose();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }


    /**
     * 回滚
     */
    public static void rollback(String key){
        synchronized (key) {
            IntellifConnetion intellifConnetion = cache.get(key);
            try {
                if (intellifConnetion != null && !intellifConnetion.isClosed()) {
                    intellifConnetion.realRollback();
                    intellifConnetion.realClose();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 释放资源
     */
    public static void release(){
       keys.remove();
    }

    public static void removeConnCache(String key) {
        synchronized (TransactionConnUtils.class){
           IntellifConnetion connetion =  cache.get(key);
            try {
                if(connetion!=null&&!connetion.isClosed()) {
                    connetion.realClose();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            cache.remove(key);
        }
    }

    /**
     * 释放资源
     */
    public static void release(String key){
        removeConnCache(key);
        reduceConn();
    }
}
