package com.nssc.zookeeper.axiom;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooDefs.Ids;

public class CreateGroup implements Watcher{
	private static final int SESSION_TIMEOUT=5000;
	
	private ZooKeeper zk;
	//等待其他任务执行，当计数器为0时程序继续
	private CountDownLatch counectedSignal = new CountDownLatch(1);
	
	/**
	 * 初始化链接
	 * @param hosts zookeeper的主机名或地址
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void connect(String hosts) throws IOException, InterruptedException {
		/**
		 * function:维护客户端与zookeeper服务之间的链接
		 * connectString:zookeeper的主机名或地址
		 * sessionTimeout：链接超时时间
		 * watcher:监听对象，以获得事件的通知
		 */
		zk = new ZooKeeper(hosts, SESSION_TIMEOUT, this);
		//开始等待
		counectedSignal.await();
	}

	@Override
	public void process(WatchedEvent event) {
		//判断客户端是否链接
		if (event.getState() == KeeperState.SyncConnected) {
		counectedSignal.countDown();
		}
	}
	/**
	 * @param groupName 指定znode的名称
	 * @throws KeeperException
	 * @throws InterruptedException
	 */
	public void create(String groupName) throws KeeperException, InterruptedException {
		String path = "/"+groupName;
		//path
		//data znode内容
		//ALC 访问控制表
		//CreateMode znode创建模式
		String createdPath=zk.create(path, null/*data*/, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
		System.out.println("Create"+createdPath);
	}
	/**
	 * 封装的退出
	 * @throws InterruptedException
	 */
	public void close() throws InterruptedException{
		zk.close();
	}
	public static void main(String[] args) throws Exception, InterruptedException {
		CreateGroup createGroup = new CreateGroup();
		createGroup.connect(args[0]);
		createGroup.create(args[1]);
		createGroup.close();
	}

}
