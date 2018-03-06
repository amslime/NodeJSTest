package com.learn.wlnutter.nodejstest;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private static File file;//我们运行这个程序需要创建的文
    String filename = "server.js";

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
        System.loadLibrary("node");
    }

    //We just want one instance of node running in the background.
    public static boolean _startedNodeAlready=false;

    public File[] getFiles(String path){
        File file=new File(path);
        File[] files=file.listFiles();
        return files;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        System.out.println("Ext is" + Environment.getExternalStorageDirectory().getAbsolutePath());

        //把assets里面的文件copy到本地运行时存储空间中。以后可维护成一次性(first run copy, next run just check)
        file = new File(getApplicationContext().getFilesDir(), filename);
        File htm = new File(getApplicationContext().getFilesDir(), "index.html");
        /*writefile("var http = require('http'); " +
                "var versions_server = http.createServer( (request, response) => { " +
                "  response.end('Versions: ' + JSON.stringify(process.versions)); " +
                "}); " +
                "versions_server.listen(3000);");
*/
        CopyAssets(getApplicationContext(), "server.js", file.getAbsolutePath());
        CopyAssets(getApplicationContext(), "index.html", htm.getAbsolutePath());
        System.out.println(file.getAbsolutePath());
        String out = readfile();
        System.out.println("ad x " + out);

        if( !_startedNodeAlready ) {
            _startedNodeAlready=true;

            new Thread(new Runnable() {
                @Override
                public void run() {

                    startNodeWithArguments(new String[]{"node", file.getAbsolutePath()
                    });

                }
            }).start();
        }


        final Button buttonVersions = (Button) findViewById(R.id.button);
        final TextView textViewVersions = (TextView) findViewById(R.id.text);

        // 按钮显示response
        buttonVersions.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                //Network operations should be done in the background.
                new AsyncTask<Void,Void,String>() {
                    @Override
                    protected String doInBackground(Void... params) {
                        String nodeResponse="";
                        try {
                            URL localNodeServer = new URL("http://localhost:3000/index.html");
                            BufferedReader in = new BufferedReader(
                                    new InputStreamReader(localNodeServer.openStream()));
                            String inputLine;
                            while ((inputLine = in.readLine()) != null)
                                nodeResponse=nodeResponse+inputLine;
                            in.close();
                        } catch (Exception ex) {
                            nodeResponse=ex.toString();
                        }
                        return nodeResponse;
                    }
                    @Override
                    protected void onPostExecute(String result) {
                        textViewVersions.setText(result);
                    }
                }.execute();

            }
        });
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native Integer startNodeWithArguments(String[] arguments);


    private void writefile(String str) {
        if (!file.exists()) {
            try{
                System.out.println(file.getAbsolutePath());
              //  file.mkdir();//关键的一步，创建以上路径下的该文件夹
                file.createNewFile();
                System.out.println("create succ");
            }catch (Exception E)
            {
               System.out.println("create fail");
            }
        }
        if (file.exists()) {
            try {
            /*以流的形式打开该文件，并设置了保存模式*
            /总共有四种模式：
            /Context.MODE_PRIVATE 为默认操作模式，代表该文件是私有数据，只能被应用本身访问，在该模式下写入的内容会覆盖原文件的内容。
            /Context.MODE_APPEND 检查文件是否存在，存在就往文件追加内容，否则就创建新文件。
            /MODE_WORLD_READABLE 表示当前文件可以被其他应用读取。
            /MODE_WORLD_WRITEABLE 表示当前文件可以被其他应用写入
             */
                FileOutputStream outstream = openFileOutput(filename, MODE_PRIVATE);
                //将文字以字节的形式存储进文件
                outstream.write(str.getBytes());
                //关闭文件存储流
                outstream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("No file");
        }
    }

    private String readfile() {
        String content = null;
        if (file.exists()) {
            try {
                //以流的形式打开该文件
                FileInputStream instream = openFileInput(filename);
                //初始化一个字数组节流，等下将之前存入的数据已流的形式读取出来，再转成字符串即可
                ByteArrayOutputStream Byte = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int len = 0;
                while ((len = instream.read(buffer)) != -1) {
                    Byte.write(buffer, 0, len);
                }
                content = Byte.toString();
                //关闭流
                instream.close();
                //关闭字节数组流
                Byte.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("No file");
        }
        //返回从文件获得的数据
        return content;
    }


    public static void CopyAssets(Context context, String oldPath, String newPath) {
        try {
            String fileNames[] = context.getAssets().list(oldPath);// 获取assets目录下的所有文件及目录名
            if (fileNames.length > 0) {// 如果是目录
                File file = new File(newPath);
                file.mkdirs();// 如果文件夹不存在，则递归
                for (String fileName : fileNames) {
                    CopyAssets(context, oldPath + "/" + fileName, newPath + "/" + fileName);
                }
            } else {// 如果是文件
                InputStream is = context.getAssets().open(oldPath);
                FileOutputStream fos = new FileOutputStream(new File(newPath));
                byte[] buffer = new byte[1024];
                int byteCount = 0;
                while ((byteCount = is.read(buffer)) != -1) {// 循环从输入流读取
                    // buffer字节
                    fos.write(buffer, 0, byteCount);// 将读取的输入流写入到输出流
                }
                fos.flush();// 刷新缓冲区
                is.close();
                fos.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
