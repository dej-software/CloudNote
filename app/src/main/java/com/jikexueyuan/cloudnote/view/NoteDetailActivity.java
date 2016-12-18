package com.jikexueyuan.cloudnote.view;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.jikexueyuan.cloudnote.R;
import com.jikexueyuan.cloudnote.bean.ExtendMovementMethod;
import com.jikexueyuan.cloudnote.bean.NoteInfo;
import com.jikexueyuan.cloudnote.db.NoteDB;
import com.jikexueyuan.cloudnote.util.DataUtils;
import com.jikexueyuan.cloudnote.util.PreferencesUtils;
import com.jikexueyuan.cloudnote.util.ServerUtils;
import com.jikexueyuan.cloudnote.util.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class NoteDetailActivity extends AppCompatActivity {

    private static final String TAG = "NoteDetailActivity";
    private static final int REQUEST_EDIT = 1000;
    private static String curResAddress;
    private static String curImgPath;

    private NoteInfo noteInfo;
    private NoteDB noteDB;

    private TextView tvTitle, tvContent;
    private Html.ImageGetter imageGetter;

    private int viewWidth;

    private String testStr = "图片1：\n" + "<img src=\"http://10.0.2.2/img/test1.jpg\">"
            + "\n图片2：\n" + "<img src=\"http://10.0.2.2/img/test2.jpg\">"
            + "\n结束\n" + "文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字" +
            "文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字" +
            "文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字" +
            "文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字" +
            "文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字" +
            "文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字" +
            "文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字" +
            "文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字" +
            "文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字" +
            "文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字" +
            "文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字" +
            "文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字" +
            "文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字" +
            "文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字" +
            "文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字" +
            "文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字文字";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_detail);

        initView();
    }

    private void initView() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tvTitle = (TextView) findViewById(R.id.tv_title);
        tvContent = (TextView) findViewById(R.id.tv_content);

        // 获取屏幕宽度 显示图片的最大宽度为0.9倍
        viewWidth = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
        System.out.println("viewWidth: " + viewWidth);

        init();

        // 设置可点击
        tvContent.setMovementMethod(ExtendMovementMethod.getInstance(handler, ImageSpan.class));

        // 获取传递过来的笔记并显示
        noteInfo = getIntent().getParcelableExtra("note");
        if (noteInfo != null) {
            tvTitle.setText(noteInfo.getTitle());
            // 换行符处理
            CharSequence cs = Html.fromHtml(noteInfo.getContent().replace("\n", "<br>"), imageGetter, null);
            tvContent.setText(cs);
        }

    }

    private void init() {

        if (PreferencesUtils.isLogin()) {
            noteDB = new NoteDB(this, PreferencesUtils.getUser());
        } else {
            noteDB = new NoteDB(this);
        }

        // 用户服务器对应的资源（图片、视频）目录
        if (PreferencesUtils.isLogin()) {
            curResAddress = ServerUtils.resDirAddress + PreferencesUtils.getUser() + "_res/";
        }

        if (PreferencesUtils.isLogin()) {
            curImgPath = StringUtils.resPath + PreferencesUtils.getUser() + "/";
        } else {
            curImgPath = StringUtils.resPath + "unlogin/";
        }

        File file = new File(curImgPath);
        if (!file.exists()) {
            if (file.mkdirs()) {
                Log.i(TAG, "目录创建成功");
            }
        }

        imageGetter = new Html.ImageGetter() {
            @Override
            public Drawable getDrawable(String source) {

                Log.i(TAG, "getDrawable " + source);
                String[] imgStr = source.split("/");
                String imgName = imgStr[imgStr.length - 1];

                Drawable drawable = null;

                Bitmap bitmap = BitmapFactory.decodeFile(curImgPath + imgName);

                int imgWidth = 0;
                int imgHeight = 0;

                if (bitmap != null) {
                    drawable = new BitmapDrawable(null, bitmap);
                    imgWidth = bitmap.getWidth();
                    imgHeight = bitmap.getHeight();
                    System.out.println(imgWidth + "  " + imgHeight);
                    // 对大于显示区域宽度的图片进行缩放显示
                    if (imgWidth > viewWidth) {
                        float scale = (float) viewWidth / imgWidth;
                        System.out.println(viewWidth + "  " + imgWidth + "  " + imgHeight + "  " + scale);
                        Matrix mx = new Matrix();
                        mx.setScale(scale, scale);
                        bitmap = Bitmap.createBitmap(bitmap, 0, 0, imgWidth, imgHeight, mx, true);
                        imgWidth = bitmap.getWidth();
                        imgHeight = bitmap.getHeight();
                    }
                }

                if (drawable != null) {
                    Log.i(TAG, "本地文件存在");
                    drawable.setBounds(0, 0, imgWidth, imgHeight);
                } else if (PreferencesUtils.isLogin()) {
                    Log.i(TAG, "本地文件不存在，从网络获取");
                    new ImageAsyncTask().execute(imgName);
                }

                return drawable;
            }
        };
    }

    /**
     * 处理图片点击事件
     */
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == ExtendMovementMethod.MESSAGE_WHAT) {
                Object obj = msg.obj;
                if (obj != null) {
                    if (obj instanceof ImageSpan) {
                        String fileName = ((ImageSpan) obj).getSource();
                        Log.d(TAG, "点击了：" + fileName);
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        if (fileName.contains(".mp4")) {
                            fileName = fileName.substring(0, fileName.length() - 4);
                            i.setDataAndType(Uri.fromFile(new File(curImgPath + fileName)), "video/*");
                        } else {
                            i.setDataAndType(Uri.fromFile(new File(curImgPath + fileName)), "image/*");
                        }
                        startActivity(i);
                    }
                }
            }
        }
    };

    /**
     * 点击编辑
     *
     * @param view
     */
    public void editOnClick(View view) {
        Log.i(TAG, "编辑当前文档");
        Intent i = new Intent(this, NoteEditActivity.class);
        Bundle b = new Bundle();

        if (noteInfo != null) {
            b.putParcelable("note", noteInfo);
            i.putExtras(b);
            startActivityForResult(i, REQUEST_EDIT);
        }
    }

    /**
     * 点击删除
     *
     * @param view
     */
    public void deleteOnClick(View view) {
        Log.i(TAG, "删除当前文档");
        new AlertDialog.Builder(this)
                .setTitle("删除笔记")
                .setMessage("确定删除此笔记？不可恢复！")
                .setNegativeButton("取消", null)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        System.out.println("id: " + noteInfo.getId());
                        if (PreferencesUtils.isLogin()) {
                            noteInfo.setDeleted(true);
                            noteDB.save(noteInfo);
                        } else {
                            DataUtils.deleteNoteFromDB(noteInfo, noteDB);
                        }

                        finish();
                    }
                }).show();
    }

    /**
     * 从服务器获取图片
     */
    class ImageAsyncTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            String imgPath = curImgPath + params[0];
            Log.i(TAG, imgPath);
            File imgFile = new File(imgPath);
            if (!imgFile.exists()) {
                try {
                    imgFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            FileOutputStream fos = null;
            try {
                URL url = new URL(curResAddress + params[0]);
                Bitmap bitmap = BitmapFactory.decodeStream(new FlushedInputStream(url.openStream()));

                fos = new FileOutputStream(imgFile);

                // 生成图片到本地
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);

                fos.flush();
                fos.close();
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return null;
            } catch (IOException e) {
                e.printStackTrace();
                // 出现异常处理
                imgFile.delete();
                if (fos != null) {
                    try {
                        fos.flush();
                        fos.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }

                return null;
            }

            return "@success";
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String s) {
            if ("@success".equals(s)) {
                Log.i(TAG, "onPostExecute 图片获取完成");
                CharSequence cs = Html.fromHtml(testStr, imageGetter, null);
                tvContent.setText(cs);
            } else {
                Log.i(TAG, "onPostExecute 图片获取失败");
            }

            super.onPostExecute(s);
        }
    }

    /**
     * 解决加载多张图片时出现错误 decoder->decode returned false
     */
    static class FlushedInputStream extends FilterInputStream {

        /**
         * Constructs a new {@code FilterInputStream} with the specified input
         * stream as source.
         * <p>
         * <p><strong>Warning:</strong> passing a null source creates an invalid
         * {@code FilterInputStream}, that fails on every method that is not
         * overridden. Subclasses should check for null in their constructors.
         *
         * @param in the input stream to filter reads on.
         */
        protected FlushedInputStream(InputStream in) {
            super(in);
        }

        @Override
        public long skip(long byteCount) throws IOException {

            long totalByteCount = 0;

            while (totalByteCount < byteCount) {
                long skipByteCount = in.skip(byteCount - totalByteCount);
                if (skipByteCount == 0) {
                    int b = read();
                    if (b < 0) {
                        break;
                    } else {
                        skipByteCount = 1;
                    }
                }

                totalByteCount += skipByteCount;
            }

            return totalByteCount;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Log.i(TAG, "返回");
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_EDIT) {
            Log.i(TAG, "编辑完成");
            if (data != null) {
                noteInfo = data.getParcelableExtra("note");
                if (noteInfo != null) {
                    tvTitle.setText(noteInfo.getTitle());
                    // 换行符处理
                    CharSequence cs = Html.fromHtml(noteInfo.getContent().replace("\n", "<br>"), imageGetter, null);
                    tvContent.setText(cs);
                }
            }
        }
    }
}
