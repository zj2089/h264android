package com.monitorend;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class WVSS extends Activity {

	private WVSSView mWvssView;
	private ReceiveNaluThread mReceiveNaluThread;
//	private MulticastLock mMulticastLock;
	private DatagramPacketQueue mDatagramPacketQueue;
	private Dialog mCommandDialog;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mWvssView = new WVSSView(this, 320, 240);
        setContentView(mWvssView);
        
//        // allow this application to receive multicast packets
//		WifiManager wifi = (WifiManager) getSystemService( Context.WIFI_SERVICE );
//		mMulticastLock = wifi.createMulticastLock("receive_nalu");
//		mMulticastLock.setReferenceCounted(false);
//		mMulticastLock.acquire();
        
        MyApp myApp = (MyApp) getApplicationContext();
        Socket socket = myApp.getSocket();
        
//        Intent intent = this.getIntent();
//        String multicastAddress = intent.getStringExtra("multicastAddress");
        
        InputStream inputStream = null;
        try {
        	inputStream = socket.getInputStream();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		mDatagramPacketQueue = new DatagramPacketQueue();
		
        mReceiveNaluThread = new ReceiveNaluThread(/*multicastAddress, */mDatagramPacketQueue);
		new RecvSpsPpsThread(mWvssView, mReceiveNaluThread, inputStream).start();
		
		new ProcessRtpPacketThread(mWvssView, mDatagramPacketQueue).start();
    }
    
    @Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		
		Log.i("STOP", "WVSS activity stopping");
		
//		mMulticastLock.release();
	}

	// Menu item IDs
    public static final int SENDCOMMAND_ID = Menu.FIRST;    
    public static final int EXIT_ID = Menu.FIRST + 1;
	private static final int SEND_COMMAND_DIALOG = 1;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        
        menu.add(0, SENDCOMMAND_ID, 0, R.string.sendCommand);   
        menu.add(0, EXIT_ID, 1, R.string.exit);

        return true;
    }

    @Override
	protected Dialog onCreateDialog(int id) {
		
    	switch(id) 
    	{
    	case SEND_COMMAND_DIALOG:
    		return (initCommandDialog());
    	}
		return super.onCreateDialog(id);
	}
    
    private Dialog initCommandDialog() {
    	
    	AlertDialog.Builder builder = new AlertDialog.Builder(WVSS.this);
    	LayoutInflater factory = LayoutInflater.from(this);
    	final View commandDialogView = factory.inflate(R.layout.send_command, null);
    	
    	builder.setIcon(R.drawable.icon);
    	builder.setTitle(R.string.CommandTitle);
    	builder.setView(commandDialogView);
    	builder.setPositiveButton(R.string.command_ok, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				
			}
		});
    	
    	builder.setNegativeButton(R.string.command_cancle, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				
			}
		});
    	
    	return mCommandDialog;
    }

	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {        
	        case SENDCOMMAND_ID:
	        {	
	        	showDialog(SEND_COMMAND_DIALOG);
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