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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract;

@SuppressWarnings("deprecation")
public class MainActivity extends TabActivity {
	
	private ImageView mImage;
	private EditText mET_phoneNum;//输入电话号码的显示框
	private Button mBtn_delete;//删除
	private GridView mGridView_keyboard;//键盘
	private Button mBtnSwitch;//展开与收起
	private Button mBtnCall;//打电话
	private Button mBtnAdd;//添加至联系人
	private Vibrator mVibrator;
	private String[] numberStr;//Map中的联系人号码
	private int[] typeInt;//来电或去电。。。
	private String [] typeStr;//同上
	private String[] cachedNameStr;
	private String[] dateStr;
	private String[] durationStr;
	private String[] nameInContacts;
	private String[] numberInContacts;
	private final Uri uri_call=CallLog.Calls.CONTENT_URI;
	private final Uri uri_contacts=Phone.CONTENT_URI;
	private final String[] projection={CallLog.Calls.CACHED_NAME,CallLog.Calls.NUMBER,CallLog.Calls.TYPE,CallLog.Calls.DURATION,CallLog.Calls.DATE};
	private final String[] keyBoardNumber={"1","2","3","4","5","6","7","8","9","*","0","#"};
	private final String[] PHONES_PROJECTION={Phone.DISPLAY_NAME,Phone.NUMBER};
	private final String sortOrder=Phone.DISPLAY_NAME + " COLLATE LOCALIZED ASC";
	private final int OUT=0x111;
 	private final int EXIT=0x112;
 	private String mNumberUChose;
 	private String mWhoUChose;
 	private List<Map<String,Object>> mList_Items;
 	private Map<String,Object> mMap_Item;
 	private List<Map<String,Object>> mList_contacts;
 	private Map<String,Object> map_contacts;
 	private int positionInt;//全局变量，用来得到被长按的item的position
 	private long idLong;//
 	private SimpleAdapter callLogsListAdapter;//与通话记录相配
 	private SimpleAdapter contactsListAdapter;//与联系人相配
 	private ListView mCallLogsListView;//通话记录ListView
 	private ListView mContactsListView;//联系人ListView
 	private int amountsOfNameInt;//ContactsContract.Contacts._COUNT;//联系人数量
 	private AudioManager mAudioManager;//与情境模式相关
 	

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//这个必须在setcontentview之前，不然就有错
        setContentView(R.layout.main);
        
        initView();
         
		initAnimation();  
		
		initDeleteBtn();

		initSwitchBtn();

		initCallBtn();
        
        initConstants();
        
        
        initTabHost();
        
        initObjects();
        
        initCallLogsListView();
        
