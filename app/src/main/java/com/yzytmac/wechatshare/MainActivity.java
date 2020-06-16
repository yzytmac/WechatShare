package com.yzytmac.wechatshare;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.modelmsg.WXWebpageObject;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private IWXAPI wxapi;
    private List<String> pkgs = new ArrayList<>();
    private List<String> appids = new ArrayList<>();
    private int mTargetScene = SendMessageToWX.Req.WXSceneSession;
    private static final int THUMB_SIZE = 150;

    private TextView tvTitle, tvContent, tvTail, tvLink;
    private ImageView ivImg;
    private Uri mInUri;
    private String mUrl = "https://yzytmac.github.io/";
    private Uri mOutUri;
    private Bitmap mBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setKey();
        initView();
    }

    private void initView() {
        tvTitle = findViewById(R.id.tv_title);
        tvContent = findViewById(R.id.tv_content);
        tvTail = findViewById(R.id.tv_tail);
        ivImg = findViewById(R.id.iv_img);
        tvLink = findViewById(R.id.tv_link);
    }


    private void setKey() {
        //腾讯新闻
        pkgs.add("com.tencent.news");
        appids.add("wx073f4a4daff0abe8");

        //uc
        pkgs.add("com.UCMobile");
        appids.add("wx020a535dccd46c11");

        //微博
        pkgs.add("com.sina.weibo");
        appids.add("wx299208e619de7026");

        //今日头条
        pkgs.add("com.ss.android.article.news");
        appids.add("wx50d801314d9eb858");

        //知乎
        pkgs.add("com.zhihu.android");
        appids.add("wxd3f6cb54399a8489");
    }

