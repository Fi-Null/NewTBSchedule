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

import java.awt.*;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
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

    public static double sqrt(double num, double accuracy) {
        double low = 0;
        double last = 0.00;
        double high = num;
        double mid = (low + high) / 2;
        do {
            if (mid * mid < num) {
                low = mid;
            } else {
                high = mid;
            }

            last = mid;
            mid = (low + high) / 2;
        } while (Math.abs(mid - last) > accuracy);

        return mid;
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

    @Test
    public void testSqrt() {
        System.out.println(sqrt(3.5, 0));
    }

    public void swap(int[] a, int i, int j) {
        int t = a[i];
        a[i] = a[j];
        a[j] = t;
    }

    public int partition(int[] arr, int startIndex, int endIndex) {
        int small = startIndex - 1;
        for (int i = startIndex; i < endIndex; ++i) {
            if (arr[i] < arr[endIndex]) {
                swap(arr, i, ++small);
            }
        }

        swap(arr, ++small, endIndex);
        return small;

//        int small = startIndex - 1;
//        int base = (int) Math.random() % (endIndex - startIndex + 1) + startIndex;
//        int target = arr[base];
//        for (int i = startIndex; i <= endIndex; ++i) {
//            if(arr[i] < target) {
//                if (small + 1 != i) {
//                    swap(arr, ++small, i);
//                } else {
//                    //如果small、i相邻则不用交换
//                    small++;
//                }
//            }
//        }
//
//        int targetIndex = 0;
//        for (int i = startIndex; i <= endIndex; i++) {
//            if (arr[i] == target) {
//                targetIndex = i;
//                break;
//            }
//        }
//
//        swap(arr, ++small, targetIndex);
//        return small;
    }

    public int[] quickPartition(int[] arr, int startIndex, int endIndex) {
        int small = startIndex - 1, great = endIndex + 1;
        int base = (int) Math.random() % (endIndex - startIndex + 1) + startIndex;
        int target = arr[base];
        int i = startIndex;

        while (i <= great - 1) {
            if (arr[i] < target) {
                swap(arr, ++small, i++);
            } else if (arr[i] > target) {
                swap(arr, --great, i);
            } else {
                i++;
            }
        }
        return new int[] {small, great};
    }

    public void quickSort(int[] arr, int startIndex, int endIndex) {
        if (startIndex > endIndex) {
            return;
        }

        int index = partition(arr, startIndex, endIndex);
        //int[] range = quickPartition(arr, startIndex, endIndex);
        quickSort(arr, startIndex, index - 1);
        quickSort(arr, index + 1, endIndex);
    }


    public void merge(int[] arr, int startIndex, int midIndex, int endIndex) {
        int[] help = new int[arr.length];

        int L = startIndex, R = midIndex + 1, i = startIndex;
        while (L <= midIndex && R <= endIndex) { //只要没有指针没越界就逐次比较
            help[i++] = arr[L] < arr[R] ? arr[L++] : arr[R++];
        }
        while (L != midIndex + 1) {
            help[i++] = arr[L++];
        }
        while (R != endIndex + 1) {
            help[i++] = arr[R++];
        }
        for (i = startIndex; i <= endIndex; i++) {
            arr[i] = help[i];
        }

    }

    public void mergSort(int[] arr, int startIndex, int endIndex) {
        if (startIndex >= endIndex) {
            return;
        }

        //(endIndex+startIndex)/2可能会导致int溢出，下面求中位数的做法更安全
        int midIndex = startIndex + ((endIndex - startIndex) >> 1);
        mergSort(arr, startIndex, midIndex);        //对左半部分排序
        mergSort(arr, midIndex + 1, endIndex);      //对右半部分排序
        merge(arr, startIndex, midIndex, endIndex);  //使整体有序

    }

    public void buildMaxHeap(int[] arr) {
        for (int i = 0; i < arr.length; i++) {
            while(arr[i] > arr[(i - 1) / 2]) {
                swap(arr, i, (i - 1) / 2);
                i = (i - 1) / 2;
            }
        }
    }

    public void heapify(int[] arr, int index, int heapSize) {
        int leftChild = 2 * index + 1;
        while (leftChild < heapSize) {

            int greateOne = leftChild + 1 < heapSize && arr[leftChild] < arr[leftChild + 1]
                    ? leftChild + 1 : leftChild;

            if (arr[index] > arr[greateOne]) {
                break;
            }

            swap(arr, index, greateOne);
            index = greateOne;
            leftChild = 2 * index + 1;

        }
    }

    public void heapSort(int[] arr) {
        buildMaxHeap(arr);

        int heapSize = arr.length;
        swap(arr, 0, --heapSize);

        while (heapSize > 0) {
            heapify(arr, 0, heapSize);
            swap(arr, 0, --heapSize);
        }
    }

    @Test
    public void testSort() {
        int[] arr = {3,1,2,5,4,3};
        //quickSort(arr, 0, arr.length - 1);
        mergSort(arr, 0, arr.length - 1);

        //heapSort(arr);
        System.out.println(Arrays.toString(arr));
    }
}