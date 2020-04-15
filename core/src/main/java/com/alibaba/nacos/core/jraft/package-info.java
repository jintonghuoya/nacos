/**
 * 这个包下面是将JRaft整合到Nacos的相关代码
 * 针对JRaft需要做如下简要说明：
 * 1.处理都是异步的，所以你会到处看到NacosClosure（用于异步结果回调）
 * 2.因为要兼容Nacos对Config和Naming的双向支持，所以需要做更多的抽象
 * 3.异步处理底层是基于Disruptor环形队列组件来实现的，比如Node.apply()是入队，StateMachine.onApply()是出队并处理
 * 很多细节的实现，请参考JRaft的官方文档或者github上的wiki
 */
package com.alibaba.nacos.core.jraft;
