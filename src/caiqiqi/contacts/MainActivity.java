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
	EditText inputText=null;//输入电话号码的显示框
	Button deleteButton=null;//删除
	GridView keyBoardView=null;//键盘
	Button switchButton=null;//展开与收起
	Button callButton=null;//打电话
	Button addButton=null;//添加至联系人
	Vibrator vibrator=null;
	String[] number=null;//Map中的联系人号码
    int[] typeInt=null;//来电或去电。。。
    String [] typeString=null;//同上
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
 	int positionInList;//全局变量，用来得到被长按的item的position
 	long idInList;//
 	SimpleAdapter callLogsListAdapter=null;//与通话记录相配
 	SimpleAdapter contactsListAdapter=null;//与联系人相配
 	ListView callLogsListView=null;//通话记录ListView
 	ListView contactsListView=null;//联系人ListView
 	int amountsOfName;//ContactsContract.Contacts._COUNT;//联系人数量
 	AudioManager audioManager=null;//与情境模式相关
    //@SuppressLint("SimpleDateFormat") @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//这个必须在setcontentview之前，不然就有错
        setContentView(R.layout.main);
        robertImage=(ImageView)findViewById(R.id.Robert_Standard);
		robertImage.setVisibility(View.VISIBLE); 
		AlphaAnimation alphaAnimation = new AlphaAnimation((float) 0.1, 1);   
        alphaAnimation.setDuration(5000);//设定动画时间   
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
        number=new String[100];//号码
        typeInt=new int[100];
        typeString=new String[100];
        cachedName=new String[100];
        dateString=new String[100];
        duration=new String[100];
        TabHost tabHost=getTabHost();
        TabSpec tab1=tabHost.newTabSpec("Tab1").setIndicator("拨号").setContent(R.id.tab01);
        TabSpec tab2=tabHost.newTabSpec("Tab2").setIndicator("通话记录").setContent(R.id.tab02);
        TabSpec tab3=tabHost.newTabSpec("Tab3").setIndicator("通讯录").setContent(R.id.tab03);
        tabHost.addTab(tab1);
        tabHost.addTab(tab2);
        tabHost.addTab(tab3);
        listItems=new ArrayList<Map<String,Object>>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Cursor cursor=getContentResolver().query(uri, projection, null, null, CallLog.Calls.DEFAULT_SORT_ORDER);
        for (int i = 0; i < 100; i++) {//对，就是这里不能使cursor.getCount，换成100就可以了，没有Contacts已停止运行什么的
            cursor.moveToPosition(i);
            dateString[i]=dateFormat.format(new Date(Long.parseLong(cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.DATE)))));//.toString();
            number[i] = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER));
            typeInt[i] = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.TYPE));//开始没有typeInt和typeString，开始只能把int的值赋给string，出错
            switch(typeInt[i]){
            case Calls.INCOMING_TYPE:typeString[i]="已接";break;//开始忘加break语句了
            case Calls.OUTGOING_TYPE:typeString[i]="已拨";break;
            case Calls.MISSED_TYPE:typeString[i]="未接";break;
            }
            cachedName[i] = cursor.getString(cursor
              .getColumnIndex(CallLog.Calls.CACHED_NAME));// 缓存的名称与电话号码，如果它的存在
            duration[i]="通话时长为："+cursor.getString(cursor
                    .getColumnIndex(CallLog.Calls.DURATION))+"秒";
            
            listItem=new HashMap<String,Object>();//这Map还必须放for里头，要不就值显示一项通话记录
            listItem.put("cachedName", cachedName[i]);
            listItem.put("number", number[i]);
            listItem.put("typeString", typeString[i]);
            listItem.put("duration", duration[i]);
            listItem.put("dateString", dateString[i]);
            listItems.add(listItem);//这样，每次执行for循环到结尾的时候都传入一个第i次的HashMap，这样ArrayList中就有100个HashMap了。
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
		         // String numberToCall=selected.getText().toString();//看这样直接selec.toString可不可以，不可以的话先getText再toString咯
				//Map<String,Object> selected=listItems.get(position);//这里的listItems为什么必须要是final呢？
				String numberToCall=(String)selected.get("number");
				Intent call=new Intent("android.intent.action.CALL",Uri.parse("tel:"+numberToCall));
				MainActivity.this.startActivity(call);
			}
        });
        callLogsListView.setOnItemLongClickListener(new OnItemLongClickListener() {//呵呵，开始我写的是OnLongClickListener，是说怎么只有一个参数呢，让 好生着急，又不能重写方法（增加一些参数）

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,//哦，AdapterView是ListView的父类
					int position, long id) {
				// TODO Auto-generated method stub
				//whoUChose=cachedName[position];//这些都是Map中形成好的有顺序的数据，不是动态的
				//numberUChose=number[position];//这些都是Map中形成好的有顺序的数据，不是动态的
				positionInList=position;
				idInList=id;
				//listAdapter.getItem(position);//哦，adapter还是别改了，直接改listview吧
				Map<String,Object> selected=(Map<String,Object>)parent.getItemAtPosition(position);//恩这样就对了，得得到一个新的Map
				whoUChose=(String)selected.get("cachedName");
				numberUChose=(String)selected.get("number");
		            AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this);
		            final  String[] whatToDo=new String[]{"发送短信","删除该项记录"};//这里是方法里面，好像不能用private修饰？
		            //设置对话框的标题
		            builder.setTitle(whoUChose);//最好改成点击项的人名
		            //添加按钮，android.content.DialogInterface.OnClickListener.OnClickListener
		            builder.setItems(whatToDo, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							if(whatToDo[0]==whatToDo[which]){//发送短信
								Uri smsToUri = Uri.parse("smsto:"+numberUChose);    
							    Intent Intent = new Intent( android.content.Intent.ACTION_SENDTO, smsToUri );
							    startActivity(Intent);
							}else if(whatToDo[1]==whatToDo[which]){//删除该项记录
								listItems.remove(positionInList);//下一行应该在一个新线程中运行?
								//缺点：这样只是删除ListView中的，而不是数据库中的
								//删除在数据库中的记录，这样Activity在下次调用的时候就不会再显示删除过的内容了
								MainActivity.this.getContentResolver().delete(uri, "_id=?", new String[]{idInList+""});//依然删不掉数据库中的数据。。。难道只有系统自带的程序能删？
								callLogsListAdapter.notifyDataSetChanged();//刚才也验证了，当我把SimpleAdapter放在onCreate内定义时，eclipse根本就没有listAdapter的提示，放在onCreate之外定义这才有的,另外，加上这句后，原先出现的ANR就不见了，而且更新的很快，看看下面那句有什么效果
								callLogsListView.invalidate();//在UI线程里调用(main thread也叫UI thread也即UI线程。)
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
        inputText=(EditText)findViewById(R.id.inputNumber);//显示框
        deleteButton=(Button)findViewById(R.id.deleteButton);//删除按钮
        deleteButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String result=inputText.getText().toString();
				if(isEmpty(result)){
					return;
				}
				else{
				inputText.setText(result.substring(0, result.length()-1)) ;//得到对象string的子string
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
        keyBoardView=(GridView)findViewById(R.id.keyBoard);//拨号键盘的网格视图
        keyBoardView.setAdapter(textAdapter);//参数上不是说必须是ListAdapter吗，怎么SimpleAdapter也行
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
				//inputText.setText(inputText.getText()+keyBoardNumber[position]);//哈哈这句是我蒙的
			}
        	
		});
        switchButton=(Button)findViewById(R.id.switchButton);
        switchButton.setText("收起");
        switchButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(switchButton.getText()=="收起"){
				keyBoardView.setVisibility(View.INVISIBLE);
				switchButton.setText("展开");
				}else if(switchButton.getText()=="展开"){
					keyBoardView.setVisibility(View.VISIBLE);
					switchButton.setText("收起");
				}
			}
		});
        callButton=(Button)findViewById(R.id.callButton);
        callButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String numberToCall=inputText.getText().toString();//如果没有会返回一个什么类型？
				if(!isEmpty(numberToCall)){
				//if(numberToCall!=null){
				Intent callIntent=new Intent("android.intent.action.CALL", Uri.parse("tel:"+numberToCall));
				MainActivity.this.startActivity(callIntent);
				}
			}
		});
        addButton=(Button)findViewById(R.id.addButton);
        //关于通讯录的
        contactsListView=(ListView)findViewById(R.id.ContactsList);
        Cursor cursorContacts=getContentResolver().query(Phone.CONTENT_URI, PHONES_PROJECTION, null, null, Phone.DISPLAY_NAME + " COLLATE LOCALIZED ASC");
        amountsOfName=MainActivity.this.getContactCount(MainActivity.this);
        nameInContacts=new String[amountsOfName];
        numberInContacts=new String[amountsOfName];
        listContacts=new ArrayList<Map<String,Object>>();
        if(cursorContacts.moveToFirst()){
        for(int i=0;i<amountsOfName;i++){
        	cursorContacts.moveToPosition(i);//傻逼，开始这句后都不加！！！
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
    	menu.add(0, OUT, 0, "导出");
    	menu.add(0, EXIT, 0, "退出");
    	return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	// TODO Auto-generated method stub
    	switch(item.getItemId()){
    	case OUT: 
    		/*String[] items=new String[]{"内部存储","SD卡"};
    		AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this);
    		builder.setTitle("选择导出路径");
    		builder.setItems(items, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							
						}
					});
					*///能选择导出路径的功能还没实现
    		
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
    	    				Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_VCARD_URI, lookupKey);//在已有的baseUri上加入path(lookupkey的Uri得到一个新的Uri)
    	    		
    	    				AssetFileDescriptor fd = MainActivity.this.getContentResolver().openAssetFileDescriptor(uri, "r");//r表示read-only access;w 表示write-only access;
    	    				FileInputStream fin = fd.createInputStream();
    	    				int len = -1;
    	    				while((len = fin.read(data)) != -1){
    	    					fout.write(data, 0, len);
    	    				}
    	    				fin.close();
    	    			}
    	    			fout.close();
    	    			//Toast.makeText(MainActivity.this,"导出成功" , Toast.LENGTH_LONG).show();
    	    			} catch (Exception e) {
    	    				e.printStackTrace();
    	    			}
					}
				});
    			newThread.start();
    		if(true){//还没想好判断语句是什么。。。
    			Toast.makeText(MainActivity.this,"导出成功" , Toast.LENGTH_LONG).show();
    		}
    			break;
    		case EXIT: 
    			MainActivity.this.finish();
    			break;
    		}
    		return super.onOptionsItemSelected(item);
    }
    private boolean isEmpty(String str){
    	
    	    return (str==null || str.trim().length()==0);//没有或者为空
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
