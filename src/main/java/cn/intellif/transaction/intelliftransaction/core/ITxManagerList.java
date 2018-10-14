package cn.intellif.transaction.intelliftransaction.core;

import java.util.Map;

public interface ITxManagerList {
    Map<String,Object> acquireActiveTxManger();
}
