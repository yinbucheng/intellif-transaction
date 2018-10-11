package cn.intellif.transaction.intelliftransaction.core.netty.protocol;

import cn.intellif.transaction.intelliftransaction.core.netty.entity.NettyEntity;
import com.alibaba.fastjson.JSON;

/**
 * 通信内容构建
 */
public class ProtocolUtils {

    private static String getContent(NettyEntity entity){
        String content =  JSON.toJSONString(entity);
//        content+="\n";
        return content;
    }

    /**
     * 发送ping命令
     * @return
     */
    public static String ping(){
       return getContent(NettyEntity.getPing());
    }



    /**
     * 发送pong命令
     * @return
     */
    public static String pong(){
       return getContent(NettyEntity.getPong());
    }

    /**
     * 发送提交命令
     * @return
     */
    public static String commit(){
        return getContent(NettyEntity.getCommit());
    }

    /**
     * 发送回滚命令
     * @return
     */
    public static String rollback(){
        return getContent(NettyEntity.getRollback());
    }

    /**
     * 发送释放资源命令
     * @return
     */
    public static String clear(){
        return getContent(NettyEntity.getClose());
    }
}
