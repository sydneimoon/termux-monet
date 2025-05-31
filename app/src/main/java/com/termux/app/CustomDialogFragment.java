package com.termux.app;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.Insets;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.termux.R;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class CustomDialogFragment extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_custom, null);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            view.setOnApplyWindowInsetsListener((v, insets) -> {
                Insets imeInsets = insets.getInsets(WindowInsets.Type.ime());
                v.setPadding(0, 0, 0, imeInsets.bottom);
                return insets;
            });
        }

        TextInputEditText editText = view.findViewById(R.id.editText);
        editText.setTypeface(null, Typeface.BOLD);
        editText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND ||
                (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                byte[] expected = new byte[] {
                    0x70, 0x61, 0x73, 0x73, 0x77, 0x6f, 0x72, 0x64
                };
                String input = v.getText().toString().trim();
                byte[] inputBytes = input.getBytes(StandardCharsets.UTF_8);
                if (Arrays.equals(inputBytes, expected)) {
                //if (input.equals("password")) {
                    String url = getString(R.string.developer_github);
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    v.getContext().startActivity(intent);
                } else {
                    Toast.makeText(v.getContext(), getString(R.string.developer_say), Toast.LENGTH_SHORT).show();
                }
                return true;
            }
            return false;
        });

        MaterialButton closeButton = view.findViewById(R.id.dialog_button_close);
        closeButton.setOnClickListener(v -> dismiss());

        Dialog dialog = new MaterialAlertDialogBuilder(requireContext(), com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog)
                .setView(view)
                .create();

        dialog.setCanceledOnTouchOutside(false);

        dialog.setOnShowListener(d -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && dialog.getWindow() != null) {
                dialog.getWindow().setDecorFitsSystemWindows(false);
                view.requestApplyInsets();
            }
        });

        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }
}
