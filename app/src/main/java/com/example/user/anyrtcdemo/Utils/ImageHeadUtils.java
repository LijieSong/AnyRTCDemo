package com.example.user.anyrtcdemo.Utils;

import java.util.Random;

/**
 * 项目名称：AnyRTCDemo
 * 类描述：ImageHeadUtils 描述:
 * 创建人：songlijie
 * 创建时间：2016/11/23 13:02
 * 邮箱:814326663@qq.com
 */
public class ImageHeadUtils {
    public static String[] userVavatar = new String[]{"http://www.poluoluo.com/qq/UploadFiles_7828/201611/2016112121091926.jpg"
            ,"http://t1.ituba.cc/mm8/tupai/20150909/211904209.jpg"
            ,"http://img.wzfzl.cn/uploads/allimg/150617/co15061H10322-18.jpg"
            ,"http://awb.img.xmtbang.com/img/uploadnew/201510/27/0baa0ad0f27644a8883f2accc88fdfc3.jpg"
            ,"http://www.807958.com.cn/imgall/o53xoltroeytemzufzxxezy/uploads/allimg/150707/8_150707164831_9.jpg"
            ,"http://www.th7.cn/d/file/p/2016/11/23/e605895922fffff2fdefc77b4ef4773b.jpg"
            ,"http://www.th7.cn/d/file/p/2016/11/23/99de1c5606896b4062157c4e7927f2e8.jpg"
            ,"http://t2.27270.com/uploads/tu/201607/27/4gn4bzisblo.jpg"
            ,"http://www.poluoluo.com/qq/UploadFiles_7828/201611/2016112121091958.jpg"
            ,"http://www.qqbody.com/uploads/allimg/201411/18-200811_159.jpg"};

    public static String getVavatar() {
        return userVavatar[new Random().nextInt(10)];
    }
}
