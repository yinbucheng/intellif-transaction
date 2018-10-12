package cn.intellif.transaction.intelliftransaction.core;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TransactionConnUtils {
    private static Map<String,IntellifConnetion> cache = new HashMap<>();

    private static ThreadLocal<String> keys = new ThreadLocal<>();

    private static ThreadLocal<Boolean> use = new ThreadLocal<Boolean>(){
        @Override
        protected Boolean initialValue() {
            return false;
        }
    };

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
        keys.set(UUID.randomUUID().toString()+System.currentTimeMillis());
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
    public  static void commit(){
      IntellifConnetion intellifConnetion =   cache.get(getKey());
      try {
        if(intellifConnetion!=null&&!intellifConnetion.isClosed()&&!use.get()) {
              intellifConnetion.realCommit();
              use.set(true);
         }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 回滚
     */
    public static void rollback(){
        IntellifConnetion intellifConnetion = cache.get(getKey());
        try {
            if(intellifConnetion!=null&&!intellifConnetion.isClosed()&&!use.get()) {
                intellifConnetion.realRollback();
                use.set(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 释放资源
     */
    public static void release(){
      String key =  keys.get();
      keys.remove();
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
          use.remove();
      }
    }

}
