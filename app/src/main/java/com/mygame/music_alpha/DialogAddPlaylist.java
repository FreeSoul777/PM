package com.mygame.music_alpha;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;

import java.util.ArrayList;

import static com.mygame.music_alpha.SongFragment.FOR_RESULT_ADD;

public class DialogAddPlaylist extends DialogFragment {

    private static final String TAG = "DialogAddPlaylist";
    public static final String DIALOG_ADD = "com.mygame.music_alpha.dialogaddplaylist";

    private Button btn_ok, btn_cancel;
    private ArrayList<PL> pl;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog  = new Dialog(getActivity(), R.style.AlertDialogTheme);
        dialog.setContentView(R.layout.dialog_add_playlist);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        final EditText txt_inputText = (EditText) dialog.findViewById(R.id.text_input);
        btn_ok = (Button) dialog.findViewById(R.id.btn_ok);
        btn_cancel = (Button) dialog.findViewById(R.id.btn_cancel);
        btn_ok.setText(getString(R.string.add));
        btn_cancel.setText(getString(R.string.cancel));
        TextView textView = (TextView) dialog.findViewById(R.id.title_dialog);
        textView.setText(getString(R.string.title_dialog_add));

        pl = MusicLab.get(getContext()).getPlaylistString();

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Toast.makeText(getContext(), getString(R.string.cancel), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "DialogAddPlaylist: Dialog -> Cancel ");
            }
        });
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(txt_inputText.getText().toString().equals("")){
                    Toast.makeText(getContext(), getString(R.string.the_entry_is_empty), Toast.LENGTH_SHORT).show();
                }
                else if(!(Contains(txt_inputText.getText().toString()))){
                    dialog.dismiss();
                    setResult(txt_inputText.getText().toString());
                    Toast.makeText(getContext(), getString(R.string.recorded) +" "+ txt_inputText.getText(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "DialogAddPlaylist: Dialog -> Add, " + txt_inputText.getText());
                }
                else {
                    Toast.makeText(getContext(), getString(R.string.NamePlayExists), Toast.LENGTH_SHORT).show();
                }
            }
        });
        return dialog;
    }

    public boolean Contains(String name) {
        for(com.mygame.music_alpha.PL playlist: pl) {
            if(playlist.getTitle().equals(name)){
                return true;
            }
        }
        return false;
    }

    private void setResult(String new_name){
        Bundle result = new Bundle();
        result.putSerializable(DIALOG_ADD, new_name);
        getParentFragmentManager().setFragmentResult(FOR_RESULT_ADD, result);
    }

    public static DialogAddPlaylist newInstance(){
        DialogAddPlaylist fragment = new DialogAddPlaylist();
        return fragment;
    }
}
