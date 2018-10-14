package cn.intellif.transaction.intelliftransaction.anotation;

import cn.intellif.transaction.intelliftransaction.aspect.DataSourceAspect;
import cn.intellif.transaction.intelliftransaction.aspect.TransactionAspect;
import cn.intellif.transaction.intelliftransaction.aspect.TxTransactionAspect;
import cn.intellif.transaction.intelliftransaction.aware.ApplicationContextAwareUtils;
import cn.intellif.transaction.intelliftransaction.core.netty.NettyClient;
import cn.intellif.transaction.intelliftransaction.core.service.ZookeeperTxMangerList;
import cn.intellif.transaction.intelliftransaction.interceptor.TransactionRequestInterceptor;
import cn.intellif.transaction.intelliftransaction.listener.ServerListener;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import({DataSourceAspect.class, TxTransactionAspect.class, TransactionAspect.class, ZookeeperTxMangerList.class,NettyClient.class, ServerListener.class, TransactionRequestInterceptor.class, ApplicationContextAwareUtils.class})
public @interface EnableIntellifTransaction {
}
