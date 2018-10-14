package cn.intellif.transaction.intelliftransaction.core.service;

import cn.intellif.transaction.intelliftransaction.constant.Constant;
import cn.intellif.transaction.intelliftransaction.core.ITxManagerList;
import cn.intellif.transaction.intelliftransaction.zookeeper.CuratorUtils;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ZookeeperTxMangerList implements ITxManagerList {
    @Value("${intelli.txmanger.zookeeper.url}")
    private String url;


    @Override
    public Map<String,Object> acquireActiveTxManger() {
        CuratorFramework client =  CuratorUtils.getClient(url, Constant.INTELLIF_TRANSACTION_NAMSPACE);
        List<String> childs = CuratorUtils.listChildPaths(client,"/");
        if(childs==null||childs.size()==0){
            throw new RuntimeException("----------->there no active txmanger can use");
        }
        String data = childs.get(0);
        String[] temps = data.split("-");
        String url = temps[0];
        Integer port = Integer.parseInt(temps[1]);
        Map<String,Object> result = new HashMap<>();
        result.put("url",url);
        result.put("port",port);
        client.close();
        return result;
    }
}
