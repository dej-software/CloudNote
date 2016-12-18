package com.jikexueyuan.cloudnote.view;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.jikexueyuan.cloudnote.R;
import com.jikexueyuan.cloudnote.bean.NoteInfo;
import com.jikexueyuan.cloudnote.db.NoteDB;
import com.jikexueyuan.cloudnote.util.FileUtils;
import com.jikexueyuan.cloudnote.util.PreferencesUtils;
import com.jikexueyuan.cloudnote.util.ProgressDialogUtils;
import com.jikexueyuan.cloudnote.util.StringUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class NoteEditActivity extends AppCompatActivity implements View.OnFocusChangeListener, View.OnClickListener {

    private static final String TAG = "NoteEditActivity";
    private static final int PICK_IMG = 1001;
    private static final int CAPTURE_IMG = 1002;
    private static final int PICK_VIDEO = 1003;
    private static final int CAPTURE_VIDEO = 1004;

    private static String curImgPath;

    private LinearLayout footer;
    private EditText editTitle, editContent;
    private int editWidth;

    private NoteInfo editNote;
    private NoteDB noteDB;
    // 是否是新建笔记
    private boolean isNewNote;
    private boolean isFirstLoad;
    // 记录编辑之前笔记的图片
    private Elements oldNoteImgElements;

    private String idDateStr, dateStr, timeStr;
    // 插入的是图片还是视频
    private boolean isImage;
    private String insertImgName;
    private String tmpImgPath, tmpVideoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_edit);

        initView();
    }

    private void initView() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        footer = (LinearLayout) findViewById(R.id.viewer_footer_bar);

        editTitle = (EditText) findViewById(R.id.edit_title);
        editContent = (EditText) findViewById(R.id.edit_content);

        editTitle.clearFocus();
        editContent.requestFocus();
        editTitle.setOnFocusChangeListener(this);
        editContent.setOnFocusChangeListener(this);


        editContent.setOnClickListener(this);

        if (PreferencesUtils.isLogin()) {
            noteDB = new NoteDB(this, PreferencesUtils.getUser());
        } else {
            noteDB = new NoteDB(this);
        }

        // 初始化用户的资源目录
        if (PreferencesUtils.isLogin()) {
            curImgPath = StringUtils.resPath + PreferencesUtils.getUser() + "/";
        } else {
            curImgPath = StringUtils.resPath + "unlogin/";
        }

        File file = new File(curImgPath);
        if (!file.exists()) {
            if (file.mkdirs()) {
                Log.i(TAG, "目录创建成功");
            } else {
                Log.i(TAG, "目录创建失败");
            }
        }

        File tmpDir = new File(StringUtils.tmpPath);
        if (!tmpDir.exists()) {
            tmpDir.mkdirs();
        }

        tmpImgPath = StringUtils.tmpPath + "tmp.jpg";
        tmpVideoPath = StringUtils.tmpPath + "tmp.mp4";

        // 记录笔记创建的相关时间
        idDateStr = StringUtils.getDateTime("yyyyMMddHHmmss");
        dateStr = StringUtils.getDateTime("yyyy-MM-dd");
        timeStr = StringUtils.getDateTime("HH:mm");

        editWidth = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);

        // 获取传递过来的笔记并编辑
        editNote = getIntent().getParcelableExtra("note");
        if (editNote != null) {
            isNewNote = false;
            // 初始化一下要编辑的旧笔记
            initEditText();
        } else {
            isNewNote = true;
            isFirstLoad = false;
            editNote = new NoteInfo();
        }
    }

    /**
     * 如果是修改旧笔记 初始化
     */
    private void initEditText() {

        // 先关闭软键盘
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editContent.getWindowToken(), 0);
        // 是第一次加载
        isFirstLoad = true;

        // 原笔记标题
        editTitle.setText(editNote.getTitle());
        // 把原笔记信息赋到编辑框内

        String noteContent = editNote.getContent();
        String[] strs = noteContent.split("<img");
        int cLength = strs.length;
        // 没有图片内容
        if (cLength <= 1) {
            editContent.setText(noteContent);
            isFirstLoad = false;
            return;
        }

        // 有图片内容 解析文本
        cLength = (cLength - 1) * 2 + 1;
        String[] contents = new String[cLength];
        contents[0] = strs[0];

        for (int i = 1, j = 1; i < strs.length; i++) {
            String[] childStrs = strs[i].split("/>");
            if (childStrs.length == 2) {
                contents[j++] = "<img" + childStrs[0] + "/>";
                contents[j++] = childStrs[1];
            } else {
                contents[j++] = "<img" + childStrs[0] + "/>";
                cLength = cLength - 1;
            }
        }

        System.out.println("contents: ");
        for (String s : contents) {
            System.out.println("s: " + s);
        }
        System.out.println("end");

        // 解析图片相关的信息
        Document doc = Jsoup.parse(editNote.getContent());
        oldNoteImgElements = doc.select("img");

        editContent.setText(contents[0]);
        editContent.setSelection(contents[0].length());
        for (int i = 1, j = 0; i < cLength; i++) {
            System.out.println(i + " " + contents[i]);
            if (contents[i].indexOf("<img") == 0) {
                String srcName = oldNoteImgElements.get(j++).attr("src");
                Bitmap bitmap = BitmapFactory.decodeFile(curImgPath + srcName);
                if (bitmap == null) {
                    bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.image_not_exist);
                }
                if (bitmap != null) {
                    SpannableString ss = getBitmapSS(bitmap, srcName);
                    insertImageToEditText(ss);
                }
            } else {
                int start = editContent.getSelectionStart();
                editContent.append(contents[i]);
                editContent.setSelection(start + contents[i].length());
            }
        }

        // 加载完成后是编辑模式 加载和编辑的insertImageToEditText处理不太一样
        isFirstLoad = false;
    }

    /**
     * 对应布局资源的点击事件处理
     *
     * @param view
     */
    public void noteEditOnClick(View view) {
        switch (view.getId()) {
            case R.id.actionbar_complete:
                saveNote();
                break;
            case R.id.insert_local:
                Log.i(TAG, "插入本地");
                insertLocal();
                break;
            case R.id.insert_camera:
                Log.i(TAG, "使用相机");
                insertCamera();
                break;
        }
    }

    /**
     * 保存笔记
     */
    private void saveNote() {

        String title = editTitle.getText().toString().trim();
        String content = editContent.getText().toString();

        System.out.println("save note: \n" + content);
        if (!isNewNote && content.equals(editNote.getContent())) {
            Log.i(TAG, "未作任何更改");
            return;
        }
        // 异步保存笔记
        new SaveNoteAsyncTask(this, title, content).execute((Void) null);
    }

    /**
     * 获取本地资源
     */
    private void insertLocal() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("获取");
        final String[] choices = {"图片", "视频"};

        final Intent i = new Intent(Intent.ACTION_GET_CONTENT);
