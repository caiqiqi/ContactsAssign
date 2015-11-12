package caiqiqi.contacts;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.provider.CallLog;
import android.provider.CallLog.Calls;
import android.app.AlertDialog;
import android.app.TabActivity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
//import android.content.ContentResolver;
import android.database.Cursor;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.Window;
import android.widget.AdapterView.OnItemClickListener;
//import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
//import android.widget.AutoCompleteTextView.Validator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
//import android.view.Menu;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract;

@SuppressWarnings("deprecation")
public class MainActivity extends TabActivity {
	ImageView robertImage;
	EditText inputText=null;//����绰�������ʾ��
	Button deleteButton=null;//ɾ��
	GridView keyBoardView=null;//����
	Button switchButton=null;//չ��������
	Button callButton=null;//��绰
	Button addButton=null;//�������ϵ��
	Vibrator vibrator=null;
	String[] number=null;//Map�е���ϵ�˺���
    int[] typeInt=null;//�����ȥ�硣����
    String [] typeString=null;//ͬ��
    String[] cachedName=null;
    String[] dateString=null;
    String[] duration=null;
    String[] nameInContacts=null;
    String[] numberInContacts=null;
 	final Uri uri=CallLog.Calls.CONTENT_URI;
 	final Uri uriContacts=Phone.CONTENT_URI;
 	final String[] projection={CallLog.Calls.CACHED_NAME,CallLog.Calls.NUMBER,CallLog.Calls.TYPE,CallLog.Calls.DURATION,CallLog.Calls.DATE};
 	final String[] keyBoardNumber={"1","2","3","4","5","6","7","8","9","*","0","#"};
 	final String[] PHONES_PROJECTION={Phone.DISPLAY_NAME,Phone.NUMBER};
 	final String sortOrder=Phone.DISPLAY_NAME + " COLLATE LOCALIZED ASC";
 	final int OUT=0x111;
 	final int EXIT=0x112;
 	String numberUChose=null;
 	String whoUChose=null;
 	List<Map<String,Object>> listItems=null;
 	Map<String,Object> listItem=null;
 	List<Map<String,Object>> listContacts=null;
 	Map<String,Object> listContact=null;
 	int positionInList;//ȫ�ֱ����������õ���������item��position
 	long idInList;//
 	SimpleAdapter callLogsListAdapter=null;//��ͨ����¼����
 	SimpleAdapter contactsListAdapter=null;//����ϵ������
 	ListView callLogsListView=null;//ͨ����¼ListView
 	ListView contactsListView=null;//��ϵ��ListView
 	int amountsOfName;//ContactsContract.Contacts._COUNT;//��ϵ������
 	AudioManager audioManager=null;//���龳ģʽ���
    //@SuppressLint("SimpleDateFormat") @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//���������setcontentview֮ǰ����Ȼ���д�
        setContentView(R.layout.main);
        robertImage=(ImageView)findViewById(R.id.Robert_Standard);
		robertImage.setVisibility(View.VISIBLE); 
		AlphaAnimation alphaAnimation = new AlphaAnimation((float) 0.1, 1);   
        alphaAnimation.setDuration(5000);//�趨����ʱ��   
        alphaAnimation.setAnimationListener(new AnimationListener() {   
            @Override  
            public void onAnimationStart(Animation animation) {   
            }   
  
            @Override  
            public void onAnimationRepeat(Animation animation) {   
            }   
  
            @Override  
            public void onAnimationEnd(Animation animation) {   
            	robertImage.setVisibility(View.GONE);
            }   
        });   
  
