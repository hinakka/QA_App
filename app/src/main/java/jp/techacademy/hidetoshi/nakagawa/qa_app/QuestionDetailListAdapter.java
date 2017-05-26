package jp.techacademy.hidetoshi.nakagawa.qa_app;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class QuestionDetailListAdapter extends BaseAdapter {
    private final static int TYPE_QUESTION = 0;
    private final static int TYPE_ANSWER = 1;

    private LayoutInflater mLayoutInflater = null;
    private Question mQuestion;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseReference;

    public QuestionDetailListAdapter(Context context, Question question) {
        mLayoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mQuestion = question;
    }

    @Override
    public int getCount() {
        return 1 + mQuestion.getAnswers().size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_QUESTION;
        } else {
            return TYPE_ANSWER;
        }
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public Object getItem(int position) {
        return mQuestion;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (getItemViewType(position) == TYPE_QUESTION) {
            if (convertView == null) {
                convertView = mLayoutInflater.inflate(R.layout.list_question_detail, parent, false);
            }

            final Button favB = (Button)convertView.findViewById(R.id.favoriteButton);
            FirebaseUser user = mAuth.getInstance().getCurrentUser();
            mDatabaseReference = FirebaseDatabase.getInstance().getReference();
            final DatabaseReference favRef = mDatabaseReference.child(Const.UsersPATH).child(user.getUid()).child(Const.FavPATH).child(mQuestion.getQuestionUid());

            favRef.addListenerForSingleValueEvent(new ValueEventListener() {
                  @Override
                  public void onDataChange(DataSnapshot dataSnapshot) {
                      HashMap map = (HashMap) dataSnapshot.getValue();
                      if (map == null) {
                          favB.setText("☆");
                      } else {
                          favB.setText("★");
                      }
                  };

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    System.out.println("The read failed: " + databaseError.getCode());
                }

              });

            favB.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    FirebaseUser user = mAuth.getInstance().getCurrentUser();
                    mDatabaseReference = FirebaseDatabase.getInstance().getReference();
                    final DatabaseReference favRef = mDatabaseReference.child(Const.UsersPATH).child(user.getUid()).child(Const.FavPATH).child(mQuestion.getQuestionUid());

                            if (favB.getText() == "☆"){
                                favB.setText("★");
                                String title = mQuestion.getTitle();
                                String name = mQuestion.getName();
                                String body = mQuestion.getBody();
                                String genre = String.valueOf(mQuestion.getGenre());

                                Map<String, String> data = new HashMap<String, String>();
                                data.put("title", title);
                                data.put("name", name);
                                data.put("body", body);
                                data.put("genre", genre);
                                favRef.setValue(data);
                            } else {
                                favB.setText("☆");
                                favRef.removeValue();
                            }
                }
            });

            String body = mQuestion.getBody();
            String name = mQuestion.getName();

            TextView bodyTextView = (TextView) convertView.findViewById(R.id.bodyTextView);
            bodyTextView.setText(body);

            TextView nameTextView = (TextView) convertView.findViewById(R.id.nameTextView);
            nameTextView.setText(name);

            byte[] bytes = mQuestion.getImageBytes();
            if (bytes.length != 0) {
                Bitmap image = BitmapFactory.decodeByteArray(bytes, 0, bytes.length).copy(Bitmap.Config.ARGB_8888, true);
                ImageView imageView = (ImageView) convertView.findViewById(R.id.imageView);
                imageView.setImageBitmap(image);
            }

        } else {
            if (convertView == null) {
                convertView = mLayoutInflater.inflate(R.layout.list_answer, parent, false);
            }

            Answer answer = mQuestion.getAnswers().get(position - 1);
            String body = answer.getBody();
            String name = answer.getName();

            TextView bodyTextView = (TextView) convertView.findViewById(R.id.bodyTextView);
            bodyTextView.setText(body);

            TextView nameTextView = (TextView) convertView.findViewById(R.id.nameTextView);
            nameTextView.setText(name);
        }

        return convertView;
    }
}