        mList_Items=new ArrayList<Map<String,Object>>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Cursor cursor=getContentResolver().query(uri_call, projection, null, null, CallLog.Calls.DEFAULT_SORT_ORDER);
        for (int i = 0; i < 100; i++) {//对，就是这里不能使cursor.getCount，换成100就可以了，没有Contacts已停止运行什么的
            cursor.moveToPosition(i);
            dateStr[i]=dateFormat.format(new Date(Long.parseLong(cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.DATE)))));
            numberStr[i] = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER));
            typeInt[i] = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.TYPE));//开始没有typeInt和typeString，开始只能把int的值赋给string，出错
			
            switch (typeInt[i]) {
			case Calls.INCOMING_TYPE:
				typeStr[i] = "已接";
				break;
			case Calls.OUTGOING_TYPE:
				typeStr[i] = "已拨";
				break;
			case Calls.MISSED_TYPE:
				typeStr[i] = "未接";
				break;
			}
            cachedNameStr[i] = cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME));// 缓存的名称与电话号码，如果它的存在
            durationStr[i]="通话时长为："+cursor.getString(cursor
                    .getColumnIndex(CallLog.Calls.DURATION))+"秒";
            
            initItems(i);
        }
        cursor.close();
        callLogsListAdapter=new SimpleAdapter(MainActivity.this, mList_Items, R.layout.simple_item, new String[] {"cachedName", "number", "typeString", "duration", "dateString"},new int[] {R.id.cachedName,R.id.number,R.id.typeString,R.id.duration,R.id.dateString});
        
        List<Map<String,Object>> Items=new ArrayList<Map<String,Object>>();
        Map<String,Object> Item;
        for(int i=0;i<keyBoardNumber.length;i++){
        	Item=new HashMap<String, Object>();
        	Item.put("keyBoardNumber", keyBoardNumber[i]);
        	Items.add(Item);
        }
        SimpleAdapter textAdapter=new SimpleAdapter(this, Items, R.layout.text_item, new String[]{"keyBoardNumber"}, new int[]{R.id.keyBoardNumber});
        initKeyboardView(textAdapter);
        
        
        
        Cursor cursorContacts=getContentResolver().query(Phone.CONTENT_URI, PHONES_PROJECTION, null, null, Phone.DISPLAY_NAME + " COLLATE LOCALIZED ASC");
        amountsOfNameInt=MainActivity.this.getContactCount(MainActivity.this);
        nameInContacts=new String[amountsOfNameInt];
        numberInContacts=new String[amountsOfNameInt];
        mList_contacts=new ArrayList<Map<String,Object>>();
        if(cursorContacts.moveToFirst()){
        for(int i=0;i<amountsOfNameInt;i++){
        	cursorContacts.moveToPosition(i);//傻逼，开始这句后都不加！！！
        	nameInContacts[i]=cursorContacts.getString(cursorContacts.getColumnIndexOrThrow(Phone.DISPLAY_NAME));
        	numberInContacts[i] = cursorContacts.getString(cursorContacts.getColumnIndexOrThrow(Phone.NUMBER));
        	map_contacts=new HashMap<String, Object>();
        	map_contacts.put("nameInContacts", nameInContacts[i]);
        	map_contacts.put("numberInContacts", numberInContacts[i]);
        	mList_contacts.add(map_contacts);
        }
        }
        cursorContacts.close();
        contactsListAdapter=new SimpleAdapter(MainActivity.this, mList_contacts, R.layout.simple_item_contacts, new String[]{"nameInContacts","numberInContacts"}, new int[]{R.id.nameInContacts,R.id.numberInContacts});
        mContactsListView.setAdapter(contactsListAdapter);
        mContactsListView.setOnItemClickListener(new OnItemClickListener() {

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
	private void initConstants() {
		
		numberStr=new String[100];//号码
        typeInt=new int[100];
        typeStr=new String[100];
        cachedNameStr=new String[100];
        dateStr=new String[100];
        durationStr=new String[100];
        
	}
	private void initObjects() {
		mVibrator=(Vibrator)getSystemService(VIBRATOR_SERVICE);
        mAudioManager=(AudioManager)getSystemService(Context.AUDIO_SERVICE);
	}
	private void initItems(int i) {
		mMap_Item=new HashMap<String,Object>();
		mMap_Item.put("cachedName", cachedNameStr[i]);
		mMap_Item.put("number", numberStr[i]);
		mMap_Item.put("typeString", typeStr[i]);
		mMap_Item.put("duration", durationStr[i]);
		mMap_Item.put("dateString", dateStr[i]);
		mList_Items.add(mMap_Item);//这样，每次执行for循环到结尾的时候都传入一个第i次的HashMap，这样ArrayList中就有100个HashMap了。
	}

	private void initAnimation() {
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
            	mImage.setVisibility(View.GONE);
            }   
        });   
  
        mImage.setAnimation(alphaAnimation);
	}
	private void initView() {
		mBtnCall=(Button)findViewById(R.id.callButton);
        mImage=(ImageView)findViewById(R.id.Robert_Standard);
        mImage.setVisibility(View.VISIBLE);
		// 显示框
        mET_phoneNum=(EditText)findViewById(R.id.inputNumber);
        mBtnAdd=(Button)findViewById(R.id.addButton);
        //关于通讯录的
        mContactsListView=(ListView)findViewById(R.id.ContactsList);
		// 拨号键盘的网格视图
        mGridView_keyboard=(GridView)findViewById(R.id.keyBoard);
        mBtnSwitch=(Button)findViewById(R.id.switchButton);
		// 删除按钮
        mBtn_delete=(Button)findViewById(R.id.deleteButton);
        mCallLogsListView=(ListView)findViewById(R.id.CallLogsList);
	}
	
	private void initCallBtn() {
		
        mBtnCall.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String numberToCall=mET_phoneNum.getText().toString();
				if(!isEmpty(numberToCall)){
				//if(numberToCall!=null){
				Intent callIntent=new Intent("android.intent.action.CALL", Uri.parse("tel:"+numberToCall));
				MainActivity.this.startActivity(callIntent);
				}
			}
		});
	}
	private void initKeyboardView(SimpleAdapter textAdapter) {
		
        mGridView_keyboard.setAdapter(textAdapter);
        mGridView_keyboard.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				
				if(mAudioManager.getRingerMode()==AudioManager.RINGER_MODE_NORMAL){
					mVibrator.vibrate(30);
		        }
				mET_phoneNum.append(keyBoardNumber[position]);
				//inputText.setText(inputText.getText()+keyBoardNumber[position]);//哈哈这句是我蒙的
			}
        	
		});
	}
	
/**
 * 初始化开关按钮（展开，收起）
 */
	private void initSwitchBtn() {
		
        mBtnSwitch.setText("收起");
        mBtnSwitch.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(mBtnSwitch.getText()=="收起"){
				mGridView_keyboard.setVisibility(View.INVISIBLE);
				mBtnSwitch.setText("展开");
				}else if(mBtnSwitch.getText()=="展开"){
					mGridView_keyboard.setVisibility(View.VISIBLE);
					mBtnSwitch.setText("收起");
				}
			}
		});
	}

