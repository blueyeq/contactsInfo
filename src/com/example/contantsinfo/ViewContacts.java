package com.example.contantsinfo;

import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.app.Activity;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
 

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.ListActivity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.widget.Button;
import android.widget.SimpleAdapter; 
import com.android.internal.telephony.*;

public class ViewContacts extends ListActivity {
	
	private TelephonyManager 				telemanger 		 = null;
	private List<HashMap<String, String>>   people 			 = null;
	private Button 							selectContactBtn = null;
	private PhoneStateListener              phonelistener    = null;
	
    /** Main **/
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts_main);
        init();
        
    }
    
    /* init */
    public void init(){
    	telemanger =  (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
    	people     =  fillMaps();
    	Button selectContactBtn = (Button) findViewById(R.id.selectContactButton);
        SimpleAdapter adapter = new SimpleAdapter(
                         this,people,R.layout.list_item,
                         new String[]{"name","key"}, 
                         new int[]{R.id.item,R.id.item2});
        
        this.setListAdapter(adapter);
        
        phonelistener = new PhoneStateListener() {
        	
        	public void onCallStateChanged(int state, String callInNumber){
        		super.onCallStateChanged(state, callInNumber);
        		switch( state ) {
        		case TelephonyManager.CALL_STATE_IDLE:
        			Log.v("call", "call idle");
        			break;
        		case TelephonyManager.CALL_STATE_RINGING:
        			Log.v("call","call ringing");
        			break;
        		case TelephonyManager.CALL_STATE_OFFHOOK:
        			Log.v("call","call offhook");
        			break;
        		default:
        			Log.v("call","call other");
        			break;
        		}
        	}
        }; 
        telemanger.listen(phonelistener, PhoneStateListener.LISTEN_CALL_STATE); 
    }
    
   
    
    
    private void SetContactButnListen(Button butn) {
    	butn.setOnClickListener(new Button.OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Log.v("butn", "click select butn");
				try {
					StopCallIn();
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NoSuchMethodException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
    	});
    }
    private List<HashMap<String, String>> fillMaps() {
        List<HashMap<String, String>> items = new ArrayList<HashMap<String, String>>(); 
        Cursor cur = null;
        try {
            // Query using ContentResolver.query or Activity.managedQuery
            cur = getContentResolver().query(
                            ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
            if (cur.moveToFirst()) {
            	int idColumn = cur.getColumnIndex( ContactsContract.Contacts._ID);
                int displayNameColumn = cur.getColumnIndex( ContactsContract.Contacts.DISPLAY_NAME);
                    // Iterate all users
                do {
                    String contactId;
                    String displayName;
                    String phoneNumber = "";
                    // Get the field values
                    contactId = cur.getString(idColumn);
                    displayName = cur.getString(displayNameColumn);
                    // Get number of user's phoneNumbers
                    int numberCount = cur.getInt(cur.getColumnIndex( ContactsContract.Contacts.HAS_PHONE_NUMBER));
                    if (numberCount>0) {
                        Cursor phones = getContentResolver().query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID 
                        + " = " + contactId 
                        /*+ " and " + ContactsContract.CommonDataKinds.Phone.TYPE 
                        + "=" + ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE*/,
                        null, null);
                        if (phones.moveToFirst()) {
                            int numberColumn = phones.getColumnIndex(
                                            ContactsContract.CommonDataKinds.Phone.NUMBER);
                            // Iterate all numbers
                            do {
                                phoneNumber += phones.getString(numberColumn) + ",";
                            } while (phones.moveToNext());
                        } 
                    }
                    // Add values to items
                    HashMap<String, String> i = new HashMap<String, String>();
                    i.put("name", displayName);
                    i.put("key", phoneNumber);
                    items.add(i);
                } while (cur.moveToNext());
            } else {
                    HashMap<String, String> i = new HashMap<String, String>();
                    i.put("name", "Your Phone");
                    i.put("key", "Have No Contacts.");
                    items.add(i);
            }
        } finally {
            if (cur != null){
            	cur.close();
            }
        }
        return items;
    }

    private void StopCallIn() throws NoSuchMethodException, ClassNotFoundException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, RemoteException{
    	Method  method = Class.forName("android.os.ServiceManager").getMethod("getService", String.class); 
    	IBinder binder = (IBinder)method.invoke(null, new Object[]{TELEPHONY_SERVICE}); 
    	ITelephony telephony = ITelephony.Stub.asInterface(binder); 
    	telephony.endCall(); 
    }
}