//        final Intent i = new Intent(Intent.ACTION_PICK);

        builder.setItems(choices, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    i.setType("image/*");
                    startActivityForResult(i, PICK_IMG);
                } else if (which == 1) {
                    i.setType("video/*");

                    // 代替上面的方式
                    Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                    i.setData(uri);

                    startActivityForResult(i, PICK_VIDEO);
                }
            }
        });

        builder.show();
    }

    /**
     * 获取相机资源
     */
    private void insertCamera() {
        // 把相机获取的资源临时存储
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("相机");
        final String[] choices = {"拍照", "录像"};

        final File file = new File(StringUtils.tmpPath);
        if (!file.exists()) {
            file.mkdirs();
        }

        final Intent i = new Intent();
        builder.setItems(choices, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    i.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
                    i.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(tmpImgPath)));
                    startActivityForResult(i, CAPTURE_IMG);
                } else if (which == 1) {
                    i.setAction(MediaStore.ACTION_VIDEO_CAPTURE);
                    i.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(tmpVideoPath)));
                    startActivityForResult(i, CAPTURE_VIDEO);
                }
            }
        });

        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "onActivityResult requestCode : " + requestCode);
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {

            Bitmap bitmap = null;
            File tmpImg = new File(tmpImgPath);

            switch (requestCode) {
                case PICK_IMG:
                    isImage = true;
                    if (data == null) {
                        return;
                    }
                    Uri uri = data.getData();
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    break;
                case PICK_VIDEO:
                    isImage = false;
                    if (data == null) {
                        return;
                    }
                    Uri videoUri = data.getData();
                    Cursor cursor = getContentResolver().query(videoUri, null, null, null, null);
                    if (cursor == null) {
                        Toast.makeText(this, "视频路径获取错误，请使用系统自带文件浏览尝试", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (cursor != null) {
                        cursor.moveToFirst();
                        String videoPath = cursor.getString(1);
                        System.out.println(cursor.toString() + " " + videoPath);
                        if (new File(videoPath).exists()) {
                            System.out.println("videoPath: " + videoPath);
                            FileUtils.copyFile(videoPath, StringUtils.tmpPath, "tmp.mp4");
                            bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.video_play);
                        } else {
                            Toast.makeText(this, "视频路径获取错误，请使用系统自带文件浏览尝试", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                    break;
                case CAPTURE_IMG:
                    isImage = true;
                    // 从转存的临时图片获取没被压缩的图片
                    if (tmpImg.exists()) {
                        bitmap = BitmapFactory.decodeFile(tmpImgPath);
                    }
                    break;
                case CAPTURE_VIDEO:
                    isImage = false;
                    // 后台处理选取的视频 在文本中插入视频图标
                    if (new File(tmpVideoPath).exists()) {
                        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.video_play);
                    }
                    break;
            }

            if (bitmap != null) {
                initImageName();
                if (requestCode == CAPTURE_IMG) {
                    // 拍照的图片比较大 从bitmap转存到本地比较慢 直接把临时图片复制好了
                    FileUtils.copyFile(tmpImgPath, StringUtils.tmpPath, insertImgName);
                } else {
                    copyImageToTmp(bitmap);
                }
                SpannableString ss = getBitmapSS(bitmap, insertImgName);
                insertImageToEditText(ss);
            }
        }
    }

    /**
     * 设置要插入的图片名
     */
    private void initImageName() {
        // 设置要插入的图片名 随机数加时间
        int randNum = (int) (Math.random() * 100);
        String fileExtend;
        if (isImage) {
            fileExtend = ".png";
        } else {
            fileExtend = ".mp4.png";
        }

        if (randNum < 10) {
            insertImgName = "0" + randNum + StringUtils.getDateTime("yyyyMMddHHmmss") + fileExtend;
        } else {
            insertImgName = randNum + StringUtils.getDateTime("yyyyMMddHHmmss") + fileExtend;
        }
    }

    /**
     * 拷贝选择的图片到临时目录
     *
     * @param bitmap
     */
    private void copyImageToTmp(final Bitmap bitmap) {

        File tmpDir = new File(StringUtils.tmpPath);
        if (!tmpDir.exists()) {
            tmpDir.mkdirs();
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                String imgPath = StringUtils.tmpPath + insertImgName;
                Log.i(TAG, imgPath);
                File imgFile = new File(imgPath);
                if (!imgFile.exists()) {
                    try {
                        imgFile.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(imgFile);
                    // 生成图片到本地 格式为PNG 压缩率为100：不压缩
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);

                    fos.flush();
                    fos.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (fos != null) {
                        try {
                            fos.flush();
                            fos.close();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                }

                // 重命名视频文件
                if (!isImage) {
                    FileUtils.renameFile(StringUtils.tmpPath, "tmp.mp4", insertImgName.substring(0, insertImgName.length() - 4));
                }

                // 复制结束后把临时图片删掉
                File tmpImg = new File(tmpImgPath);
                if (tmpImg.exists()) {
                    tmpImg.delete();
                }
            }
        }).start();
    }

    /**
     * 把SpannableString放入EditText中
     *
     * @param ss
     */
    private void insertImageToEditText(SpannableString ss) {
        // 获取已输入内容
        Editable editable = editContent.getText();
        // 插入新内容
        int start = editContent.getSelectionStart();
        System.out.println("start : " + start);

        // 插入图片之前如果有文字 先换行
        if (isNewNote || !isFirstLoad) {
            if (start > 0) {
                editable.insert(start, "\n");
                start += 1;
            }
        }

        editable.insert(start, ss);
        // 插入图片之后加个换行
        if (isNewNote || !isFirstLoad) {
            editable.insert(start + ss.length(), "\n");
        }
        // 设置新内容和光标位置
        editContent.setText(editable);
        if (isNewNote || !isFirstLoad) {
            editContent.setSelection(start + 1 + ss.length());
        } else {
            editContent.setSelection(start + ss.length());
        }
    }

    /**
     * 获取可以在EditText中显示的图片(SpannableString)
     */
    private SpannableString getBitmapSS(Bitmap bitmap, String srcName) {
        int imgWidth = bitmap.getWidth();
        int imgHeight = bitmap.getHeight();

        // 对大于编辑区域宽度的图片进行缩放显示
        if (imgWidth > editWidth) {
            float scale = (float) editWidth / imgWidth;
            Matrix mx = new Matrix();
            System.out.println(scale);
            mx.setScale(scale, scale);

            System.out.println(imgWidth + " : " + imgHeight);
            System.out.println((int) (imgWidth * scale) + " : " + (int) (imgHeight * scale));
            // 处理可能会出现内存溢出
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, imgWidth, imgHeight, mx, true);
        }

        String src = "<img src=\"" + srcName + "\" />";
        SpannableString ss = new SpannableString(src);
        ImageSpan span = new ImageSpan(this, bitmap);
        ss.setSpan(span, 0, src.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        return ss;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FileUtils.deleteDirAllFile(new File(StringUtils.tmpPath));
    }

    /**
     * 焦点改变时处理
     *
     * @param v
     * @param hasFocus
     */
    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        Log.i(TAG, "onFocusChange");
        if (hasFocus) {
            switch (v.getId()) {
                case R.id.edit_title:
                    footer.setVisibility(View.GONE);
                    break;
                case R.id.edit_content:
                    footer.setVisibility(View.VISIBLE);
                    break;
            }
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
    public void onClick(View v) {
        // 编辑区域的点击处理
        if (v.getId() == R.id.edit_content) {
            int selectStart = editContent.getSelectionStart();
            String contentStr = editContent.getText().toString();

            // 两张图片中间无字符时: /><img 点击图片光标会定位错误到前一张图中，导致输入文字插入到了 <img 标签中
            if (selectStart + 5 < contentStr.length()) {
                if ('>' == contentStr.charAt(selectStart) && "/><img".equals(contentStr.substring(selectStart - 1, selectStart + 5))) {
                    System.out.println("图片相邻了");
                    // 把光标加1
                    editContent.setSelection(selectStart + 1);
                }
            }
            // TODO 点击时光标处理 暂未发现其他异常
        }
    }

    public class SaveNoteAsyncTask extends AsyncTask<Void, Void, Boolean> {

        private Context context;
        private String title, content;

        public SaveNoteAsyncTask(Context context, String title, String content) {
            this.context = context;
            this.title = title;
            this.content = content;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            Log.i(TAG, "完成编辑,保存笔记");

            if (editNote.getId() == 0) {
                editNote.setId(Long.parseLong(idDateStr));
            }

            editNote.setDate(dateStr);
            editNote.setTime(timeStr);
            editNote.setSynced(false);
            editNote.setDeleted(false);

            if ("".equals(title) && "".equals(content)) {
                Log.i(TAG, "笔记无输入");
                return false;
            }

            // 未输入标题 使用固定格式制作一个
            if ("".equals(title)) {
                title = "无标题(" + dateStr + " " + timeStr + ")";
            }

            // 解析图片相关的信息 从临时目录转存到资源文件夹下
            Document doc = Jsoup.parse(content);
            Elements newElements = doc.select("img");
            for (Element element : newElements) {
                String fileName = element.attr("src");
                // 旧笔记修改模式 如果资源是旧资源 跳出
                if (!isNewNote) {
                    if (isSrcExist(oldNoteImgElements, fileName)) {
                        continue;
                    }
                }

                System.out.println(fileName);
                FileUtils.moveFile(StringUtils.tmpPath + fileName, curImgPath);
                if (fileName.contains(".mp4")) {
                    FileUtils.moveFile(StringUtils.tmpPath + fileName.substring(0, fileName.length() - 4), curImgPath);
                }
            }

            // 旧笔记修改模式 要把被删除的资源内容从资源文件文件夹下移除
            if (!isNewNote && oldNoteImgElements != null) {
                for (Element element : oldNoteImgElements) {
                    String fileName = element.attr("src");
                    //  如果此资源在新资源列表中不存在 删除
                    if (!isSrcExist(newElements, fileName)) {
                        FileUtils.deleteFile(new File(curImgPath + fileName));
                        if (fileName.contains(".mp4")) {
                            FileUtils.deleteFile(new File(curImgPath + fileName.substring(0, fileName.length() - 4)));
                        }
                    }
                }
            }

            editNote.setTitle(title);
            // TODO BUG: 为了显示及编辑正常，相邻图片间加入换行，这里if语句未做全面的判定（比如 /><img 有可能是用户输入的信息）
            if (content.contains("/><img")) {
                StringBuilder builder = new StringBuilder(content);
                builder.insert(content.indexOf("/><img") + 2, "\n");
                content = builder.toString();
            }
            editNote.setContent(content);

            noteDB.save(editNote);

            return true;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ProgressDialogUtils.showProgressDialog(context, "", "正在保存");
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);

            ProgressDialogUtils.hideProgressDialog();

            if (aBoolean) {
                Intent i = new Intent(context, NoteDetailActivity.class);
                Bundle b = new Bundle();

                if (editNote != null) {
                    b.putParcelable("note", editNote);
                    i.putExtras(b);
                    setResult(RESULT_OK, i);
                }
                finish();
            } else {
                Toast.makeText(context, "笔记为空", Toast.LENGTH_SHORT).show();
            }
        }

    }

    /**
     * 判断src是否已存在elements中
     *
     * @param elements
     * @param src
     * @return
     */
    private boolean isSrcExist(Elements elements, String src) {
        for (Element element : elements) {
            if (src.equals(element.attr("src"))) {
                return true;
            }
        }

        return false;
    }
}
