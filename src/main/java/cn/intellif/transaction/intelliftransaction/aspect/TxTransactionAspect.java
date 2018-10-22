package cn.intellif.transaction.intelliftransaction.aspect;

import cn.intellif.transaction.intelliftransaction.constant.Constant;
import cn.intellif.transaction.intelliftransaction.core.TransactionConnUtils;
import cn.intellif.transaction.intelliftransaction.core.netty.protocol.ProtocolUtils;
import cn.intellif.transaction.intelliftransaction.utils.ConnectionTimeOutUtils;
import cn.intellif.transaction.intelliftransaction.utils.LockUtils;
import cn.intellif.transaction.intelliftransaction.utils.SocketManager;
import cn.intellif.transaction.intelliftransaction.utils.WebUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class TxTransactionAspect  implements Ordered {


    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Value("${intellif.transaction.timeout}")
    private Integer timeout;

    @Around("@annotation(cn.intellif.transaction.intelliftransaction.anotation.TxTransaction)")
    public Object aroundWithTx(ProceedingJoinPoint joinPoint) throws Throwable {
        return runTxTransaction(joinPoint);
    }

    @Around("this(cn.intellif.transaction.intelliftransaction.anotation.TxTransaction) && execution( * *(..))")
    public Object aroundWithTx2(ProceedingJoinPoint joinPoint) throws Throwable {
        return runTxTransaction(joinPoint);
    }

    private Object runTxTransaction(ProceedingJoinPoint joinPoint) throws Throwable {
        //判断其是否为上一个调用链过来的如果是直接放行,或者已经执行过TxTransaction
        String token =   WebUtils.getRequest().getHeader(Constant.TRANSATION_TOKEN);
        if(token!=null&&!token.equals("")||TransactionConnUtils.keyIsNotEmpty()){
            return joinPoint.proceed();
        }
        /**
         * 创建唯一标示
         */
        TransactionConnUtils.intKey();
        String key = TransactionConnUtils.getKey();
        try{
            if(!SocketManager.getInstance().getNetState()){
                throw new RuntimeException("-----------> txmanger is closed please make sure txmanager is running");
            }
            logger.info("-----------> begian create a new  distributed transaction");
            //将唯一表示告诉txmanger并开启超时机制
            LockUtils.initLock(key);
            SocketManager.getInstance().sendMsg(ProtocolUtils.register());
            LockUtils.getLock(key).await(120);
            ConnectionTimeOutUtils.timeOut(timeout,key);
            Object result =  joinPoint.proceed();
            if(ConnectionTimeOutUtils.successFlag(key)) {
                //发送提交命令 及关闭命令
                SocketManager.getInstance().sendMsg(ProtocolUtils.commit());
            }else{
                throw new RuntimeException("----------->throw timeout runing ");
            }
            return result;
        }catch (Exception e){
            //发送回滚及 关闭命令
            SocketManager.getInstance().sendMsg(ProtocolUtils.rollback());
            TransactionConnUtils.rollback(key);
            throw new RuntimeException(e);
        }finally {
            //释放资源
            Thread.sleep(20);
            SocketManager.getInstance().sendMsg(ProtocolUtils.clear());
            TransactionConnUtils.release();
            LockUtils.removeLock(key);
            ConnectionTimeOutUtils.removeSuccessFlag(key);
        }
    }




    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE;
    }
}
