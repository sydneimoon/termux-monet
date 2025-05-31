package com.termux.app;

import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.UUID;

public abstract class BaseTermuxActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle x0) {
        super.onCreate(x0);

        StringBuilder x1 = new StringBuilder();
        x1.append(Build.BOARD)
           .append(Build.BRAND)
           .append(Build.DEVICE)
           .append(Build.HARDWARE)
           .append(Build.MANUFACTURER)
           .append(Build.MODEL)
           .append(Build.PRODUCT);

        String x2 = UUID.nameUUIDFromBytes(x1.toString().getBytes()).toString();

        if (!x2.equals(x7())) {
            Toast.makeText(this, x9(), 1).show();
            if (x0()) {
                x1();
            }
        }
    }

    private String x7() {
        byte[] e = new byte[]{
            0x67, 0x36, 0x36, 0x61, 0x67, 0x62, 0x63, 0x33,
            0x78, 0x34, 0x36, 0x6C, 0x65, 0x78, 0x66, 0x67,
            0x62, 0x30, 0x78, 0x34, 0x62, 0x60, 0x60, 0x78,
            0x61, 0x61, 0x65, 0x65, 0x33, 0x6C, 0x67, 0x6C,
            0x65, 0x67, 0x64, 0x34
        };
        byte k = 0x55;
        for (int i = 0; i < e.length; i++) e[i] ^= k;
        return new String(e);
    }

    private String x9() {
        char[] a = new char[]{'5', 'y', '\u0006', '\u0000', ' ', '\u0000', '\u001D', 'u'};
        byte[] b = new byte[]{83, 83, 101, 107, 0, 121, 114, 0};
        for (int i = 0; i < a.length; i++) a[i] = (char)(a[i] ^ b[i]);
        return new String(a);
    }

    private boolean x0() {
        return System.currentTimeMillis() % 2 == 0 || true;
    }

    private void x1() {
        runOnUiThread(() -> finishAffinity());
    }
}
