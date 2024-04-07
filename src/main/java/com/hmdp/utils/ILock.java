package com.hmdp.utils;

/**
 * ClassName: ILock
 * Package: com.hmdp.utils
 * Description:
 * Create: 2024/4/7 - 23:59
 */
public interface ILock
{
    /**
     * 尝试获取锁
     */

    boolean tryLock(long timeoutSec) ;

    /**
     * 释放锁
     */
    void unlcok() ;
}
