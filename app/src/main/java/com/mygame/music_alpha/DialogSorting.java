package com.mygame.music_alpha;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;

import static com.mygame.music_alpha.MusicLab.SORT_SONGS;
import static com.mygame.music_alpha.SongFragment.FOR_RESULT;

public class DialogSorting extends DialogFragment {

    private static final String TAG = "DialogSorting";
    public static final String ORDER_DIALOG = "com.mygame.music_alpha.dialogsorting";
    private static final String CHOICE_SORT = "choice_sort";

    private RadioGroup radioGroup;
    private RadioButton radioButton;
    private Button btn_ok, btn_cancel;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog  = new Dialog(getActivity(), R.style.AlertDialogTheme);
        dialog.setContentView(R.layout.dialog_delete_in_pl);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        String choice = (String) getArguments().getSerializable(CHOICE_SORT);

        btn_ok = (Button) dialog.findViewById(R.id.btn_ok);
        btn_cancel = (Button) dialog.findViewById(R.id.btn_cancel);
        btn_ok.setText(getString(R.string.sorting));
        btn_cancel.setText(getString(R.string.cancel));
        TextView textView = (TextView) dialog.findViewById(R.id.title_dialog);
        textView.setText(getString(R.string.sort));
        radioGroup = dialog.findViewById(R.id.ratingRadioGroup);

        String[] s = {getString(R.string.date), getString(R.string.song), getString(R.string.album),
                getString(R.string.artist)};

        for(int i = 0; i < s.length; ++i){
            RadioButton rr = new RadioButton(getContext());
            int _dp_18 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 18, getResources().getDisplayMetrics());
            int _dp_15 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15, getResources().getDisplayMetrics());
            int _dp_0 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0, getResources().getDisplayMetrics());
            rr.setText(s[i]);
            rr.setTextSize(18);
            rr.setTextColor(Color.WHITE);
            rr.setOnClickListener(this::checkButton);
            if(i == 0 && choice.equals("sortByDate")) {
                rr.setChecked(true);
            }
            else if(i == 1 && choice.equals("sortByName")) {
                rr.setChecked(true);
            }
            else if(i == 2 && choice.equals("sortByAlbum")) {
                rr.setChecked(true);
            }
            else if(i == 3 && choice.equals("sortByArtist")) {
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
                Toast.makeText(getContext(), R.string.cancel, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "DialogSorting: Dialog -> Cancel ");
            }
        });
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int radioId = radioGroup.getCheckedRadioButtonId();
                dialog.dismiss();
                Toast.makeText(getContext(), R.string.ok + ", " + radioButton.getText(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "DialogSorting: Dialog -> OK ");
                switch (radioId){
                    case 0:
                        if(!SORT_SONGS.equals("sortByDate")) {
                            SORT_SONGS = "sortByDate";
                            MusicLab.ORDER = MediaStore.Audio.Media.DATE_MODIFIED + " DESC";
                            setResult(MediaStore.Audio.Media.DATE_MODIFIED + " DESC");
                        }
                        break;
                    case 1:
                        if(!SORT_SONGS.equals("sortByName")) {
                            SORT_SONGS = "sortByName";
                            MusicLab.ORDER = MediaStore.Audio.Media.TITLE + " COLLATE LOCALIZED ASC";
                            setResult(MediaStore.Audio.Media.TITLE + " COLLATE LOCALIZED ASC");
                        }
                        break;
                    case 2:
                        if(!SORT_SONGS.equals("sortByAlbum")) {
                            SORT_SONGS = "sortByAlbum";
                            MusicLab.ORDER = MediaStore.Audio.Media.ALBUM + " COLLATE LOCALIZED ASC";
                            setResult(MediaStore.Audio.Media.ALBUM + " COLLATE LOCALIZED ASC");
                        }
                        break;
                    case 3:
                        if(!SORT_SONGS.equals("sortByArtist")) {
                            SORT_SONGS = "sortByArtist";
                            MusicLab.ORDER = MediaStore.Audio.Media.ARTIST + " COLLATE LOCALIZED ASC";
                            setResult(MediaStore.Audio.Media.ARTIST + " COLLATE LOCALIZED ASC");
                        }
                        break;
                }
//                Toast.makeText(getContext(), "SORTING", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Songs IS SORTING");
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
        result.putSerializable(ORDER_DIALOG, order);
        getParentFragmentManager().setFragmentResult(FOR_RESULT, result);
    }

    public static DialogSorting newInstance(String s){
        Bundle args = new Bundle();
        args.putSerializable(CHOICE_SORT, s);

        DialogSorting fragment = new DialogSorting();
        fragment.setArguments(args);
        return fragment;
    }
}
