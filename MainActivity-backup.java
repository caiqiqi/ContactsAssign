package caiqiqi.contacts;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.net.Uri;
import android.os.Bundle;
import android.provider.CallLog;
import android.provider.CallLog.Calls;
import android.annotation.SuppressLint;
//import android.app.Activity;
//import android.app.Activity;
import android.app.TabActivity;
//import android.content.ContentResolver;
import android.database.Cursor;
//import android.view.View;
//import android.widget.AdapterView;
//import android.widget.AdapterView.OnItemClickListener;
//import android.widget.SimpleAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
//import android.view.Menu;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

@SuppressWarnings("deprecation")
public class MainActivity extends TabActivity {
	String[] number=null;//����
    int[] typeInt=null;
    String [] typeString=null;
    String[] cachedName=null;
    String[] dateString=null;
    String[] duration=null;
	Uri uri=CallLog.Calls.CONTENT_URI;
	String[] projection={CallLog.Calls.CACHED_NAME,CallLog.Calls.NUMBER,CallLog.Calls.TYPE,CallLog.Calls.DURATION,CallLog.Calls.DATE};
    @SuppressLint("SimpleDateFormat") @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        //TabHost tabHost=(TabHost)findViewById(android.R.id.tabs);
        //tabHost.setup();//��ʼû�����
        TabHost tabHost=getTabHost();
        TabSpec tab1=tabHost.newTabSpec("Tab1").setIndicator("����").setContent(R.id.tab01);
        TabSpec tab2=tabHost.newTabSpec("Tab2").setIndicator("ͨ����¼").setContent(R.id.tab02);
        TabSpec tab3=tabHost.newTabSpec("Tab3").setIndicator("ͨѶ¼").setContent(R.id.tab03);
        tabHost.addTab(tab1);
        tabHost.addTab(tab2);
        tabHost.addTab(tab3);
        List<Map<String,Object>> listItems=new ArrayList<Map<String,Object>>();
        //ContentResolver resolver=getContentResolver();//���룬���֣����ͣ����뻹�Ǻ�������δ�����磩�����ڣ�ͨ��ʱ��
        //final Cursor cursor = resolver.query(CallLog.Calls.CONTENT_URI,new String[]{CallLog.Calls.NUMBER,CallLog.Calls.CACHED_NAME,CallLog.Calls.TYPE, CallLog.Calls.DATE,CallLog.Calls.DURATION},null, null,CallLog.Calls.DEFAULT_SORT_ORDER);
        //cursor.moveToPosition(0);
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Cursor cursor=getContentResolver().query(uri, projection, null, null, CallLog.Calls.DEFAULT_SORT_ORDER);
        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++) {
            cursor.moveToPosition(i);
            Date date = new Date(Long.parseLong(cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.DATE))));
            //Date date=new Date(cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE)));
            dateString[i]=dateFormat.format(date);//.toString();
            number[i] = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER));
            typeInt[i] = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.TYPE));//��ʼû��typeInt��typeString����ʼֻ�ܰ�int��ֵ����string������
            switch(typeInt[i]){
            case Calls.INCOMING_TYPE:typeString[i]="�ѽ�";break;//��ʼ����break�����
            case Calls.OUTGOING_TYPE:typeString[i]="�Ѳ�";break;
            case Calls.MISSED_TYPE:typeString[i]="δ��";break;
            }
            cachedName[i] = cursor.getString(cursor
              .getColumnIndex(CallLog.Calls.CACHED_NAME));// �����������绰���룬������Ĵ���
            duration[i]=cursor.getString(cursor
                    .getColumnIndex(CallLog.Calls.DURATION));
            Map<String,Object> listItem=new HashMap<String,Object>();
            listItem.put("cachedName", cachedName[i]);
            listItem.put("number", number[i]);
            listItem.put("typeString", typeString[i]);
            listItem.put("duration", duration[i]);
            listItem.put("dateString", dateString[i]);
            listItems.add(listItem);
        }
        SimpleAdapter simpleAdapter=new SimpleAdapter(this, listItems, R.layout.simple_item, new String[] {"cachedName", "number", "typeString", "duration", "dateString"},new int[] {R.id.cachedName,R.id.number,R.id.typeString,R.id.duration,R.id.dateString});
        ListView listView=(ListView)findViewById(R.id.myList);
        listView.setAdapter(simpleAdapter);
        /*listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				// TODO Auto-generated method stub
				
			}
        	
        });*/
    }
}
