package net.virifi.android.spstarter;

import java.util.Calendar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class SMSServiceStarterActivity extends Activity {
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        Button startButton = (Button) findViewById(R.id.start_button);
        startButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				EditText addressEditText = (EditText) findViewById(R.id.address_edit_text);
				String address = addressEditText.getText().toString();
				Intent intent = new Intent("android.provider.Telephony.WAP_PUSH_RECEIVED");
				intent.setClassName("jp.co.nttdocomo.carriermail", "jp.co.nttdocomo.carriermail.SMSService");
				intent.setType("application/vnd.wap.emn+wbxml");
				byte[] pdu = buildPdu(address);
				if (pdu == null)
					return;
				intent.putExtra("data", pdu);
				startService(intent);
			}
		});
    }
    
	private byte[] buildPdu(String address) {
		if (address == null)
			return null;
		byte[] addressByte = address.getBytes();
		
		Calendar cal = Calendar.getInstance();
		byte[] dateByte = new byte[7];
		int year = cal.get(Calendar.YEAR);
		int upper = (int)(year / 100);
		int lower = year % 100;
		dateByte[0] = (byte)convertToHexValue(upper);
		dateByte[1] = (byte)convertToHexValue(lower);
		dateByte[2] = (byte)convertToHexValue(cal.get(Calendar.MONTH));
		dateByte[3] = (byte)convertToHexValue(cal.get(Calendar.DATE));
		dateByte[4] = (byte)convertToHexValue(cal.get(Calendar.HOUR_OF_DAY));
		dateByte[5] = (byte)convertToHexValue(cal.get(Calendar.MINUTE));
		dateByte[6] = (byte)convertToHexValue(cal.get(Calendar.SECOND));
		
		byte[] bytes1 = new byte[]{0x3, 0xd, 0x6a, 0, -0x7b, 0x7, 0x3};
		byte[] bytes2 = new byte[]{'\0', 0x5, -0x3d, 7};
		
		int dataLength = bytes1.length + addressByte.length + bytes2.length + dateByte.length + 1;
		byte[] data = new byte[dataLength];
		data[dataLength - 1] = 1;
		
		int startIndex = 0;
		System.arraycopy(bytes1, 0, data, startIndex, bytes1.length);
		startIndex += bytes1.length;
		System.arraycopy(addressByte, 0, data, startIndex, addressByte.length);
		startIndex += addressByte.length;
		System.arraycopy(bytes2, 0, data, startIndex, bytes2.length);
		startIndex += bytes2.length;
		System.arraycopy(dateByte, 0, data, startIndex, dateByte.length);
		
		return data;
	}
	
	private int convertToHexValue(int x) {
		String str = String.valueOf(x);
		return Integer.valueOf(str, 16);
	}
}