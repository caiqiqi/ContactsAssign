package caiqiqi.contacts;

import java.util.ArrayList;
import java.util.HashMap;

import android.provider.ContactsContract;
import android.util.Log;


public class VCard {
	private static final String LOG_TAG = VCard.class.getName();
	private static final String VCARD_BEGIN = "BEGIN:VCARD\n\n";
	private static final String VCARD_VERSION = "VERSION:2.1\n\n";
	private static final String VCARD_END = "END:VCARD\n\n";
	public static final String TEL_TYPE = "TYPE";
	public static final String TEL_NUM = "NUM";
	
	public static final String EMAIL_TYPE = "TYPE";
	public static final String EMAIL_ADDR = "ADDR";
	
	private StringBuilder vcard = null;
	private ArrayList<HashMap<String, String>> mTels = null;//电话号码的HashMap的List
	private ArrayList<HashMap<String, String>> mEmail = null;//Email地址的List
	private String mName = "";
	private String mRawInfo = "";
	public VCard() {
		vcard = new StringBuilder();
		mTels = new ArrayList<HashMap<String, String>>();
		mEmail = new ArrayList<HashMap<String,String>>();
		vcard.append(VCARD_BEGIN);
		vcard.append(VCARD_VERSION);
	}
	
	public void setRawInfo(String cardinfo) {
		mRawInfo = cardinfo;
	}
	public String getRawInfo() {
		return mRawInfo;
	}
	
	public void setName(String name) {
		String nName = "N:" + name + "\n\n";
		vcard.append(nName);
		String fnName = "FN:" + name + "\n\n";
		vcard.append(fnName);
		mName = name;
		Log.v(LOG_TAG, "Name=" + name);
	}
	public String getName() {
		return mName;
	}
	public void setTel(int type, String number) {
		String tel = "TEL;";
		switch(type) {
		case ContactsContract.CommonDataKinds.Phone.TYPE_HOME:
			tel += "HOME:" + number;
			break;
		case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE:
			tel += "CELL:" + number;
			break;
		case ContactsContract.CommonDataKinds.Phone.TYPE_WORK:
			tel += "WORK:" + number;
			break;
		default:
			tel += "OTHER:" + number;
			break;
		}
		tel += "\n\n";
		HashMap<String, String> item = new HashMap<String, String>();
		item.put(TEL_TYPE, Integer.toString(type));
		item.put(TEL_NUM, number);
		mTels.add(item);
		vcard.append(tel);
		Log.v(LOG_TAG, "tel=" + tel);
	}
	
	public ArrayList<HashMap<String, String>> getTels() {
		return mTels;
	}
	public void setEmail(int type, String address) {
		String email = "EMAIL;";
		switch(type) {
		case ContactsContract.CommonDataKinds.Email.TYPE_HOME:
			email += "HOME:" + address;
			break;
		case ContactsContract.CommonDataKinds.Email.TYPE_MOBILE:
			email += "CELL:" + address;
			break;
		case ContactsContract.CommonDataKinds.Email.TYPE_WORK:
			email += "WORK:" + address;
			break;
		default:
			email += "OTHER:" + address;
			break;
		}
		email += "\n\n";
		HashMap<String, String> item = new HashMap<String, String>();
		item.put(EMAIL_TYPE, Integer.toString(type));
		item.put(EMAIL_ADDR, address);
		mEmail.add(item);
		vcard.append(email);
		Log.v(LOG_TAG, "email=" + email);
	}
	public ArrayList<HashMap<String, String>> getEmail() {
		return mEmail;
	}
	public String toString() {
		String ret = "";
		if(mRawInfo.equals("")) {
			vcard.append(VCARD_END);
			ret = vcard.toString();
		}
		else {
			ret = mRawInfo;
		}
		//Log.v(LOG_TAG, "Vcard=" + ret);
		return ret;
	}
}