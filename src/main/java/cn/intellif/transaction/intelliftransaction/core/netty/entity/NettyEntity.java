package cn.intellif.transaction.intelliftransaction.core.netty.entity;

import cn.intellif.transaction.intelliftransaction.core.TransactionConnUtils;

import java.io.Serializable;

/**
 * 通信机制
 */
public class NettyEntity implements Serializable{
    private String key;
    private Integer status;
    public static int PING =0;
    public static int PONG =1;
    public static int COMMIT =2;
    public static int ROLLBACK =3;
    public static int CLOSE =4;

    public NettyEntity() {
    }

    public NettyEntity(String key, Integer status) {
        this.key = key;
        this.status = status;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public static NettyEntity getPing(){
        String key  =TransactionConnUtils.getKey();
        if(key==null){
            key = "";
        }
        return new NettyEntity(key,PING);
    }

    public static NettyEntity getPong(){
        return new NettyEntity(TransactionConnUtils.getKey(),PONG);
    }

    public static NettyEntity getCommit(){
        return new NettyEntity(TransactionConnUtils.getKey(),COMMIT);
    }

    public static NettyEntity getRollback(){
        return new NettyEntity(TransactionConnUtils.getKey(),ROLLBACK);
    }

    public static NettyEntity getClose(){
        return new NettyEntity(TransactionConnUtils.getKey(),CLOSE);
    }
}
