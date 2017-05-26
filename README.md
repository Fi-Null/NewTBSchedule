# NewTBSchedule
Transform And Upgrade TBSchedule
对[TBSchedule]分布式任务调度进行了简单改造；
主要改造点：
1、使用ScheduledExecutorService代替timer执行任务调度。Timer的缺陷：
    （1）Timer在执行定时任务时只会创建一个线程，所以如果存在多个任务，且任务时间过长，
	 超过了两个任务的间隔时间，会发生一些缺陷
    （2）如果TimerTask抛出RuntimeException，Timer会停止所有任务的运行
    （3）Timer执行周期任务时依赖系统时间，如果当前系统时间发生变化会出现一些执行上的变化
2、Curator代替原生ZooKeeper操作，Curator对ZooKeeper进行了一次包装，
对原生ZooKeeper的操作做了大量优化（Client和Server之间的连接可能出现的问题处理）
，可以进一步提高TBSchedule的高可用。数据节点的更新通过timer任务去扫描（太low），
改造为zookeeper事件监听机制来更新数据节点的数据
