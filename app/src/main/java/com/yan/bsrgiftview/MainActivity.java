package com.yan.bsrgiftview;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.yan.bsrgift.BSRGiftLayout;
import com.yan.bsrgift.BSRGiftView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "DownloadTask";
//    private String[] gifs = new String[12];
    private List<String> gifs = new ArrayList<String>();
    BSRGiftView imageButton;
    BSRGiftLayout giftLayout;
    GiftAnmManager giftAnmManager;
    int time = 0;
    private ZIP zip;

    private DownloadTask downloadTask;

    private DownloadListener mListener = new DownloadListener() {
        @Override
        public void onProgress(int progress) {
            Log.d(TAG, "onProgress 下载进度: " + progress);
        }

        @Override
        public void onSuccess() {
            downloadTask = null;
            Log.d(TAG, "onSucess: 下载成功");
            Toast.makeText(MainActivity.this, "下载成功", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onFailed() {
            downloadTask = null;
            Log.d(TAG, "onFailed: 下载失败");
        }

        @Override
        public void onPaused() {
            downloadTask = null;
            Log.d(TAG, "onPaused: 下载暂停");
        }

        @Override
        public void onCanceled() {
            downloadTask = null;
            Log.d(TAG, "onCanceled: 退出下载");
        }
    };
    /**
     * 下载礼物
     */
    private Button mDownGift;
    /**
     * 解压礼物
     */
    private Button mUnZipGift;
    private String downloadUrl;
    private String directory;
    private String fileName;
    private String cachepath;
    /**
     * 播放礼物
     */
    private Button mPlayGift;
    private String foldername;
    /**
     * 添加礼物
     */
    private Button mAddGift;
    private File giftFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();


        imageButton = (BSRGiftView) findViewById(R.id.gift_view);
        giftLayout = (BSRGiftLayout) findViewById(R.id.gift_layout);

        giftAnmManager = new GiftAnmManager(giftLayout, this);
        /*findViewById(R.id.btn2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (time++ % 8) {
                    case 0:
                        giftAnmManager.showKiss();
                        break;
                    case 1:
                        giftAnmManager.showCarTwo();
                        break;
                    case 2:
                        giftAnmManager.showDragon();
                        break;
                    case 3:
                        giftAnmManager.showKQ();
                        break;
                    case 4:
                        giftAnmManager.showCarOne();
                        break;
                    case 5:
                        giftAnmManager.showShip();
                        break;
                    case 6:
                        giftAnmManager.showCarOnePath();
                        break;
                    case 7:
                        giftAnmManager.showPositionInScreen();
                        break;

                }
            }
        });*/

        //动态权限判断和设置
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }

    private void initView() {
        mDownGift = (Button) findViewById(R.id.down_gift);
        mDownGift.setOnClickListener(this);
        mUnZipGift = (Button) findViewById(R.id.UnZip_gift);
        mUnZipGift.setOnClickListener(this);
        mPlayGift = (Button) findViewById(R.id.play_gift);
        mPlayGift.setOnClickListener(this);
        mAddGift = (Button) findViewById(R.id.add_gift);
        mAddGift.setOnClickListener(this);

        zip = new ZIP();
        downloadUrl = "http://static.huajiao.com/huajiao/gifteffect/90049_31.zip";
        fileName = downloadUrl.substring(downloadUrl.lastIndexOf("/"));

        foldername = fileName.substring(fileName.lastIndexOf("/"), fileName.lastIndexOf(".")); //提取文件名作为文件夹名字
        cachepath = MyApplication.getContext().getExternalCacheDir() + "/gifts"; //下载保存路径
        isExist(cachepath);//判断是否存在缓存文件夹
        giftFile = new File(cachepath + foldername);
        isExist(giftFile.toString());

    }

    /**
     * 判断文件夹是否存在，否则创建
     *
     * @param path
     */
    private static void isExist(String path) {
        File folder = new File(path);
        //判断文件夹是否存在,如果不存在则创建文件夹
        if (!folder.exists()) {
            folder.mkdir();
            Log.d(TAG, "创建文件夹");
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.down_gift:
                if (downloadTask == null) {
                    downloadTask = new DownloadTask(mListener);
                    downloadTask.execute(downloadUrl, cachepath);
                }
                break;
            case R.id.UnZip_gift:
                File file = new File(cachepath + fileName);
                try {
                    ZIP.UnZipFolder(file.toString(), cachepath+"/");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.play_gift:
                giftAnmManager.showGift(gifs);
                break;
            case R.id.add_gift:

                //文件遍历程序
                getAllFiles();
                break;
            default:
                break;
        }
    }

    // 遍历接收一个文件路径，然后把文件子目录中的所有文件遍历并输出来
    private void getAllFiles() {


        File files[] = giftFile.listFiles();

        if (files != null) {
            for (File f : files) {
                String fileString = f.toString();
                if (".png".equals(fileString.substring(fileString.lastIndexOf(".")))) {
                    gifs.add(fileString);
                }
            }
        }
        Toast.makeText(this, "遍历图片", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "拒绝权限将无法使用程序", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
