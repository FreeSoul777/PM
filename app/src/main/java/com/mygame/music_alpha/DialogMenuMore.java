package com.mygame.music_alpha;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;

import static com.mygame.music_alpha.SongFragment.FOR_RESULT;

public class DialogMenuMore extends DialogFragment {

    private static final String TAG = "DialogMenuMore";
    public static final String RES = "com.mygame.music_alpha.dialogmenumore";
    private static final String DATA = "string[]";
    private static final String NAME = "nameSongForDialogMenuMore";

    private RadioGroup radioGroup;
    private RadioButton radioButton;
    private Button btn_ok, btn_cancel;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new Dialog(getActivity(), R.style.AlertDialogTheme);
        dialog.setContentView(R.layout.dialog_delete_in_pl);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        String name = (String) getArguments().getSerializable(NAME);
        String[] s = (String[]) getArguments().getSerializable(DATA);

        btn_ok = (Button) dialog.findViewById(R.id.btn_ok);
        btn_cancel = (Button) dialog.findViewById(R.id.btn_cancel);
        btn_ok.setText(R.string.ok);
        btn_cancel.setText(R.string.cancel);
        TextView textView = (TextView) dialog.findViewById(R.id.title_dialog);
        textView.setText(name);
        radioGroup = dialog.findViewById(R.id.ratingRadioGroup);
        for(int i = 0; i < s.length; ++i){
            RadioButton rr = new RadioButton(getContext());
            int _dp_18 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 18, getResources().getDisplayMetrics());
            int _dp_15 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15, getResources().getDisplayMetrics());
            int _dp_0 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0, getResources().getDisplayMetrics());
            rr.setText(s[i]);
            rr.setTextSize(18);
            rr.setTextColor(Color.WHITE);
            rr.setOnClickListener(this::checkButton);
            if(i == 0) {
                rr.setChecked(true);
            }
            rr.setId(i);
            RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(RadioGroup.LayoutParams.WRAP_CONTENT, RadioGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(_dp_0, _dp_0, _dp_0, _dp_15);
            radioGroup.addView(rr, params);
        }

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Toast.makeText(getContext(), getString(R.string.cancel), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "MenuMore: Dialog -> Cancel ");
            }
        });
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int radioId = radioGroup.getCheckedRadioButtonId();
                radioButton = radioGroup.findViewById(radioId);
                dialog.dismiss();
                setResult(radioButton.getText().toString());
                Toast.makeText(getContext(), getString(R.string.ok) + ", " + radioButton.getText(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "MenuMore: Dialog -> OK ");
            }
        });
        return dialog;
    }

    private void checkButton(View v){
        int radioId = radioGroup.getCheckedRadioButtonId();
        radioButton = radioGroup.findViewById(radioId);
//        Toast.makeText(getContext(), "Selected: " + radioId, Toast.LENGTH_SHORT).show();
    }

    private void setResult(String order){
        Bundle result = new Bundle();
        result.putSerializable(RES, order);
        getParentFragmentManager().setFragmentResult(FOR_RESULT, result);
    }

    public static DialogMenuMore newInstance(String[] s, String name){
        Bundle args = new Bundle();
        args.putSerializable(DATA, s);
        args.putSerializable(NAME, name);

        DialogMenuMore fragment = new DialogMenuMore();
        fragment.setArguments(args);
        return fragment;
    }
}
