package com.taobao.pamirs.schedule.test;

import com.taobao.pamirs.schedule.zk.ZKTools;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.apache.zookeeper.server.auth.DigestAuthenticationProvider;
import org.junit.Test;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

public class ZookeeperTest {

    public CuratorFramework createZk() {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(
                2000,
                5,
                3000
        );

        // 默认用户名：root 密码：root
        String authString = "ScheduleAdmin:password";


        CuratorFramework zk = CuratorFrameworkFactory.builder()
                .connectString("localhost:2181")
                .retryPolicy(retryPolicy)
                .authorization("digest", authString.getBytes())
                .build();

        zk.start();

        return zk;
    }

    @Test
    public void testCloseStatus() throws Exception {
        CuratorFramework zk = createZk();
        int i = 1;
        while (true) {
            try {
                StringWriter writer = new StringWriter();
                ZKTools.printTree(zk, "/zookeeper/quota", writer, "");
                System.out.println(i++ + "----" + writer.getBuffer().toString());
                Thread.sleep(2000);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    @Test
    public void testPrint() throws Exception {
        CuratorFramework zk = createZk();
        StringWriter writer = new StringWriter();
        ZKTools.printTree(zk, "/", writer, "\n");
        System.out.println(writer.getBuffer().toString());
    }

    @Test
    public void deletePath() throws Exception {
        CuratorFramework zk = createZk();

        ZKTools.deleteTree(zk, "/taobao-pamirs-com.taobao.pamirs.schedule");
        StringWriter writer = new StringWriter();
        ZKTools.printTree(zk, "/", writer, "\n");
        System.out.println(writer.getBuffer().toString());
    }

    @Test
    public void testACL() throws Exception {
        CuratorFramework zk = createZk();
        List<ACL> acls = new ArrayList<ACL>();
        acls.add(new ACL(ZooDefs.Perms.ALL, new Id("digest", DigestAuthenticationProvider.generateDigest("TestUser:password"))));
        acls.add(new ACL(ZooDefs.Perms.READ, Ids.ANYONE_ID_UNSAFE));

        ZKTools.createPath(zk, "/abc", CreateMode.PERSISTENT, acls);
    }
}