package com.lvsijian8.flowerpot.ui.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.lidroid.xutils.BitmapUtils;
import com.lvsijian8.flowerpot.R;
import com.lvsijian8.flowerpot.global.Const;
import com.lvsijian8.flowerpot.http.HttpHelper;
import com.lvsijian8.flowerpot.utils.BitmapHelper;
import com.lvsijian8.flowerpot.utils.UIUtils;

import java.util.HashMap;

public class AppendActivity extends AppCompatActivity implements View.OnClickListener {

    private AlertDialog dialog;
    private TextView tv_num;//显示当前的尺度
    private TextView tv_min;//显示尺度最小值
    private TextView tv_max;//显示尺度最大值
    private TextView tv_unit;//显示尺度单位
    private EditText et_flowername;//花的名称
    private Button bt_water_day, bt_water_time, bt_water_ml;//浇水的设置按钮
    private Button bt_bottle_day, bt_bottle_time, bt_bottle_ml;//施肥的设置按钮
    private String num_water_day = "15", num_water_time = "12", num_water_ml = "200";//决定浇水的数值
    private String num_bottle_day = "15", num_bottle_time = "12", num_bottle_ml = "200";//决定施肥的数值
    private int fid = -1;//植物的id
    private Button btn_sumbit;//提交
    private Button btn_reset;//取消
    private View view_seekbar;
    private SeekBar mseekBar;
    private static final int STATE_BOTTLE_DAY = 0;
    private static final int STATE_BOTTLE_TIME = 1;
    private static final int STATE_BOTTLE_ML = 2;
    private static final int STATE_WATER_DAY = 3;
    private static final int STATE_WATER_TIME = 4;
    private static final int STATE_WATER_ML = 5;
    private static int mCurrent = STATE_BOTTLE_DAY;
    private AlertDialog.Builder builder;
    private RelativeLayout layout_select;
    private ImageView iv_plant;//植物图片
    private TextView tv_c;//植物学名
    private TextView tv_e;//植物英文名
    private BitmapUtils bitmapUtils;
    private HttpHelper httpHelper;
    private Handler mhandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Const.APPEND_STATE_SUCCESS:
                    Toast.makeText(UIUtils.getContext(), "添加花盆成功", Toast.LENGTH_SHORT).show();
                    if (Const.APPEND_INTSTATE == Const.APPEND_REGISTER) {
                        //来自注册页面
                        startActivity(new Intent(AppendActivity.this, ContainActivity.class));
                        finish();//终结自己
                    } else if (Const.APPEND_INTSTATE == Const.APPEND_DATA) {
                        //来自消息页面
                        finish();
                    } else if (Const.APPEND_INTSTATE == Const.APPEND_POT) {
                        //来自设备页面
                        finish();
                    }
                    finish();
                    break;
                case Const.APPEND_STATE_FAIL:
                    Toast.makeText(UIUtils.getContext(), "添加花盆失败，请重试", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_append);
        getSupportActionBar().hide();
        initui();
        initdata();
        httpHelper = HttpHelper.getInstances();
        httpHelper.setOnDetailgetData(new HttpHelper.OnDetailgetData() {
            @Override
            public void successGet(String data) {
                if (!TextUtils.isEmpty(data)) {
                    String ek = data.toString().trim();
                    Message message = new Message();
                    if (ek.equals("success")) {
                        message.what = Const.APPEND_STATE_SUCCESS;
                    } else {
                        message.what = Const.APPEND_STATE_FAIL;
                    }
                    mhandler.sendMessage(message);
                }
            }

            @Override
            public void failGet() {

            }
        });
    }

    private void initui() {
        bitmapUtils = BitmapHelper.getBitmapUtils();
        view_seekbar = View.inflate(this, R.layout.layout_seekbar, null);
        mseekBar = (SeekBar) view_seekbar.findViewById(R.id.seekbar_SeekBar);
        tv_num = (TextView) view_seekbar.findViewById(R.id.tv_SeekBar_num);
        tv_min = (TextView) view_seekbar.findViewById(R.id.tv_SeekBar_min);
        tv_max = (TextView) view_seekbar.findViewById(R.id.tv_SeekBar_max);
        tv_unit = (TextView) view_seekbar.findViewById(R.id.tv_SeekBar_unit);
        et_flowername = (EditText) findViewById(R.id.ed_append_flowername);
        bt_bottle_day = (Button) findViewById(R.id.bt_bottle_day);
        bt_bottle_time = (Button) findViewById(R.id.bt_bottle_time);
        bt_bottle_ml = (Button) findViewById(R.id.bt_bottle_ml);
        bt_water_day = (Button) findViewById(R.id.bt_water_day);
        bt_water_time = (Button) findViewById(R.id.bt_water_time);
        bt_water_ml = (Button) findViewById(R.id.bt_water_ml);
        layout_select = (RelativeLayout) findViewById(R.id.rlayout_append_select);
        iv_plant = (ImageView) findViewById(R.id.iv_append_plant);
        tv_c = (TextView) findViewById(R.id.tv_append_namec);
        tv_e = (TextView) findViewById(R.id.tv_append_namee);
        btn_sumbit = (Button) findViewById(R.id.btn_append_sumbit);
        btn_reset = (Button) findViewById(R.id.btn_append_reset);

        builder = new AlertDialog.Builder(AppendActivity.this);
        builder.setView(view_seekbar);
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (mCurrent) {
                    case STATE_BOTTLE_DAY:
                        num_bottle_day = tv_num.getText().toString().trim();
                        break;
                    case STATE_BOTTLE_TIME:
                        num_bottle_time = tv_num.getText().toString().trim();
                        break;
                    case STATE_BOTTLE_ML:
                        num_bottle_ml = tv_num.getText().toString().trim();
                        break;
                    case STATE_WATER_DAY:
                        num_water_day = tv_num.getText().toString().trim();
                        break;
                    case STATE_WATER_TIME:
                        num_water_time = tv_num.getText().toString().trim();
                        break;
                    case STATE_WATER_ML:
                        num_water_ml = tv_num.getText().toString().trim();
                        break;
                }


            }
        });

    }

    private void initdata() {
        bt_bottle_day.setOnClickListener(this);
        bt_bottle_time.setOnClickListener(this);
        bt_bottle_ml.setOnClickListener(this);
        bt_water_day.setOnClickListener(this);
        bt_water_time.setOnClickListener(this);
        bt_water_ml.setOnClickListener(this);
        btn_reset.setOnClickListener(this);
        btn_sumbit.setOnClickListener(this);
        layout_select.setOnClickListener(this);
        dialog = builder.create();

        mseekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tv_num.setText("" + progress);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 0X10 && resultCode == -1) {
            Bundle bundle = data.getExtras();
            String pic = bundle.getString("pic");
            fid = bundle.getInt("fid");
            String namec = bundle.getString("namec");
            String namee = bundle.getString("namee");
            bitmapUtils.display(iv_plant, Const.URL_PATH + "sql_image" + pic);
            tv_c.setText(namec);
            tv_e.setText(namee);
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //选择植物
            case R.id.rlayout_append_select:
                Intent intent = new Intent(AppendActivity.this, PlantActivity.class);
                startActivityForResult(intent, 0X10);
                overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                break;
            //设置施肥间隔
            case R.id.bt_bottle_day:
                mCurrent = STATE_BOTTLE_DAY;
                mseekBar.setMax(30);
                mseekBar.setProgress(Integer.parseInt(num_bottle_day));
                tv_min.setText("0");
                tv_max.setText("30");
                tv_unit.setText("天");

                dialog.setTitle("设置施肥间隔");
                dialog.show();
                break;
            //设置施肥小时
            case R.id.bt_bottle_time:
                mCurrent = STATE_BOTTLE_TIME;
                mseekBar.setMax(24);
                mseekBar.setProgress(Integer.parseInt(num_bottle_time));
                tv_min.setText("0");
                tv_max.setText("24");
                tv_unit.setText("点");

                dialog.setTitle("设置施肥时间");
                dialog.show();
                break;
            //设置施肥量
            case R.id.bt_bottle_ml:
                mCurrent = STATE_BOTTLE_ML;
                mseekBar.setMax(400);
                mseekBar.setProgress(Integer.parseInt(num_bottle_ml));
                tv_min.setText("0");
                tv_max.setText("400");
                tv_unit.setText("ml");

                dialog.setTitle("设置施肥量");
                dialog.show();
                break;
            //设置浇水间隔
            case R.id.bt_water_day:
                mCurrent = STATE_WATER_DAY;
                mseekBar.setMax(30);
                mseekBar.setProgress(Integer.parseInt(num_water_day));
                tv_min.setText("0");
                tv_max.setText("30");
                tv_unit.setText("天");

                dialog.setTitle("设置浇水间隔");
                dialog.show();
                break;
            //设置浇水时间
            case R.id.bt_water_time:
                mCurrent = STATE_WATER_TIME;
                mseekBar.setMax(24);
                mseekBar.setProgress(Integer.parseInt(num_water_time));
                tv_min.setText("0");
                tv_max.setText("24");
                tv_unit.setText("点");

                dialog.setTitle("设置浇水时间");
                dialog.show();
                break;
            //设置浇水量
            case R.id.bt_water_ml:
                mCurrent = STATE_WATER_ML;
                mseekBar.setMax(400);
                mseekBar.setProgress(Integer.parseInt(num_water_ml));
                tv_min.setText("0");
                tv_max.setText("400");
                tv_unit.setText("ml");

                dialog.setTitle("设置浇水量");
                dialog.show();
                break;
            //提交
            case R.id.btn_append_sumbit:
                //联网
                if (fid != -1) {
                    String name = et_flowername.getText().toString().trim();
                    if (!TextUtils.isEmpty(name)) {
                        HashMap<String, Object> parms = new HashMap<>();
                        parms.put(Const.USER_ID, UIUtils.getSpInt(Const.USER_ID) + "");
                        parms.put("fid", fid + "");
                        parms.put("flowername", name);
                        parms.put("num_bottle_day", num_bottle_day);
                        parms.put("num_bottle_time", num_bottle_time);
                        parms.put("num_bottle_ml", num_bottle_ml);
                        parms.put("num_water_day", num_water_day);
                        parms.put("num_water_time", num_water_time);
                        parms.put("num_water_ml", num_water_ml);
                        httpHelper.getDetailData(Const.URL_APPEND, parms);
                    } else {
                        Toast.makeText(UIUtils.getContext(), "您没给花盆命名", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(UIUtils.getContext(), "您还没选择植物", Toast.LENGTH_SHORT).show();
                }
                break;
            //取消
            case R.id.btn_append_reset:
                finish();

                break;

        }
    }

    private long back_time;

    //双击返回的逻辑处理
    @Override
    public void onBackPressed() {
        //当前界面由注册页面进入时，设置双击退出
        if (Const.APPEND_INTSTATE == Const.APPEND_REGISTER) {
            long progress = System.currentTimeMillis();
            if (progress - back_time > 2000) {
                Toast.makeText(UIUtils.getContext(), "再按一次退出", Toast.LENGTH_SHORT).show();
                back_time = progress;
            } else {
                finish();
            }
        } else {
            finish();
        }
    }

}
