package com.termux.shared.termux.interact;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.Selection;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.graphics.drawable.GradientDrawable;
import android.graphics.Insets;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;

import com.termux.shared.R;
//import android.view.LayoutInflater;

public final class TextInputDialogUtils {

    public interface TextSetListener {
        void onTextSet(String text);
    }

    public static void textInput(FragmentActivity activity,
                                 int titleText,
                                 String initialText,
                                 int positiveButtonText,
                                 final TextSetListener onPositive,
                                 int neutralButtonText,
                                 final TextSetListener onNeutral,
                                 int negativeButtonText,
                                 final TextSetListener onNegative,
                                 @Nullable final DialogInterface.OnDismissListener onDismiss) {

        TextInputDialogFragment fragment = TextInputDialogFragment.newInstance(
                titleText, initialText,
                positiveButtonText, onPositive,
                neutralButtonText, onNeutral,
                negativeButtonText, onNegative,
                onDismiss
        );

        fragment.show(activity.getSupportFragmentManager(), "TextInputDialog");
    }

    public static class TextInputDialogFragment extends DialogFragment {

        private static int titleText;
        private static String initialText;
        private static int positiveButtonText;
        private static int neutralButtonText;
        private static int negativeButtonText;

        private static TextSetListener onPositive;
        private static TextSetListener onNeutral;
        private static TextSetListener onNegative;
        private static DialogInterface.OnDismissListener onDismiss;

        public static TextInputDialogFragment newInstance(
                int titleRes,
                String initial,
                int posRes,
                TextSetListener onPos,
                int neuRes,
                TextSetListener onNeu,
                int negRes,
                TextSetListener onNeg,
                @Nullable DialogInterface.OnDismissListener onDismissListener
        ) {
            titleText = titleRes;
            initialText = initial;
            positiveButtonText = posRes;
            neutralButtonText = neuRes;
            negativeButtonText = negRes;
            onPositive = onPos;
            onNeutral = onNeu;
            onNegative = onNeg;
            onDismiss = onDismissListener;
            return new TextInputDialogFragment();
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            Context context = requireContext();

            float dip = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, context.getResources().getDisplayMetrics());
            int padding = Math.round(24 * dip);

            LinearLayout layout = new LinearLayout(context);
            //View rootView = LayoutInflater.from(context).inflate(R.layout.dialog_termux, null);
            //LinearLayout layout = rootView.findViewById(R.id.dialog_container);

            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(padding, padding, padding, padding);

            // Título
            TextView titleView = new TextView(context);
            titleView.setText(titleText);
            titleView.setTextColor(Color.parseColor("#9c9c9c"));
            titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            titleView.setTypeface(null, Typeface.BOLD);
            //titleView.setPadding(0, 0, 0, Math.round(12 * dip));
            titleView.setPadding(30, 20, 30, 20);
            titleView.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            GradientDrawable background = new GradientDrawable();
            background.setColor(Color.parseColor("#60000000"));
            background.setCornerRadius(14 * dip);
            titleView.setBackground(background);

            // Linea divisoria
            View divider = new View(context);
            LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                Math.round(0.5f * dip)
            );
            dividerParams.setMargins(0, Math.round(12 * dip), 0, Math.round(10 * dip));
            divider.setLayoutParams(dividerParams);
            divider.setBackgroundColor(Color.parseColor("#333333"));

            // TextInputLayout
            TextInputLayout inputLayout = new TextInputLayout(context);
            inputLayout.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
            inputLayout.setHint("texto");
            inputLayout.setBoxBackgroundColor(Color.TRANSPARENT);
            inputLayout.setBoxStrokeColor(Color.TRANSPARENT);
            inputLayout.setBoxStrokeWidth(0);
            inputLayout.setBoxStrokeWidthFocused(0);
            inputLayout.setHintTextColor(ColorStateList.valueOf(Color.parseColor("#4e4e4e")));
            inputLayout.setDefaultHintTextColor(ColorStateList.valueOf(Color.parseColor("#4e4e4e")));
            Drawable icon = ContextCompat.getDrawable(context, R.drawable.ic_lead_pencil);
            inputLayout.setStartIconDrawable(icon);
            inputLayout.setStartIconTintList(ColorStateList.valueOf(Color.parseColor("#d7e7a9")));
            inputLayout.setStartIconMinSize((int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 38, context.getResources().getDisplayMetrics()));

