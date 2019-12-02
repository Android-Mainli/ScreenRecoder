package com.ryan.screenrecoder.ui.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.display.DisplayManager;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ryan.screenrecoder.R;
import com.ryan.screenrecoder.application.ScreenApplication;
import com.ryan.screenrecoder.application.SysValue;
import com.ryan.screenrecoder.util.NetWorkUtils;
import com.ryan.screenrecoder.util.SharedUtil;
import com.ryan.screenrecoder.util.SysUtil;

/**
 * https://blog.csdn.net/chailongger/article/details/83653652
 */
public class MainActivity extends Activity implements View.OnClickListener {
    private final int REQUEST_CODE = 0x11;
    private final int PERMISSION_CODE = 0x12;
    private static final String DEFAULT_IP = "192.168.137.60";

    private Button button_tcp_preview;
    private Button button_tcp_send;
    private EditText edittext_tcp_send_ip;

    private MediaProjectionManager mediaProjectionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView textView = (TextView) findViewById(R.id.current_id);
        textView.setText("当前IP:" + NetWorkUtils.getLocalIpAddress(this));
        button_tcp_preview = ((Button) findViewById(R.id.button_tcp_preview));
        button_tcp_send = ((Button) findViewById(R.id.button_tcp_send));
        edittext_tcp_send_ip = ((EditText) findViewById(R.id.edittext_tcp_send_ip));
        String ip = SharedUtil.init(this).getIp();
        if (TextUtils.isEmpty(ip)) {
            edittext_tcp_send_ip.setHint("默认IP:" + DEFAULT_IP);
        }
        button_tcp_preview.setOnClickListener(this);
        button_tcp_send.setOnClickListener(this);
        if (SysValue.api >= Build.VERSION_CODES.M) {
            getAppPermission();
        } else if (SysValue.api >= 21) {
            getMeidProjection();
        } else {
            //todo 需要root权限或系统签名
            ScreenApplication.getInstance().setDisplayManager(((DisplayManager) getSystemService(Context.DISPLAY_SERVICE)));
        }
    }

    private void getAppPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_CODE);
    }

    @TargetApi(21)
    private void getMeidProjection() {
        mediaProjectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
        startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            ScreenApplication.getInstance().setMediaProjection(mediaProjectionManager.getMediaProjection(resultCode, data));
        } else {
            Toast.makeText(this, "无法录制屏幕", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getMeidProjection();
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_tcp_preview:
                // 启动接收端
                startActivity(new Intent(this, PlayerActivity.class));
                break;
            case R.id.button_tcp_send:
                // 启动发送端
                String ip = edittext_tcp_send_ip.getText().toString();
                if (TextUtils.isEmpty(ip)) {
                    ip = DEFAULT_IP;
                }
                if (!SysUtil.isIpAddress(ip)) {
                    Toast.makeText(this, "请输入有效的IP地址", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(this, TcpSendActivity.class);
                intent.putExtra("ip", ip);
                startActivity(intent);
                break;
        }
    }
}
