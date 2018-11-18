package droid.com.intelli_note;

import java.util.ArrayList;
import java.util.List;

// 딕셔너리를 관리하는 객체
public class Manager {
    private ArrayList<Dictionary> data=new ArrayList<>();


    //MemoLIst와 MemoEdit에서 Manager를 공유해야되므로 싱글톤 패턴이라는 공유 객체로 만들어 준다.
    //싱글톤 패턴
    //2
    private static Manager instance=new Manager();
    //1 new Manager()불가능
    private Manager(){
    }
    //3
    public static Manager getInstance(){
        return instance;
    }

    public void add(Dictionary d){
        data.add(d);
    }

    public Dictionary get(int index){
        return data.get(index);
    }

    public void update(int index,Dictionary d){
        data.set(index,d);
    }

    public void delete(int index){
        data.remove(index);
    } // 인덱스를 통해서 딕셔너리를 삭제

    public void delete(Dictionary d){
        data.remove(d);
    }  // 딕셔너리를 하나 삭제

    public int getCount(){return data.size();} // 딕셔너리 개수 반환

    public ArrayList<Dictionary> getData() {
        return data;
    } // 딕셔너리 리스트 전체 반환

    public void clear(){ // 전체 데이터 삭제
        data.clear();
    }

}
