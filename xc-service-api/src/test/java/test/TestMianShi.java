package test;

import org.assertj.core.util.DateUtil;
import org.junit.Test;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Scanner;
import java.util.TimeZone;

public class TestMianShi {
    public static void main(String[] args) throws ParseException {
        //jjxf();
        //llt();
        //test3();
        Date ss = getW3cTimeConvertString2Date("2019-09-05T09:56:02.000Z", "UTC");

        System.out.println(ss);
//        //List<Integer> list = new ArrayList<>();
//        Scanner sc = new Scanner(System.in);
//        System.out.println("请输入第一个数字");
//        int a = sc .nextInt();
//        System.out.println("请输入第二个数字");
//        int b = sc .nextInt();
//        System.out.println("请输入第三个数字");
//        int c = sc .nextInt();
//        System.out.println("请输入第四个数字");
//        int d = sc .nextInt();
//        System.out.println("请输入第五个数字");
//        int e = sc .nextInt();
//        System.out.println("请输入第六个数字");
//        int f = sc .nextInt();
//        int [] arr = {a,b,c,d,e,f};
//        paixu(arr);
//        System.out.println("从小到大顺序为:");
////        for (int i : arr) {
////            System.out.println("排序为:"+i);
////        }
//        for (int i = 0; i < arr.length; i++) {
//            System.out.println("第"+(i+1)+"数字为:"+arr[i]);
//        }
    }

    public static Date getW3cTimeConvertString2Date(String date,String timeZone) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.CHINESE);
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date parse = format.parse(date);
        return parse;
    }

    public static String dealDateFormat(String oldDate) {
        Date date1 = null;
        DateFormat df2 = null;
        try {
            oldDate= oldDate.replace("Z", " UTC");
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS Z");
            Date date = df.parse(oldDate);
            SimpleDateFormat df1 = new SimpleDateFormat ("EEE MMM dd HH:mm:ss Z yyyy", Locale.UK);
            date1 = df1.parse(date.toString());

            df2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return df2.format(date1);
    }

    /*
    public static void test3(){
        Date date = new Date("2019-09-05T09:56:02.000Z");
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timeStr = format.format(date);
        System.out.println("时间是:"+timeStr);
    }*/

    public static void test2(){
        BigDecimal bigDecimal = new BigDecimal(0.1);
        Object o = 0.1;
        String o1 = (String) o;
        System.out.println("结果是:"+o1);
        System.out.println("输入:"+bigDecimal+"元");
        BigDecimal bigDecimal1 = new BigDecimal("0.1");
        System.out.println("输入:"+bigDecimal1+"元");
    }

    public static void test1(){
        Long b = 4379L;
        String a = "12341564897654"+b;
        Long aLong = Long.valueOf("31941248085065032609794");
        System.out.println(aLong);
    }

    public static void test(){
        String num = "10";
        Double a = Double.valueOf(num);
        double l = a / 100;
        System.out.println(l);
    }
    //冒泡排序
    public static void paixu(int[] arr){
        for (int i = 0; i < arr.length; i++) {
            for (int j = 0;j<arr.length-i-1;j++){
                if (arr[j]>arr[j+1]){
                    int a = arr [j];
                    arr[j] = arr [j+1];
                    arr[j+1] = a;
                }
            }
        }
    }
    //99乘法表
    public static void jjxf(){
        System.out.println("生成99乘法表");
        for (int i = 1; i <= 9; i++) {
            System.out.println(" ");
            for (int j = 1; j <= i; j++) {
                System.out.print(j + " X " + i + " = " + (i * j)+"   ");
            }
        }
    }
    //输出测试
    public static void llt(){
        int i1 = 10;
        int i2 = 10;
        System.err.println("i1 + i2 = " + i1 + i2);
        //System.err.println("i1 - i2" + i1 - i2);
        System.err.println("i1 * i2 = " + i1 * i2);
        System.err.println("i1 / i2 = " + i1 / i2);
    }
}
