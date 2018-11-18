package droid.com.intelli_note;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {


    boolean isPageOpen=false; // 프레임 옮겨졌는지의 여부
    Animation translateLeftAnim;
    Animation translateRightAnim;

    ConstraintLayout page;
    private SQLiteDatabase db;
    private final String databaseName="Intelli_Note";
    private final String tableName="memo";
    private final String pictableName="picture";
    private int index;
    private CustomAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private Manager mgr=Manager.getInstance();
    private int del_position=-1;
    //private int count = -1;


    @TargetApi(Build.VERSION_CODES.M)
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db=openOrCreateDatabase(databaseName,MODE_ENABLE_WRITE_AHEAD_LOGGING,null);// 데이터 베이스 생성
/*        db.execSQL("drop table if exists memo");
        db.execSQL("drop table if exists picture");*/
        db.execSQL("create table if not exists "+tableName+"("
                +"_id integer PRIMARY KEY,"
                +"content text,"
                +"date text,"
                +"picture_num integer DEFAULT 0);");
        db.execSQL("create table if not exists "+pictableName+"("
                +"_id integer ,"
                +"idx integer DEFAULT 0,"
                +"picture BLOB," +
                "CONSTRAINT _id_fk FOREIGN KEY(_id)" +
                "REFERENCES "+tableName+"(_id)," +
                "PRIMARY KEY(_id,idx));");
        page = (ConstraintLayout)findViewById(R.id.page); // 화면 이동을 위한 프레임.
        index = -1;

        translateLeftAnim= AnimationUtils.loadAnimation(this,R.anim.translate_left); // 프레임 이동을 위한 애니메이션 함수
        translateRightAnim= AnimationUtils.loadAnimation(this,R.anim.translate_right); // 프레임 이동을 위한 애니메이션 함수

        SlidingPageAnimationListener animListener=new SlidingPageAnimationListener(); // 프레임 이동을 위한 애니메이션 함수
        translateLeftAnim.setAnimationListener(animListener); // 프레임 이동을 위한 애니메이션 함수
        translateRightAnim.setAnimationListener(animListener); // 프레임 이동을 위한 애니메이션 함수

        mRecyclerView = (RecyclerView)findViewById(R.id.recyclerview_main_list);
        mLinearLayoutManager = new LinearLayoutManager(this); // 리사이클 뷰 생성
        mRecyclerView.setLayoutManager(mLinearLayoutManager); //
        mLinearLayoutManager.setSmoothScrollbarEnabled(true);
        // RecyclerView를 위해 CustomAdapter를 사용합니다.
        mAdapter = new CustomAdapter(mgr.getData()); // 리사이클 뷰의 사용자 정의 어뎁터

        mRecyclerView.setAdapter(mAdapter); // 어뎁터 달기

        mRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getApplicationContext(), mRecyclerView, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) { // 뷰 클릭을 할 시에 생성
                if(isPageOpen) { // 삭제를 누르면 isPageOpen이 1이 된다. 그러면 리사이클 뷰 del 설정 가능.
                    Dictionary d = mAdapter.getDictionary(position); // index의 번호를 가져온다.
                    if (d.isDel()) { // 토글 방식 구현
                        d.setDel(false);
                    } else {
                        d.setDel(true);
                    }
                }else{
                    //Intent로 position 넘겨 주어서 편집으로 화면으로 화면 전환
                    String SQL="select _id from "+tableName +" order by date desc";
                    Cursor c=db.rawQuery(SQL,null);
                    c.move(position+1);

                    Intent intent=new Intent();
                    intent.putExtra("index",c.getInt(0));
                    intent.setClass(MainActivity.this,NextActivity.class);
                    startActivity(intent);
                    //Toast.makeText(getApplicationContext(), position + "번 째 아이템 클릭", Toast.LENGTH_SHORT).show();
                }
                mAdapter.notifyDataSetChanged(); //변경된 데이터를 화면에 반영
            }

            //길게 누를시의 핸들러. 삭제를 할수 있음
            @Override
            public void onLongItemClick(View view, int position) {
                if(isPageOpen) return ;
                Activity activity=MainActivity.this;
                del_position=position;

                activity.openContextMenu(mRecyclerView);
                // Toast.makeText(getApplicationContext(), position + "번 째 아이템 롱 클릭", Toast.LENGTH_SHORT).show();
            }
        }));

        this.registerForContextMenu(mRecyclerView); // context 메뉴를 나의 리사이클 뷰에 붙인다.

        // RecyclerView의 줄(row) 사이에 수평선을 넣기위해 사용됩니다. 데코레이션
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
                mLinearLayoutManager.getOrientation());
        mRecyclerView.addItemDecoration(dividerItemDecoration);

    }

    @Override
    protected void onStart() { //
        super.onStart();
        mgr.clear(); // mgr을 통해서 일단 초기화 시켜버림

        String SQL="select * from "+tableName+" order by date desc";
        Cursor c=db.rawQuery(SQL,null);

        for(int i=0;i<c.getCount();i++){ // DB에서 전체 데이터를 초기화된 mgr 메니저로 넣는 과정
            c.moveToNext();
            mgr.add(new Dictionary(false,c.getString(1).length()<10?c.getString(1):c.getString(1).substring(0,10),c.getString(2)));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAdapter.notifyDataSetChanged();;
    }

    @Override
    public void onBackPressed() { //뒤로가리 버튼을 눌렀을 때 실행
        if(isPageOpen){ // 삭제버튼 눌렀을 때 pageOpen
            page.startAnimation(translateLeftAnim); // 애니메이션
            if(mgr.getCount()!=0) // 데이터의 개수가 0개 이상일 때만 실행
                for(int i=0;i<mgr.getCount();i++){
                    mgr.get(i).setDel(false); // del을 다 false로 만들어 준다.
                }
            return ;
        }
        super.onBackPressed();
    }

    ///꾹 눌렀을 때 나오는 ContextMenu 생성
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(Menu.NONE,101,Menu.NONE,"삭제");
    }

    //"삭제" ContextItem이 선택되면 해당 position에 있는 Item을 삭제
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if(item.getItemId()==101){ // 삭제 버튼이 눌렀을 때 실행
            String SQL="select _id from "+tableName +" order by date desc"; // 데이터 받아오기
            Cursor c=db.rawQuery(SQL,null); // 실제 데이터베이스에 내림차순된 id를 받아온다.
            c.move(del_position+1); // recycleview의 인덱스를 통해서 접근해서 데이터베이스의 아이디를 받아옴.

            String deleteSQL="delete from "+tableName+" where _id="+c.getInt(0);
            db.execSQL(deleteSQL); // 데이터 베이스에서 삭제

            mgr.delete(del_position); // 리사이클뷰에서 삭제
            mAdapter.notifyDataSetChanged(); //변경된 데이터를 화면에 반영
            return true;
        }
        return super.onContextItemSelected(item);
    }

    //옵션 메뉴 생성
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    //옵션 메뉴가 선택 되었을 때
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.mi_add: // 추가, 인텐트를 넘김
                Intent intent=new Intent();
                intent.setClass(MainActivity.this,NextActivity.class);
                startActivity(intent);
                break;
            case R.id.mi_del: // 삭제
                if(mAdapter.getItemCount()==0)break;//선택된 아이템이 없으면 리턴
                if(isPageOpen){ // 삭제 페이지가 열려 있으면 선택된 아이템들 삭제
                    String selectSQL="select _id from "+tableName +" order by date desc";

                    Cursor c=db.rawQuery(selectSQL,null);
                    for(int i=0;i<mgr.getCount();i++){ // 복수선택이 될 수 있으므로 루프문 설정
                        Dictionary d=mgr.get(i);
                        c.moveToNext();
                        if(d.isDel()==true){ // 선택된 친구들 삭제
                            String deleteSQL="delete from "+tableName+" where _id="+c.getInt(0);
                            db.execSQL(deleteSQL);
                            mgr.delete(i--);
                        }
                    }
                    page.startAnimation(translateLeftAnim);
                }else{ //RadionButton이 보여지게 프레임을 움직인다.
                    page.startAnimation(translateRightAnim);
                }
                break;
        }
        mAdapter.notifyDataSetChanged(); //변경된 데이터를 화면에 반영

        return super.onOptionsItemSelected(item);
    }
    private class SlidingPageAnimationListener implements Animation.AnimationListener{ // isPageOpen의 이벤트 핸들러
        @Override
        public void onAnimationStart(Animation animation) {
            if(isPageOpen) {
                isPageOpen = false;
            }else{
                isPageOpen=true;
            }
        }

        @Override
        public void onAnimationEnd(Animation animation) {
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    }
}