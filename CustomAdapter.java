package droid.com.intelli_note;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.ArrayList;


public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.CustomViewHolder> {

    private ArrayList<Dictionary> mList;

    public class CustomViewHolder extends RecyclerView.ViewHolder {
        protected RadioButton del; // 리사이클 뷰에 1개에 나타나는 애들
        protected TextView title;
        protected TextView date;

        public CustomViewHolder(View view) { // 생성자
            super(view);
            this.del = (RadioButton) view.findViewById(R.id.del);
            this.title = (TextView) view.findViewById(R.id.title);
            this.date = (TextView) view.findViewById(R.id.date);
        }

    }
    public CustomAdapter(ArrayList<Dictionary> list) {
        this.mList = list;
    } // 리스트를 어뎁터 안에 넣는다. 어뎁터의 함수로 리스트뷰를 수정한다.

    // RecyclerView에 새로운 데이터를 보여주기 위해 필요한 ViewHolder를 생성해야 할 때 호출됩니다.
    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_list, null);
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_list, viewGroup, false);

        CustomViewHolder viewHolder = new CustomViewHolder(view);

        return viewHolder;
    }

    // Adapter의 특정 위치(position)에 있는 데이터를 보여줘야 할때 호출됩니다.
    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder viewholder, int position) { // 셋팅

        //viewholder.del.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);
        viewholder.title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 35);
        viewholder.date.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);

        viewholder.itemView.setLayoutParams(new RecyclerView.LayoutParams(1000,200));
        viewholder.del.setGravity(Gravity.CENTER);
        viewholder.title.setGravity(Gravity.LEFT);
        viewholder.date.setGravity(Gravity.LEFT);

        viewholder.del.setChecked(mList.get(position).isDel());
        viewholder.title.setText(mList.get(position).getTitle());
        viewholder.date.setText(mList.get(position).getDate());
    }

    @Override
    public int getItemCount() {
        return (null != mList ? mList.size() : 0);
    }

    public Dictionary getDictionary(int position){
        return mList.get(position);
    }

}
