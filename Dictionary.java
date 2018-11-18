package droid.com.intelli_note;

public class Dictionary {

    private boolean del; // 삭제 체크 유무
    private String title; // 제목(내용)
    private String date; // 날짜

    public Dictionary(boolean del, String title, String date) { // 메모장 데이터 객체

        setDel(del);
        setTitle(title);
        setDate(date);
    }

    public boolean isDel() {
        return del;
    }

    public void setDel(boolean del) {
        this.del = del;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
