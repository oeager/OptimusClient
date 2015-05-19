package com.developer.bsince.sample;

import android.graphics.Bitmap;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bsince.optimus.callback.BaseProcessor;
import com.bsince.optimus.callback.Processor;
import com.bsince.optimus.client.OptimusClient;
import com.bsince.optimus.client.req.Method;
import com.bsince.optimus.data.MulitDataSet;
import com.bsince.optimus.data.SimpleDataSet;
import com.bsince.optimus.event.ImageEvent;
import com.bsince.optimus.event.SimpleEvent;
import com.bsince.optimus.event.TextEvent;

import java.io.File;
import java.io.FileNotFoundException;


public class MainActivity extends ActionBarActivity {

    private EditText mEditText;
    private ImageView view;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mEditText = (EditText) findViewById(R.id.text);
        view = (ImageView) findViewById(R.id.imageshow);
        mEditText.setText(Environment.getExternalStorageDirectory().getAbsolutePath());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public void onAction(View v){
        switch (v.getId()){
            case R.id.get:

               TextEvent event= new TextEvent("http://www.baidu.com", new BaseProcessor<String>() {
                    @Override
                    public void onSuccess(int statusCode, String s) {
                        mEditText.setText(s);
                    }

                    @Override
                    public void onFail(int statusCode, Exception e) {
                        mEditText.setText(statusCode+":"+e.toString());
                    }
                }) ;
                OptimusClient.get(this).postEvent(event);
                break;
            case R.id.post:
                SimpleDataSet ds = new SimpleDataSet(Method.POST,"/habit");
                ds.put("user","张三");
                ds.put("habit","吃饭,睡觉");
                SimpleEvent<JavaBean> event2= new SimpleEvent<JavaBean>(ds, new BaseProcessor<JavaBean>() {
                    @Override
                    public void onSuccess(int statusCode, JavaBean s) {
                        mEditText.setText(s.value);
                    }

                    @Override
                    public void onFail(int statusCode, Exception e) {
                        mEditText.setText(statusCode+":"+e.toString());
                    }
                }) {
                    @Override
                    public JavaBean parseToJavaBean(String data) throws Exception {
                        //parse to your own jave bean
                        JavaBean bean = new JavaBean();
                        bean.value  = data;
                        return bean;
                    }
                };
                OptimusClient.get(this).postEvent(event2);
                break;
            case R.id.put:
                    MulitDataSet dataSet = new MulitDataSet(Method.POST,"/upload");
                            dataSet.put("file","xxx");
                try {
                    dataSet.put("f",new File(Environment.getExternalStorageDirectory(),"xiong.jpg"));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    mEditText.setText("文件不存在");
                    return;
                }
                TextEvent te= new TextEvent(dataSet, new BaseProcessor<String>() {
                    @Override
                    public void onSuccess(int statusCode, String s) {
                        mEditText.setText("上传成功:"+s);
                    }

                    @Override
                    public void onFail(int statusCode, Exception e) {
                        mEditText.setText("上传失败："+statusCode+":"+e.toString());
                    }
                });
                OptimusClient.get(this).postEvent(te);
                break;
            case R.id.image:
                ImageEvent event1 = new ImageEvent("http://pica.nipic.com/2007-11-09/2007119124513598_2.jpg", new BaseProcessor<Bitmap>() {
                    @Override
                    public void onSuccess(int statusCode, Bitmap bitmap) {
                        view.setImageBitmap(bitmap);
                    }

                    @Override
                    public void onFail(int statusCode, Exception e) {
                        Toast.makeText(MainActivity.this,"图片下载失败",Toast.LENGTH_SHORT).show();
                    }
                },0,0, Bitmap.Config.RGB_565);
                event1.setCache(false);
                event1.setTarget("image");//标记用于取消请求
                OptimusClient.get(this).postEvent(event1);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        OptimusClient.get(this).cancelAllRequests();;
        OptimusClient.get(this).cancelRequests("image");
    }

    class  JavaBean{
        String value;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