//    private void setBrowserKey() {
//        browser.put("oppo", "wxd7d1c11a5a7fa0df");
//        browser.put("xiaomi", "wxc87ca23cfe029db3");
//        browser.put("meizu", "wx7575781cdff09bb2");
//        browser.put("huawei", "wx7fb4374b90f45295");
//        browser.put("honor", "wx7fb4374b90f45295");
//    }


    public void share() {

        String title = tvTitle.getText().toString();
        String content = tvContent.getText().toString();
//        String img = mImgEt.getText().toString();

        if (TextUtils.isEmpty(title)) {
            Toast.makeText(MainActivity.this, "请设置标题", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(content)) {
            Toast.makeText(MainActivity.this, "请设置文案", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(mUrl)) {
            Toast.makeText(MainActivity.this, "请设置分享链接", Toast.LENGTH_SHORT).show();
            return;
        }
//        if (mInUri == null || TextUtils.isEmpty(mInUri.toString())) {
//            Toast.makeText(MainActivity.this, "请设置分享图片", Toast.LENGTH_SHORT).show();
//            return;
//        }
        if (tailIndex == -1) {
            Toast.makeText(MainActivity.this, "请设置分享小尾巴", Toast.LENGTH_SHORT).show();
            return;
        }

        WXWebpageObject webpage = new WXWebpageObject();
        webpage.webpageUrl = mUrl;
        final WXMediaMessage msg = new WXMediaMessage(webpage);
        msg.title = title;
        msg.description = content;
        if (mOutUri != null) {
            try {
                mBitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(mOutUri));
            } catch (IOException pE) {
                pE.printStackTrace();
            }
        }

        if (mBitmap == null) {
            mBitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.ic_launcher2);
        }

        Bitmap thumbBmp = Bitmap.createScaledBitmap(mBitmap, THUMB_SIZE, THUMB_SIZE, true);
        msg.thumbData = Util.bmpToByteArray(thumbBmp, true);
        mBitmap.recycle();
        mBitmap=null;
        thumbBmp.recycle();
        thumbBmp=null;


        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction("webpage");
        req.message = msg;
        req.scene = mTargetScene;
        wxapi.sendReq(req);
    }

    private String buildTransaction(final String type) {
        return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
    }


    public void selectImg() {
        //调用相册
        Intent intent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mOutUri = Uri.fromFile(new File("/sdcard/img_crop.jpg"));
        //获取图片路径
        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null) {
            mInUri = data.getData();
            cropImg(mInUri, mOutUri, 2);
        } else if (requestCode == 2 && resultCode == Activity.RESULT_OK && data != null) {
            //裁剪完成
            try {
                mBitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(mOutUri));
                ivImg.setImageBitmap(mBitmap);
            } catch (FileNotFoundException pE) {
                pE.printStackTrace();
            }
        }
    }

    public void look() {
        if (!TextUtils.isEmpty(mUrl)) {
            Intent intent = new Intent(this, WebActivity.class);
            intent.putExtra("web_url", mUrl);
            startActivity(intent);
        } else {
            Toast.makeText(MainActivity.this, "设置网页链接", Toast.LENGTH_SHORT).show();
        }
    }

    //调用系统截图
    private void cropImg(Uri inUri, Uri outUri, int requestCode) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        //可以选择图片类型，如果是*表明所有类型的图片
        intent.setDataAndType(inUri, "image/*");
        // 下面这个crop = true是设置在开启的Intent中设置显示的VIEW可裁剪
        intent.putExtra("crop", "true");
        // aspectX aspectY 是宽高的比例，这里设置的是正方形（长宽比为1:1）
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // outputX outputY 是裁剪图片宽高
        intent.putExtra("outputX", 1000);
        intent.putExtra("outputY", 1000);
        //裁剪时是否保留图片的比例，这里的比例是1:1
        intent.putExtra("scale", true);
        //是否是圆形裁剪区域，设置了也不一定有效
        //intent.putExtra("circleCrop", true);
        //设置输出的格式
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        //是否将数据保留在Bitmap中返回
        intent.putExtra("return-data", true);
        intent.putExtra("scaleUpIfNeeded", true);
        //输出路径
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outUri);
        //是否人脸识别
        intent.putExtra("noFaceDetection", true);
        startActivityForResult(intent, requestCode);
    }


    public void onClick(View pView) {
        switch (pView.getId()) {
            case R.id.tv_title:
                showInputDialog("请输入标题", tvTitle);
                break;
            case R.id.tv_content:
                showInputDialog("请输入文案", tvContent);
                break;
            case R.id.iv_img:
                selectImg();
                break;
            case R.id.tv_tail:
                selectTail();
                break;
            case R.id.ll_share:
                share();
                break;
            case R.id.ll_link:
                showInputDialog("设置网页链接", tvLink, true);
                break;
            case R.id.ll_internet:
                look();
                break;


            default:
        }
    }


    private void showInputDialog(String title, final TextView tv) {
        showInputDialog(title, tv, false);
    }

    private void showInputDialog(String title, final TextView tv, final boolean isLink) {
        final EditText editText = new EditText(MainActivity.this);
        if (isLink) {
            editText.setText(mUrl);
        } else {
            editText.setText(tv.getText());
        }
        AlertDialog.Builder vBuilder = new AlertDialog.Builder(MainActivity.this);
        vBuilder.setTitle(title).setView(editText);

        vBuilder.setNegativeButton("取消", null);
        vBuilder.setPositiveButton("确定", null);

        final AlertDialog vDialog = vBuilder.show();
        vDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String vTrim = editText.getText().toString().trim();
                if (TextUtils.isEmpty(vTrim)) {
                    Toast.makeText(MainActivity.this, "内容不能为空", Toast.LENGTH_SHORT).show();
                }else {
                    if (isLink) {
                        mUrl = vTrim;
                    } else {
                        tv.setText(vTrim);
                    }
                    if (TextUtils.isEmpty(mUrl)) {
                        tv.setTextColor(Color.BLACK);
                    } else {
                        tv.setHintTextColor(MainActivity.this.getResources().getColor(R.color.colorAccent));
                    }
                    vDialog.dismiss();
                }
            }
        });
    }


    private int tailIndex = -1;

    private void selectTail() {
        final String[] items = {"腾讯新闻", "UC浏览器", "新浪微博", "今日头条", "知乎"};
        final AlertDialog.Builder vBuilder = new AlertDialog.Builder(MainActivity.this);
        vBuilder.setTitle("请选择小尾巴(必须安装伪装的APP才能使用)");
        vBuilder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                tailIndex = which;
            }
        });

        vBuilder.setNegativeButton("取消", null);
        vBuilder.setPositiveButton("确定", null);

        final AlertDialog vDialog = vBuilder.show();
        vDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tailIndex == -1) {
                    Toast.makeText(MainActivity.this, "请选择小尾巴", Toast.LENGTH_SHORT).show();
                } else {
                    String appID = appids.get(tailIndex);
                    String pkg = pkgs.get(tailIndex);
                    boolean vB = Util.checkAppInstalled(MainActivity.this, pkg);
                    if (vB) {
                        tvTail.setText(items[tailIndex]);
                        vDialog.dismiss();
                        wxapi = WXAPIFactory.createWXAPI(new MyContextWrapper(MainActivity.this.getApplicationContext(), pkg), appID, true);
                        wxapi.registerApp(appID);
                    } else {
                        Toast.makeText(MainActivity.this, "安装该客户端后才能使用小尾巴!", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }
}
