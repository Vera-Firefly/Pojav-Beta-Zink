package net.kdt.pojavlaunch;

import static net.kdt.pojavlaunch.Tools.shareLog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;

import androidx.annotation.Keep;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

@Keep
public class ExitActivity extends AppCompatActivity {

    @SuppressLint("StringFormatInvalid") //invalid on some translations but valid on most, cant fix that atm
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int code = -1;
        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            code = extras.getInt("code",-1);
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);

        new AlertDialog.Builder(this)
                .setMessage(getString(R.string.mcn_exit_title,code))
                .setPositiveButton(R.string.main_share_logs, (dialog, which) -> shareLog(this))
                .setOnDismissListener(dialog -> ExitActivity.this.finish())
                .show();
    }

    public static void showExitMessage(Context ctx, int code) {
        Intent i = new Intent(ctx,ExitActivity.class);
        i.putExtra("code",code);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.startActivity(i);
    }

}
