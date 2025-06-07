package com.termux.app;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.termux.R;

import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.graphics.Insets; // Para API 30+
import android.os.Build;

public class CustomDialogFragment extends DialogFragment {

    /*@NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_custom, null);

        MaterialButton closeButton = view.findViewById(R.id.dialog_button_close);
        closeButton.setOnClickListener(v -> dismiss());

        Dialog dialog = new MaterialAlertDialogBuilder(requireContext(), com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog)
                .setView(view)
                .create();

        dialog.setCanceledOnTouchOutside(false);

        return dialog;
    }*/

@NonNull
@Override
public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
    View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_custom, null);

    // Ajuste del padding inferior cuando aparece el teclado
    view.setOnApplyWindowInsetsListener((v, insets) -> {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            Insets imeInsets = insets.getInsets(WindowInsets.Type.ime());
            v.setPadding(0, 0, 0, imeInsets.bottom); // Ajuste solo del padding inferior
        }
        return insets;
    });

    MaterialButton closeButton = view.findViewById(R.id.dialog_button_close);
    closeButton.setOnClickListener(v -> dismiss());

    Dialog dialog = new MaterialAlertDialogBuilder(requireContext(), com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog)
            .setView(view)
            .create();

    dialog.setCanceledOnTouchOutside(false);

    // Muy importante: decorFitsSystemWindows = false para manejar los insets manualmente
    dialog.setOnShowListener(d -> {
        if (dialog.getWindow() != null) {
            dialog.getWindow().setDecorFitsSystemWindows(false);
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
