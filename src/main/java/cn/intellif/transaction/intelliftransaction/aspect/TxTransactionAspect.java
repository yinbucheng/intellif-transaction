package cn.intellif.transaction.intelliftransaction.aspect;

import cn.intellif.transaction.intelliftransaction.constant.Constant;
import cn.intellif.transaction.intelliftransaction.core.TransactionConnUtils;
import cn.intellif.transaction.intelliftransaction.core.netty.protocol.ProtocolUtils;
import cn.intellif.transaction.intelliftransaction.utils.LockUtils;
import cn.intellif.transaction.intelliftransaction.utils.SocketManager;
import cn.intellif.transaction.intelliftransaction.utils.WebUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Aspect
@Component
public class TxTransactionAspect  implements Ordered {


    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Around("@annotation(cn.intellif.transaction.intelliftransaction.anotation.StartDTransaction)")
    public Object aroundWithTx(ProceedingJoinPoint joinPoint) throws Throwable {
        return runTxTransaction(joinPoint);
    }

    @Around("this(cn.intellif.transaction.intelliftransaction.anotation.StartDTransaction) && execution( * *(..))")
    public Object aroundWithTx2(ProceedingJoinPoint joinPoint) throws Throwable {
        return runTxTransaction(joinPoint);
    }

    private Object runTxTransaction(ProceedingJoinPoint joinPoint) throws Throwable {
        //判断其是否为上一个调用链过来的如果是直接放行
        HttpServletRequest request =  WebUtils.getRequest();
        String token = null;
        if(request!=null){
            token = request.getHeader(Constant.TRANSATION_TOKEN);
        }
        if(token!=null){
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
            Object result =  joinPoint.proceed();
           //发送提交命令 及关闭命令
            SocketManager.getInstance().sendMsg(ProtocolUtils.commit());
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
        }
    }




    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE;
    }
}
