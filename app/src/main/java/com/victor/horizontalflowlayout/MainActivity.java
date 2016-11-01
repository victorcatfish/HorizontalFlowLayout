package com.victor.horizontalflowlayout;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.widget.TextView;

import com.victor.horizontalflowlayoutlib.HorizontalFlowLayout;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    String[] data = {"QQ","视频","电子书","酒店","单机","小说","放开那三国","斗地主","优酷","网游","WIFI万能钥匙","播放器","捕鱼达人2","机票","游戏","熊出没之熊大快跑","美图秀秀","浏览器","单机游戏","我的世界","电影电视","QQ空间","旅游","免费游戏","2048","刀塔传奇","壁纸","节奏大师","锁屏","装机必备","天天动听","备份","网盘"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        HorizontalFlowLayout flowLayout = (HorizontalFlowLayout) findViewById(R.id.hfl_layout);

        // 采用直接new的方式进行设置
        //ScrollView scrollView = new ScrollView(this);
        //HorizontalFlowLayout flowLayout = new HorizontalFlowLayout(this);
        //scrollView.addView(flowLayout);
        flowLayout.setPadding(5,5,5,5);
        //flowLayout.setHorizontalSpace(15);

        Random random = new Random();

        for (int i =0; i< data.length; i++) {
            TextView textView = new TextView(this);
            textView.setText(data[i]);
            textView.setPadding(5,5,5,5);
            textView.setGravity(Gravity.CENTER);
            // 字体随机大小
            textView.setTextSize(random.nextInt(16) + 10);
            // 随机背景
            int r = random.nextInt(220) + 30;
            int g = random.nextInt(220) + 30;
            int b = random.nextInt(220) + 30;
            int rgb = Color.rgb(r, g, b);
            textView.setBackgroundColor(rgb);
            flowLayout.addView(textView);

        }

        //setContentView(scrollView);
    }
}
