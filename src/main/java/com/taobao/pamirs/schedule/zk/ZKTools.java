package com.taobao.pamirs.schedule.zk;

import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.ACL;

import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ZKTools {

    public static void createPath(CuratorFramework zk, String path, CreateMode createMode, List<ACL> acl) throws Exception {
        String[] list = path.split("/");
        String zkPath = "";
        for (String str : list) {
            if (zk.checkExists().forPath(path) == null) {
                zkPath = zkPath + "/" + str;
                if (zk.checkExists().forPath(zkPath) == null) {

                    zk.create()
                            .creatingParentsIfNeeded()
                            .withMode(createMode)
                            .withACL(acl)
                            .forPath(zkPath, null);
                }
            }
        }
    }

    public static void printTree(CuratorFramework zk, String path, Writer writer, String lineSplitChar) throws Exception {
        String[] list = getTree(zk, path);

        for (String name : list) {

            String value = new String(zk.getData().forPath(path));

            if (value == null) {
                writer.write(name + lineSplitChar);
            } else {
                writer.write("[" + new String(value) + "]" + lineSplitChar);
            }
        }
    }

    public static void deleteTree(CuratorFramework zk, String path) throws Exception {
        String[] list = getTree(zk, path);
        for (int i = list.length - 1; i >= 0; i--) {
            zk.delete().forPath(list[i]);
        }
    }


    public static String[] getTree(CuratorFramework zk, String path) throws Exception {
        if (zk.checkExists().forPath(path) == null) {
            return new String[0];
        }
        List<String> dealList = new ArrayList<String>();
        dealList.add(path);
        int index = 0;
        while (index < dealList.size()) {
            String tempPath = dealList.get(index);
            List<String> children = zk.getChildren().forPath(tempPath);
            if (!"/".equalsIgnoreCase(tempPath)) {
                tempPath = tempPath + "/";
            }
            Collections.sort(children);
            for (int i = children.size() - 1; i >= 0; i--) {
                dealList.add(index + 1, tempPath + children.get(i));
            }
            index++;
        }
        return dealList.toArray(new String[0]);
    }
}
