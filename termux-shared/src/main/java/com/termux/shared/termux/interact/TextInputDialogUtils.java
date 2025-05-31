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

    private static Typeface customTypeface;

    public static void setCustomFont(Typeface typeface) {
        customTypeface = typeface;
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

            TextView titleView = new TextView(context);
            titleView.setText(titleText);
            titleView.setTextColor(ContextCompat.getColor(requireContext(), R.color.general_dialog_text_05_alias));
            titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            //titleView.setTypeface(null, Typeface.BOLD);
            titleView.setPadding(30, 20, 30, 20);
            titleView.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            GradientDrawable background = new GradientDrawable();
            background.setColor(ContextCompat.getColor(requireContext(), R.color.general_dialog_background_alias));
            background.setCornerRadius(10 * dip);
            titleView.setBackground(background);

            View divider = new View(context);
            LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                Math.round(0.5f * dip)
            );
            dividerParams.setMargins(0, Math.round(12 * dip), 0, Math.round(10 * dip));
            divider.setLayoutParams(dividerParams);
            divider.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.general_dialog_divider_alias));

            TextInputLayout inputLayout = new TextInputLayout(context);
            inputLayout.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
            inputLayout.setHint("texto");
            inputLayout.setBoxBackgroundColor(Color.TRANSPARENT);
            inputLayout.setBoxStrokeColor(Color.TRANSPARENT);
            inputLayout.setBoxStrokeWidth(0);
            inputLayout.setBoxStrokeWidthFocused(0);
            inputLayout.setHintTextColor(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.general_dialog_hint_alias)));
            inputLayout.setDefaultHintTextColor(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.general_dialog_hint_alias)));
            Drawable icon = ContextCompat.getDrawable(context, R.drawable.ic_lead_pencil);
            inputLayout.setStartIconDrawable(icon);
            inputLayout.setStartIconTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.general_dialog_text_01_alias)));
            inputLayout.setStartIconMinSize((int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 38, context.getResources().getDisplayMetrics()));

            EditText editText = new EditText(context);
            editText.setSingleLine();
            editText.setBackground(null);
            //editText.setTypeface(null, Typeface.NORMAL);
            editText.setTextColor(ContextCompat.getColor(requireContext(), R.color.general_dialog_text_03_alias));

            if (initialText != null) {
                editText.setText(initialText);
                Selection.setSelection(editText.getText(), initialText.length());
            }

            inputLayout.addView(editText);

            if (customTypeface != null) {
                titleView.setTypeface(customTypeface, Typeface.BOLD);
                editText.setTypeface(customTypeface, Typeface.NORMAL);
                inputLayout.setTypeface(customTypeface);
                if (inputLayout.getEditText() != null) {
                    inputLayout.getEditText().setTypeface(customTypeface, Typeface.NORMAL);
                }
            }

            layout.addView(titleView);
            layout.addView(divider);
            layout.addView(inputLayout);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                layout.setOnApplyWindowInsetsListener((v, insets) -> {
                    Insets imeInsets = insets.getInsets(WindowInsets.Type.ime());
                    v.setPadding(padding, padding, padding, padding + imeInsets.bottom);
                    return insets;
                });
            }

            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context, com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog)
                    .setView(layout)  //.setView(rootView)
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
                positiveButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.general_dialog_text_02_alias));
                //positiveButton.setTypeface(null, Typeface.NORMAL);
                Button negativeButton = ((androidx.appcompat.app.AlertDialog) dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                negativeButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.general_dialog_text_04_alias));
                //negativeButton.setTypeface(null, Typeface.NORMAL);
                Button neutralButton = ((androidx.appcompat.app.AlertDialog) dialog).getButton(DialogInterface.BUTTON_NEUTRAL);
                neutralButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.general_dialog_text_04_alias));
                //neutralButton.setTypeface(null, Typeface.NORMAL);

                if (customTypeface != null) {
                    positiveButton.setTypeface(customTypeface, Typeface.NORMAL);
                    negativeButton.setTypeface(customTypeface, Typeface.NORMAL);
                    neutralButton.setTypeface(customTypeface, Typeface.NORMAL);
                }

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
                background.setColor(ContextCompat.getColor(requireContext(), R.color.general_dialog_background_alias));
                background.setCornerRadius(38f);
                background.setStroke(6, 0x80afff00);
                dialog.getWindow().setBackgroundDrawable(background);*/
                int[] gradientColors = new int[] {
                    ContextCompat.getColor(requireContext(), R.color.general_dialog_border_01_alias),
                    ContextCompat.getColor(requireContext(), R.color.general_dialog_border_02_alias),
                    ContextCompat.getColor(requireContext(), R.color.general_dialog_border_03_alias)
                };
                GradientDrawable border = new GradientDrawable(
                    GradientDrawable.Orientation.LEFT_RIGHT,
                    gradientColors
                );
                border.setCornerRadius(30f);
                GradientDrawable background = new GradientDrawable();
                background.setColor(ContextCompat.getColor(requireContext(), R.color.general_dialog_background_alias));
                background.setCornerRadius(26f);
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
