package gr.vaggelis.myapplication.alerts;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import gr.vaggelis.myapplication.R;

/**
 * Created on 07/03/2019.
 */
public class AlertUtil {

    /**
     * Warning Dialog
     * @param context context
     * @param message The message to display
     * @param positiveBtnText The text you want to display on positive button
     * @param negativeBtnText The text you want to display on negative button
     * @return True if user clicks OK
     */
    public static boolean showAlert(final Context context, final String message, int positiveBtnText, int negativeBtnText) {

        @SuppressLint("HandlerLeak") final Handler handler = new Handler(){
            @Override
            public void handleMessage(Message msg)
            {
                throw new RuntimeException();
            }
        };

        Resources res = context.getResources();
        android.support.v7.app.AlertDialog.Builder alert = new android.support.v7.app.AlertDialog.Builder(context);
        alert.setTitle("GPS Settings");

        LinearLayout base = new LinearLayout(context);
        base.setOrientation(LinearLayout.VERTICAL);
        base.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        base.setPadding(16, 16, 16, 16);

        TextView tv = new TextView(context, null, R.style.TextNormal);
        tv.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        tv.setText(message);
        base.addView(tv);
        alert.setView(base);
        final boolean[] userClickedOK = {false};

        alert.setPositiveButton(res.getString(positiveBtnText), (dialog, whichButton) -> {
            handler.sendMessage(handler.obtainMessage());
            userClickedOK[0] = true;
        });
        alert.setNegativeButton(res.getString(negativeBtnText), (dialog, whichButton) -> {
            handler.sendMessage(handler.obtainMessage());
            userClickedOK[0] = false;
        });
        // Create and show dialog
        android.support.v7.app.AlertDialog alertDialog = alert.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        try{
            alertDialog.show();
        }catch (Exception e){
            e.printStackTrace();
        }


        try{
            Looper.loop();
        }
        catch(RuntimeException e){
            e.printStackTrace();
        }

        return  userClickedOK[0];
    }
}
