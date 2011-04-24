package com.monitorend;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class WVSS extends Activity {

	WVSSView mWvssView;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mWvssView = new WVSSView(this, 320, 240);
        setContentView(mWvssView);
    }
    
    // Menu item IDs
    public static final int CONNECTING_ID = Menu.FIRST;    
    public static final int EXIT_ID = Menu.FIRST + 1; 

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        
        menu.add(0, CONNECTING_ID, 0, R.string.connect_server);   
        menu.add(0, EXIT_ID, 1, R.string.exit);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {        
	        case CONNECTING_ID:
	        {	
	        	new ConnectCenterServerThread(mWvssView).start();
	        	
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