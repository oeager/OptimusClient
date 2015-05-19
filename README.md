
# OptimusClient
这是一个基于Apache HttpClient实现的一个异步Http客户端。

特征： 
====
1.异步以回调实现http请求。 <br/>
2.使用线程池并发请求限制资源的使用。<br/>
3.支持Http的所有谓词。<br/>
4.封装请求参数，使调用更加容易。<br/>
5.可配置化的实现，全局配置与单次请求配置结合，让一切更可控。<br/>
6.支持Http缓存。<br/>

使用：
====
#####1.eclipse环境
下载jar文件[download the jar](https://github.com/oeager/optimusClient/raw/master/Download/OptimusClient_1.0.0.jar),添加到项目的libs目录下。<br/>
#####2.AndroidStudio
在项目的build.gradle中加入下面这句代码:
```Gradle
dependencies {
    compile 'com.bsince.optimus:app:1.0.0'
}
```

下面是简单的使用示例。<br/>
###1.配置
>>如果你不想使用默认的配置，你需要定义一个类，实现com.bsince.optimus.client.MetaAdapter接口，然后在applyOptions方法
进行配置。<br/>
```Java
public class ConfigAdapter implements MetaAdapter {
    @Override<br/>
    public void applyOptions(Context mContext, OptimusConfigBuilder builder) {
        builder.endPoint("http://192.168.1.226:8080/HttpService")
                .cookie(new PersistentCookieStore(mContext))//cookie配置
                .urlEncodeEnable(true)
                .connectionTimeOut(5000)//连接超时时间
                .isCache(true)//是否缓存
                .responseTimeOut(5000)//socketTimeOut
//                 .addcredentials(...) //添加凭证
//                .httpCacheDirectory(...)//http的缓存目录
//                .sslSocketFactory(MySSLSocketFactory.getFixedSocketFactory())
//                .allRequestHeader(...)//所有请求都有的请求头
//                .assisExecutor(...)//辅助线程池（常用于缓存）
//                .mainExecutor(...) //主线程池（用于网络请求）
//                .enableRedirects(...)//是否允许重定向
                //...
                //...更多的可配置项
                //...
                .userAgent("optmusclinet/1.0");
                }
}
```

###2.添加标签
>>完成以上配置后，你需要在你程序的Manifest.xml中添加<meta-data>标签，如下：<br/>
```Xml
<meta-data android:name="com.developer.bsince.sample.ConfigAdapter" 
            android:value="MetaAdapter"></meta-data>
 ```
  `注：如果使用了混淆，请在混淆文件中加入:`
-keep public class * implements com.bsince.optimus.client.MetaAdapter<br/>

>>如果你无需配置，上面两步都可以省略，optimusClient会给你一个默认的配置。<br/>

###3.Get请求示例：<br/>
```Java
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
 ```
###4.Post请求示例：
 ```Java
SimpleDataSet ds = new SimpleDataSet(Method.POST,"/habit");
 ds.put("user","张三");
 ds.put("habit","睡觉");
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
 ```
###5.文件上传：
```Java
MulitDataSet dataSet = new MulitDataSet(Method.POST,"/upload");
dataSet.put("param","xxx");
try {
    dataSet.put("file",new File(Environment.getExternalStorageDirectory(),"xiong.jpg"));
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
```
###6.加载图片
```Java
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
                event1.setCache(true);//是否使用缓存,若图片超过2M,请标为false
                event1.setTarget("image");//标记用于取消请求
                OptimusClient.get(this).postEvent(event1);
```
###7.取消请求
```Java
OptimusClient.get(this).cancelRequests("image");//取消某个标记的一组请求
OptimusClient.get(this).cancelAllRequests();//取消所有请求
```
`如果你在使用过程中发现什么问题，请与我联系:oeager@foxmail.com`


 
