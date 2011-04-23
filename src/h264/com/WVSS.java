package h264.com;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class WVSS extends Activity {

	WVSSView vv;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vv = new WVSSView(this);
        setContentView(vv);
    }
    
    // Menu item IDs
    public static final int PLAY_ID = Menu.FIRST;    
    public static final int EXIT_ID = Menu.FIRST + 1; 

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        
        menu.add(0, PLAY_ID, 0, R.string.play);   
        menu.add(0, EXIT_ID, 1, R.string.exit);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {        
	        case PLAY_ID:
	        {
	          // 此处设定不同分辨率的码流文件
	          
	        	String file = "/sdcard/352x288.264"; //352x288.264"; //240x320.264"; 
	        	vv.PlayVideo(file);
	        	
	        	Log.d("pIC", "start to play");
	        	
	            return true;
	        }
	        case EXIT_ID:
	        {
	        	finish();
	            return true;
	        }
        }
        return super.onOptionsItemSelected(item);
    }
}