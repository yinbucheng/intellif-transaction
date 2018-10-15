package cn.intellif.transaction.intelliftransaction.core.service;

import cn.intellif.transaction.intelliftransaction.constant.Constant;
import cn.intellif.transaction.intelliftransaction.core.ITxManagerList;
import cn.intellif.transaction.intelliftransaction.zookeeper.CuratorUtils;
import org.apache.curator.framework.CuratorFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ZookeeperTxMangerList implements ITxManagerList {
    @Value("${intelli.txmanger.zookeeper.url}")
    private String url;
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public Map<String,Object> acquireActiveTxManger() {
        CuratorFramework client =  CuratorUtils.getClient(url, Constant.INTELLIF_TRANSACTION_NAMSPACE);
        try {
            List<String> childs = CuratorUtils.listChildPaths(client, "/");
            logger.info("----------->get path from zookeeper:" + childs);
            if (childs == null || childs.size() == 0) {
                logger.error("----------->there no active txmanger can use, use default host 127.0.0.1 and port 9898");
                Map<String, Object> map = new HashMap<>();
                map.put("url", "127.0.0.1");
                map.put("port", 9898);
                return map;
            }
            Collections.sort(childs);
            String data = childs.get(0);
            String[] temps = data.split("-");
            String url = temps[1];
            Integer port = Integer.parseInt(temps[2]);
            Map<String, Object> result = new HashMap<>();
            result.put("url", url);
            result.put("port", port);
            logger.info("----------->acquire netty connection url and port from zookeeper, url:" + url + " port:" + port);
            return result;
        }catch (Exception e){
            logger.error("-----------> acquire netty connection cause error:"+e.getCause()+e.getMessage());
            Map<String, Object> map = new HashMap<>();
            map.put("url", "127.0.0.1");
            map.put("port", 9898);
            return map;
        }finally {
            if(client!=null) {
                client.close();
            }
        }
    }
}