        robertImage.setAnimation(alphaAnimation);  
        vibrator=(Vibrator)getSystemService(VIBRATOR_SERVICE);
        audioManager=(AudioManager)getSystemService(Context.AUDIO_SERVICE);
        number=new String[100];//����
        typeInt=new int[100];
        typeString=new String[100];
        cachedName=new String[100];
        dateString=new String[100];
        duration=new String[100];
        TabHost tabHost=getTabHost();
        TabSpec tab1=tabHost.newTabSpec("Tab1").setIndicator("����").setContent(R.id.tab01);
        TabSpec tab2=tabHost.newTabSpec("Tab2").setIndicator("ͨ����¼").setContent(R.id.tab02);
        TabSpec tab3=tabHost.newTabSpec("Tab3").setIndicator("ͨѶ¼").setContent(R.id.tab03);
        tabHost.addTab(tab1);
        tabHost.addTab(tab2);
        tabHost.addTab(tab3);
        listItems=new ArrayList<Map<String,Object>>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Cursor cursor=getContentResolver().query(uri, projection, null, null, CallLog.Calls.DEFAULT_SORT_ORDER);
        for (int i = 0; i < 100; i++) {//�ԣ��������ﲻ��ʹcursor.getCount������100�Ϳ����ˣ�û��Contacts��ֹͣ����ʲô��
            cursor.moveToPosition(i);
            dateString[i]=dateFormat.format(new Date(Long.parseLong(cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.DATE)))));//.toString();
            number[i] = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER));
            typeInt[i] = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.TYPE));//��ʼû��typeInt��typeString����ʼֻ�ܰ�int��ֵ����string������
            switch(typeInt[i]){
            case Calls.INCOMING_TYPE:typeString[i]="�ѽ�";break;//��ʼ����break�����
            case Calls.OUTGOING_TYPE:typeString[i]="�Ѳ�";break;
            case Calls.MISSED_TYPE:typeString[i]="δ��";break;
            }
            cachedName[i] = cursor.getString(cursor
              .getColumnIndex(CallLog.Calls.CACHED_NAME));// �����������绰���룬������Ĵ���
            duration[i]="ͨ��ʱ��Ϊ��"+cursor.getString(cursor
                    .getColumnIndex(CallLog.Calls.DURATION))+"��";
            
            listItem=new HashMap<String,Object>();//��Map�������for��ͷ��Ҫ����ֵ��ʾһ��ͨ����¼
            listItem.put("cachedName", cachedName[i]);
            listItem.put("number", number[i]);
            listItem.put("typeString", typeString[i]);
            listItem.put("duration", duration[i]);
            listItem.put("dateString", dateString[i]);
            listItems.add(listItem);//������ÿ��ִ��forѭ������β��ʱ�򶼴���һ����i�ε�HashMap������ArrayList�о���100��HashMap�ˡ�
        }
        cursor.close();
        callLogsListAdapter=new SimpleAdapter(MainActivity.this, listItems, R.layout.simple_item, new String[] {"cachedName", "number", "typeString", "duration", "dateString"},new int[] {R.id.cachedName,R.id.number,R.id.typeString,R.id.duration,R.id.dateString});
        callLogsListView=(ListView)findViewById(R.id.CallLogsList);
        callLogsListView.setAdapter(callLogsListAdapter);
        callLogsListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				// TODO Auto-generated method stub
				Map<String,Object> selected=(Map<String,Object>)parent.getItemAtPosition(position);
		         // String numberToCall=selected.getText().toString();//������ֱ��selec.toString�ɲ����ԣ������ԵĻ���getText��toString��
				//Map<String,Object> selected=listItems.get(position);//�����listItemsΪʲô����Ҫ��final�أ�
				String numberToCall=(String)selected.get("number");
				Intent call=new Intent("android.intent.action.CALL",Uri.parse("tel:"+numberToCall));
				MainActivity.this.startActivity(call);
			}
        });
        callLogsListView.setOnItemLongClickListener(new OnItemLongClickListener() {//�Ǻǣ���ʼ��д����OnLongClickListener����˵��ôֻ��һ�������أ��� �����ż����ֲ�����д����������һЩ������

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,//Ŷ��AdapterView��ListView�ĸ���
					int position, long id) {
				// TODO Auto-generated method stub
				//whoUChose=cachedName[position];//��Щ����Map���γɺõ���˳������ݣ����Ƕ�̬��
				//numberUChose=number[position];//��Щ����Map���γɺõ���˳������ݣ����Ƕ�̬��
				positionInList=position;
				idInList=id;
				//listAdapter.getItem(position);//Ŷ��adapter���Ǳ���ˣ�ֱ�Ӹ�listview��
				Map<String,Object> selected=(Map<String,Object>)parent.getItemAtPosition(position);//�������Ͷ��ˣ��õõ�һ���µ�Map
				whoUChose=(String)selected.get("cachedName");
				numberUChose=(String)selected.get("number");
		            AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this);
		            final  String[] whatToDo=new String[]{"���Ͷ���","ɾ�������¼"};//�����Ƿ������棬��������private���Σ�
		            //���öԻ���ı���
		            builder.setTitle(whoUChose);//��øĳɵ���������
		            //��Ӱ�ť��android.content.DialogInterface.OnClickListener.OnClickListener
		            builder.setItems(whatToDo, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							if(whatToDo[0]==whatToDo[which]){//���Ͷ���
								Uri smsToUri = Uri.parse("smsto:"+numberUChose);    
							    Intent Intent = new Intent( android.content.Intent.ACTION_SENDTO, smsToUri );
							    startActivity(Intent);
							}else if(whatToDo[1]==whatToDo[which]){//ɾ�������¼
								listItems.remove(positionInList);//��һ��Ӧ����һ�����߳�������?
								//ȱ�㣺����ֻ��ɾ��ListView�еģ����������ݿ��е�
								//ɾ�������ݿ��еļ�¼������Activity���´ε��õ�ʱ��Ͳ�������ʾɾ������������
								MainActivity.this.getContentResolver().delete(uri, "_id=?", new String[]{idInList+""});//��Ȼɾ�������ݿ��е����ݡ������ѵ�ֻ��ϵͳ�Դ��ĳ�����ɾ��
								callLogsListAdapter.notifyDataSetChanged();//�ղ�Ҳ��֤�ˣ����Ұ�SimpleAdapter����onCreate�ڶ���ʱ��eclipse������û��listAdapter����ʾ������onCreate֮�ⶨ������е�,���⣬��������ԭ�ȳ��ֵ�ANR�Ͳ����ˣ����Ҹ��µĺܿ죬���������Ǿ���ʲôЧ��
								callLogsListView.invalidate();//��UI�߳������(main threadҲ��UI threadҲ��UI�̡߳�)
							}
						}
		            });
		            builder.create().show();
				return false;
			}
        	
		});
        List<Map<String,Object>> Items=new ArrayList<Map<String,Object>>();
        Map<String,Object> Item;
        for(int i=0;i<keyBoardNumber.length;i++){
        	Item=new HashMap<String, Object>();
        	Item.put("keyBoardNumber", keyBoardNumber[i]);
        	Items.add(Item);
        }
        SimpleAdapter textAdapter=new SimpleAdapter(this, Items, R.layout.text_item, new String[]{"keyBoardNumber"}, new int[]{R.id.keyBoardNumber});
        inputText=(EditText)findViewById(R.id.inputNumber);//��ʾ��
        deleteButton=(Button)findViewById(R.id.deleteButton);//ɾ����ť
        deleteButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String result=inputText.getText().toString();
				if(isEmpty(result)){
					return;
				}
				else{
				inputText.setText(result.substring(0, result.length()-1)) ;//�õ�����string����string
				}
			}
		});
        deleteButton.setOnLongClickListener(new OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				// TODO Auto-generated method stub
				if(AudioManager.RINGER_MODE_NORMAL==audioManager.getRingerMode()){
					vibrator.vibrate(30);
		        }
				
				inputText.setText("");
				return false;
			}
		});
        keyBoardView=(GridView)findViewById(R.id.keyBoard);//���ż��̵�������ͼ
        keyBoardView.setAdapter(textAdapter);//�����ϲ���˵������ListAdapter����ôSimpleAdapterҲ��
        keyBoardView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				// TODO Auto-generated method stub
				//Vibrator vibrator=(Vibrator)getSystemService(VIBRATOR_SERVICE);
				if(audioManager.getRingerMode()==AudioManager.RINGER_MODE_NORMAL){
					vibrator.vibrate(30);
		        }
				inputText.append(keyBoardNumber[position]);
				//inputText.setText(inputText.getText()+keyBoardNumber[position]);//������������ɵ�
			}
        	
		});
        switchButton=(Button)findViewById(R.id.switchButton);
        switchButton.setText("����");
        switchButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(switchButton.getText()=="����"){
				keyBoardView.setVisibility(View.INVISIBLE);
				switchButton.setText("չ��");
				}else if(switchButton.getText()=="չ��"){
					keyBoardView.setVisibility(View.VISIBLE);
					switchButton.setText("����");
				}
			}
		});
        callButton=(Button)findViewById(R.id.callButton);
        callButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String numberToCall=inputText.getText().toString();//���û�л᷵��һ��ʲô���ͣ�
				if(!isEmpty(numberToCall)){
				//if(numberToCall!=null){
				Intent callIntent=new Intent("android.intent.action.CALL", Uri.parse("tel:"+numberToCall));
				MainActivity.this.startActivity(callIntent);
				}
			}
		});
        addButton=(Button)findViewById(R.id.addButton);
        //����ͨѶ¼��
        contactsListView=(ListView)findViewById(R.id.ContactsList);
        Cursor cursorContacts=getContentResolver().query(Phone.CONTENT_URI, PHONES_PROJECTION, null, null, Phone.DISPLAY_NAME + " COLLATE LOCALIZED ASC");
        amountsOfName=MainActivity.this.getContactCount(MainActivity.this);
        nameInContacts=new String[amountsOfName];
        numberInContacts=new String[amountsOfName];
        listContacts=new ArrayList<Map<String,Object>>();
        if(cursorContacts.moveToFirst()){
        for(int i=0;i<amountsOfName;i++){
        	cursorContacts.moveToPosition(i);//ɵ�ƣ���ʼ���󶼲��ӣ�����
        	nameInContacts[i]=cursorContacts.getString(cursorContacts.getColumnIndexOrThrow(Phone.DISPLAY_NAME));
        	numberInContacts[i] = cursorContacts.getString(cursorContacts.getColumnIndexOrThrow(Phone.NUMBER));
        	listContact=new HashMap<String, Object>();
        	listContact.put("nameInContacts", nameInContacts[i]);
        	listContact.put("numberInContacts", numberInContacts[i]);
        	listContacts.add(listContact);
        }
        }
        cursorContacts.close();
        contactsListAdapter=new SimpleAdapter(MainActivity.this, listContacts, R.layout.simple_item_contacts, new String[]{"nameInContacts","numberInContacts"}, new int[]{R.id.nameInContacts,R.id.numberInContacts});
        contactsListView.setAdapter(contactsListAdapter);
        contactsListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				// TODO Auto-generated method stub
				Map<String,Object> selected=(Map<String,Object>)parent.getItemAtPosition(position);
				String numberToCall=(String)selected.get("numberInContacts");
				Intent call=new Intent("android.intent.action.CALL",Uri.parse("tel:"+numberToCall));
				MainActivity.this.startActivity(call);
			}
        	
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	// TODO Auto-generated method stub
    	menu.add(0, OUT, 0, "����");
    	menu.add(0, EXIT, 0, "�˳�");
    	return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	// TODO Auto-generated method stub
    	switch(item.getItemId()){
    	case OUT: 
    		/*String[] items=new String[]{"�ڲ��洢","SD��"};
    		AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this);
    		builder.setTitle("ѡ�񵼳�·��");
    		builder.setItems(items, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							
						}
					});
					*///��ѡ�񵼳�·���Ĺ��ܻ�ûʵ��
    		
    		Thread newThread;
    		newThread=new Thread(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					ContentResolver cr = getContentResolver();
    	    		Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,null, null, null, null);
    	    		int index = cur.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY); 
    	    		FileOutputStream fout;
    	    		try {
    	    		//String basedpath = Environment.getExternalStorageDirectory().getAbsolutePath();
    	    			String basedpath ="/storage/sdcard0";
    	    			String path = basedpath+"/tt.vcf";
    	    			//Toast.makeText(MainActivity.this,path , Toast.LENGTH_LONG).show();
    	    			File f = new File(path);
    	    			f.createNewFile();
    	    			fout = new FileOutputStream(path);
    	    			byte[] data = new byte[1024 * 1];
    	    			while(cur.moveToNext()){
    	    				String lookupKey = cur.getString(index);
    	    				Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_VCARD_URI, lookupKey);//�����е�baseUri�ϼ���path(lookupkey��Uri�õ�һ���µ�Uri)
    	    		
    	    				AssetFileDescriptor fd = MainActivity.this.getContentResolver().openAssetFileDescriptor(uri, "r");//r��ʾread-only access;w ��ʾwrite-only access;
    	    				FileInputStream fin = fd.createInputStream();
    	    				int len = -1;
    	    				while((len = fin.read(data)) != -1){
    	    					fout.write(data, 0, len);
    	    				}
    	    				fin.close();
    	    			}
    	    			fout.close();
    	    			//Toast.makeText(MainActivity.this,"�����ɹ�" , Toast.LENGTH_LONG).show();
    	    			} catch (Exception e) {
    	    				e.printStackTrace();
    	    			}
					}
				});
    			newThread.start();
    		if(true){//��û����ж������ʲô������
    			Toast.makeText(MainActivity.this,"�����ɹ�" , Toast.LENGTH_LONG).show();
    		}
    			break;
    		case EXIT: 
    			MainActivity.this.finish();
    			break;
    		}
    		return super.onOptionsItemSelected(item);
    }
    private boolean isEmpty(String str){
    	
    	    return (str==null || str.trim().length()==0);//û�л���Ϊ��
    	}
    private int getContactCount(Context context){   
        Cursor c=context.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, new String[]{ContactsContract.Contacts._COUNT}, null, null, null);   
        try{   
            c.moveToFirst();   
            return c.getInt(0);   
        }catch(Exception e){   
            return 0;   
        }finally{   
            c.close();   
        }   
    }  
}