/**
 * 初始化删除拨出的号码按钮
 */
	private void initDeleteBtn() {
		
        mBtn_delete.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String result=mET_phoneNum.getText().toString();
				if(isEmpty(result)){
					return;
				}
				else{
				mET_phoneNum.setText(result.substring(0, result.length()-1)) ;//得到对象string的子string
				}
			}
		});
        mBtn_delete.setOnLongClickListener(new OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				// TODO Auto-generated method stub
				if(AudioManager.RINGER_MODE_NORMAL==mAudioManager.getRingerMode()){
					mVibrator.vibrate(30);
		        }
				
				mET_phoneNum.setText("");
				return false;
			}
		});
	}

/**
 * 初始化通话记录ListView
 */
	private void initCallLogsListView() {
		
        mCallLogsListView.setAdapter(callLogsListAdapter);
        mCallLogsListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				// TODO Auto-generated method stub
				Map<String,Object> selected=(Map<String,Object>)parent.getItemAtPosition(position);
				String numberToCall=(String)selected.get("number");
				Intent call=new Intent("android.intent.action.CALL",Uri.parse("tel:"+numberToCall));
				MainActivity.this.startActivity(call);
			}
        });
        mCallLogsListView.setOnItemLongClickListener(new OnItemLongClickListener() {//呵呵，开始我写的是OnLongClickListener，是说怎么只有一个参数呢，让 好生着急，又不能重写方法（增加一些参数）

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,//哦，AdapterView是ListView的父类
					int position, long id) {
				// TODO Auto-generated method stub
				//whoUChose=cachedName[position];//这些都是Map中形成好的有顺序的数据，不是动态的
				//numberUChose=number[position];//这些都是Map中形成好的有顺序的数据，不是动态的
				positionInt=position;
				idLong=id;
				//listAdapter.getItem(position);//哦，adapter还是别改了，直接改listview吧
				Map<String,Object> selected=(Map<String,Object>)parent.getItemAtPosition(position);//恩这样就对了，得得到一个新的Map
				mWhoUChose=(String)selected.get("cachedName");
				mNumberUChose=(String)selected.get("number");
		            AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this);
		            final  String[] whatToDo=new String[]{"发送短信","删除该项记录"};//这里是方法里面，好像不能用private修饰？
		            //设置对话框的标题
		            builder.setTitle(mWhoUChose);//最好改成点击项的人名
		            //添加按钮，android.content.DialogInterface.OnClickListener.OnClickListener
		            builder.setItems(whatToDo, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							if(whatToDo[0]==whatToDo[which]){//发送短信
								Uri smsToUri = Uri.parse("smsto:"+mNumberUChose);    
							    Intent Intent = new Intent( android.content.Intent.ACTION_SENDTO, smsToUri );
							    startActivity(Intent);
							}else if(whatToDo[1]==whatToDo[which]){//删除该项记录
								mList_Items.remove(positionInt);//下一行应该在一个新线程中运行?
								//缺点：这样只是删除ListView中的，而不是数据库中的
								//删除在数据库中的记录，这样Activity在下次调用的时候就不会再显示删除过的内容了
								MainActivity.this.getContentResolver().delete(uri_call, "_id=?", new String[]{idLong+""});//依然删不掉数据库中的数据。。。难道只有系统自带的程序能删？
								callLogsListAdapter.notifyDataSetChanged();//刚才也验证了，当我把SimpleAdapter放在onCreate内定义时，eclipse根本就没有listAdapter的提示，放在onCreate之外定义这才有的,另外，加上这句后，原先出现的ANR就不见了，而且更新的很快，看看下面那句有什么效果
								mCallLogsListView.invalidate();//在UI线程里调用(main thread也叫UI thread也即UI线程。)
							}
						}
		            });
		            builder.create().show();
				return false;
			}
        	
		});
	}

/**
 * 初始化TabHost（拨号，通话记录，通讯录）
 */
	private void initTabHost() {
		TabHost tabHost=getTabHost();
        TabSpec tab1=tabHost.newTabSpec("Tab1").setIndicator("拨号").setContent(R.id.tab01);
        TabSpec tab2=tabHost.newTabSpec("Tab2").setIndicator("通话记录").setContent(R.id.tab02);
        TabSpec tab3=tabHost.newTabSpec("Tab3").setIndicator("通讯录").setContent(R.id.tab03);
        tabHost.addTab(tab1);
        tabHost.addTab(tab2);
        tabHost.addTab(tab3);
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

/**
 * 获取通讯录的联系人数
 */
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
