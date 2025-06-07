package com.termux.app;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.Insets;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.termux.R;

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