            EditText editText = new EditText(context);
            editText.setSingleLine();
            editText.setBackground(null);
            //editText.setTypeface(null, Typeface.NORMAL);
            editText.setTextColor(Color.parseColor("#c3e4fd"));

            if (initialText != null) {
                editText.setText(initialText);
                Selection.setSelection(editText.getText(), initialText.length());
            }

            inputLayout.addView(editText);
            layout.addView(titleView);
            layout.addView(divider);
            layout.addView(inputLayout);

            // Ajuste para insets del teclado
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                layout.setOnApplyWindowInsetsListener((v, insets) -> {
                    Insets imeInsets = insets.getInsets(WindowInsets.Type.ime());
                    v.setPadding(padding, padding, padding, padding + imeInsets.bottom);
                    return insets;
                });
            }

            //MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context, com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog)
                    //.setView(rootView)
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context, com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog)
                    .setView(layout)
                    .setPositiveButton(positiveButtonText, (dialog, which) -> {
                        if (onPositive != null)
                            onPositive.onTextSet(editText.getText().toString());
                    });

            if (onNeutral != null) {
                builder.setNeutralButton(neutralButtonText, (dialog, which) -> {
                    onNeutral.onTextSet(editText.getText().toString());
                });
            }

            if (onNegative != null) {
                builder.setNegativeButton(negativeButtonText, (dialog, which) -> {
                    onNegative.onTextSet(editText.getText().toString());
                });
            }

            /*if (onNegative != null) {
                builder.setNegativeButton(negativeButtonText, (dialog, which) -> {
                    onNegative.onTextSet(editText.getText().toString());
                });
            } else {
                builder.setNegativeButton(android.R.string.cancel, null);
            }*/

            Dialog dialog = builder.create();

            dialog.setCanceledOnTouchOutside(false);

            dialog.setOnShowListener(d -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && dialog.getWindow() != null) {
                    dialog.getWindow().setDecorFitsSystemWindows(false);
                    layout.requestApplyInsets();
                }

                Button positiveButton = ((androidx.appcompat.app.AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                positiveButton.setTextColor(Color.parseColor("#d3c0f9"));
                positiveButton.setTypeface(null, Typeface.NORMAL);
                Button negativeButton = ((androidx.appcompat.app.AlertDialog) dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                negativeButton.setTextColor(Color.parseColor("#888888"));
                negativeButton.setTypeface(null, Typeface.NORMAL);
                Button neutralButton = ((androidx.appcompat.app.AlertDialog) dialog).getButton(DialogInterface.BUTTON_NEUTRAL);
                neutralButton.setTextColor(Color.parseColor("#666666"));
                neutralButton.setTypeface(null, Typeface.NORMAL);
            });

            return dialog;
        }

        @Override
        public void onStart() {
            super.onStart();
            Dialog dialog = getDialog();
            if (dialog != null && dialog.getWindow() != null) {
                //dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                /*GradientDrawable background = new GradientDrawable();
                background.setColor(0xcd000000);
                background.setCornerRadius(48f);
                background.setStroke(6, 0x80afff00);
                dialog.getWindow().setBackgroundDrawable(background);*/
                // Drawable con degradado para el borde
                int[] gradientColors = new int[] {
                    0x804efcd7,
                    0x807abbff,
                    0x80c64dff
                };
                GradientDrawable border = new GradientDrawable(
                    GradientDrawable.Orientation.LEFT_RIGHT,
                    gradientColors
                );
                border.setCornerRadius(48f);
                // Drawable interno (fondo del diálogo)
                GradientDrawable background = new GradientDrawable();
                background.setColor(0xcd000000);
                background.setCornerRadius(44f);
                // Combinar, ajustar y aplicar
                Drawable[] layers = new Drawable[] { border, background };
                LayerDrawable layerDrawable = new LayerDrawable(layers);
                layerDrawable.setLayerInset(1, 6, 6, 6, 6);
                dialog.getWindow().setBackgroundDrawable(layerDrawable);
            }
        }

        @Override
        public void onDismiss(@NonNull DialogInterface dialog) {
            super.onDismiss(dialog);
            if (onDismiss != null) onDismiss.onDismiss(dialog);
        }
    }
}
