package com.debug.kill.server.utils;

import org.joda.time.DateTime;

import java.text.SimpleDateFormat;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @Author:lhy
 * @Date: 2020/1/6 8:53
 * 传统方法生成随机id
 **/
public class RandomUtil {
    private static final SimpleDateFormat dateFormatOne = new SimpleDateFormat("yyyyMMddHHmmssSS");

    /**
     * 解决了Random类在多线程下多个线程竞争内部唯一的原子性种子变量而导致大量线程自旋重试的不足
     */
    private static final ThreadLocalRandom random = ThreadLocalRandom.current();

    /**
     * 生成订单编号-方式一
     *
     * @return
     */
    public static String generateOrderCode() {
        //TODO:时间戳+N为随机数流水号
        return dateFormatOne.format(DateTime.now().toDate()) + generateNumber(4);
    }

    public static String generateNumber(int num) {

        StringBuffer sb = new StringBuffer();
        for (int i = 1; i <= num; i++) {
            //该方法的作用是生成一个随机的int值，该值介于[0,n)的区间，也就是0到n之间的随机int值，包含0而不包含n
            sb.append(random.nextInt(9));
        }
        return sb.toString();
    }

    public static void main(String args[]) {
        for (int i = 1; i <= 1000; i++) {
            System.out.println(RandomUtil.generateOrderCode());
        }
    }
}
