package com.monitorend;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class WVSS extends Activity {

	private WVSSView mWvssView;
	private ReceiveNaluThread mReceiveNaluThread;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mWvssView = new WVSSView(this, 320, 240);
        setContentView(mWvssView);
        
        MyApp myApp = (MyApp) getApplicationContext();
        Socket socket = myApp.getSocket();
        
        Intent intent = this.getIntent();
        String multicastAddress = intent.getStringExtra("multicastAddress");
        
        InputStream inputStream = null;
        try {
        	inputStream = socket.getInputStream();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
        mReceiveNaluThread = new ReceiveNaluThread(mWvssView, multicastAddress);
		new RecvSpsPpsThread(mWvssView, mReceiveNaluThread, inputStream).start();
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
//	        	new ConnectCenterServerThread(mWvssView).start();
	        	
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