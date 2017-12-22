package com.example.jyn.remotemeeting.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.example.jyn.remotemeeting.Activity.Chat_A;
import com.example.jyn.remotemeeting.Activity.Main_after_login_A;
import com.example.jyn.remotemeeting.DataClass.Chat_log;
import com.example.jyn.remotemeeting.DataClass.Chat_room;
import com.example.jyn.remotemeeting.DataClass.Data_for_netty;
import com.example.jyn.remotemeeting.DataClass.Users;
import com.example.jyn.remotemeeting.Etc.Static;
import com.example.jyn.remotemeeting.Fragment.Chat_F;
import com.example.jyn.remotemeeting.R;
import com.example.jyn.remotemeeting.Util.Image_round_helper;
import com.example.jyn.remotemeeting.Util.Myapp;
import com.example.jyn.remotemeeting.Util.RetrofitService;
import com.example.jyn.remotemeeting.Util.ServiceGenerator;
import com.github.kimkevin.cachepot.CachePot;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.glide.transformations.CropCircleTransformation;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by JYN on 2017-12-09.
 */

public class RCV_chatRoom_list_adapter extends RecyclerView.Adapter<RCV_chatRoom_list_adapter.ViewHolder> {

    private Context context;
    private int itemLayout;
    private String request;
    private ArrayList<Chat_room> rooms;
    public static String TAG = "all_"+RCV_chatRoom_list_adapter.class.getSimpleName();
    public Myapp myapp;

    /** RecyclerAdapter 생성자 */
    public RCV_chatRoom_list_adapter(Context context, int itemLayout, ArrayList<Chat_room> rooms, String request) {
        Log.d(TAG, "ViewHolder_ RCV_chatRoom_list_adapter: 생성");
        this.context = context;
        this.itemLayout = itemLayout;
        this.rooms = rooms;
        this.request = request;

        // 어플리케이션 객체 생성
        myapp = Myapp.getInstance();
    }

    /** 뷰홀더 */
    class ViewHolder extends RecyclerView.ViewHolder {

        /** 버터나이프*/
        @BindView(R.id.container)                   LinearLayout container;
        @BindView(R.id.profile_img)                 ImageView profile_img;
        @BindView(R.id.cross_divider)               ImageView cross_divider;
        @BindView(R.id.title)                       TextView title;
        @BindView(R.id.counting)                    TextView counting;
        @BindView(R.id.content)                     TextView content;
        @BindView(R.id.time)                        TextView time;
        @BindView(R.id.unread_msg)                  TextView unread_msg;

        ViewHolder(View itemView) {
            super(itemView);
            Log.d(TAG, "ViewHolder");
            Log.d(TAG, "request: "+ request);
            ButterKnife.bind(this,itemView);

            container.setClickable(true);
            container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();
                    Log.d(TAG, "클릭 아이템 position: " + pos);
                    Log.d(TAG, "채팅방 no: " + rooms.get(pos).getChatroom_no());
                    Log.d(TAG, "채팅방 참여중인 유저 수: " + rooms.get(pos).getUser_nickname_arr().size());
                    Log.d(TAG, "채팅방의 마지막 메세지 no: " + rooms.get(pos).getLast_msg_no());
                    if(rooms.get(pos).getLast_log() != null) {
                        Log.d(TAG, "채팅방의 마지막 메세지 타입: " + rooms.get(pos).getLast_log().getMsg_type());
                        Log.d(TAG, "채팅방의 마지막 메세지 유저번호: " + rooms.get(pos).getLast_log().getUser_no());
                        Log.d(TAG, "채팅방의 마지막 메세지 내용: " + rooms.get(pos).getLast_log().getMsg_content());
                    }
                    for(int i=0; i<rooms.get(pos).getUser_nickname_arr().size(); i++) {
                        Log.d(TAG, "채팅방 유저 닉네임 리스트_"+ i + ": " + rooms.get(pos).getUser_nickname_arr().get(i));
                    }
                    for(int j=0; j<rooms.get(pos).getUser_img_filename_arr().size(); j++) {
                        Log.d(TAG, "채팅방 유저 프로필 URL_"+ j + ": " + rooms.get(pos).getUser_img_filename_arr().get(j));
                    }
                    Log.d(TAG, "채팅방의 마지막 메세지 전송 시각: " + rooms.get(pos).getTransmission_time_for_local());
                    Log.d(TAG, "채팅방의 안읽은 메세지 개수: " + rooms.get(pos).getUnread_msg_count());

                    // TODO: 해당 채팅방 액티비티로 이동
                    // CachePot 이용해서 클릭한 rooms 객체 전달
                    CachePot.getInstance().push("chat_room", rooms.get(pos));

                    // Chat_A 액티비티(채팅방) 열기
                    // 채팅방 리스트로부터 채팅방을 여는 것임을 intent 값으로 알린다
                    Intent intent = new Intent(context, Chat_A.class);
                    intent.putExtra("from", "list");
                    ((Main_after_login_A)context).startActivityForResult(intent, Main_after_login_A.REQUEST_CHAT_ROOM);
                }
            });
        }
    }

    /** onCreateViewHolder => 뷰홀더 생성 - 인플레이팅 */
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder");
        View view = LayoutInflater.from(parent.getContext()).inflate(itemLayout, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);

        return viewHolder;
    }

    /** onBindViewHolder => 리스트뷰의 getView 역할 */
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int pos) {
        Log.d(TAG, "onBindViewHolder");

        final ArrayList<Bitmap> temp_bitmap_arr = new ArrayList<>();

        // 메세지 내용 get
        String last_log_msg_content = "none exist";
        if(rooms.get(pos).getLast_log() != null) {
            last_log_msg_content = rooms.get(pos).getLast_log().getMsg_content();
        }

        // 메세지 서버 전송 시각 get
        String last_log_transmission_time_for_local = rooms.get(pos).getTransmission_time_for_local();
        // 총 방 인원 get
        int member_count = rooms.get(pos).getUser_nickname_arr().size();
        // 읽지 않은 메세지 수 get
        int unread_msg_count = rooms.get(pos).getUnread_msg_count();
        Log.d(TAG, "unread_msg_count: " + unread_msg_count);
        // 채팅방 제목 get
        String chat_room_title = rooms.get(pos).getChat_room_title();

        StringBuilder user_nickname_list = new StringBuilder();
        for(int i=0; i<rooms.get(pos).getUser_nickname_arr().size(); i++) {
            if(i==0) {
                user_nickname_list.append(rooms.get(pos).getUser_nickname_arr().get(i));
            }
            else {
                user_nickname_list.append(", ").append(rooms.get(pos).getUser_nickname_arr().get(i));
            }
        }

        StringBuilder user_img_filename_list = new StringBuilder();
        for(int j=0; j<rooms.get(pos).getUser_img_filename_arr().size(); j++) {
            if(j==0) {
                user_img_filename_list.append(rooms.get(pos).getUser_img_filename_arr().get(j));
            }
            else {
                user_img_filename_list.append(", ").append(rooms.get(pos).getUser_img_filename_arr().get(j));
            }
        }

        Log.d(TAG, "채팅방 유저 프로필 URL 리스트: " + user_img_filename_list);
        Log.d(TAG, "채팅방 유저 닉네임 리스트: " + user_nickname_list);
        Log.d(TAG, "채팅방 제목: " + chat_room_title);
        Log.d(TAG, "채팅방 참여중인 유저 수: " + member_count);
        Log.d(TAG, "채팅방의 마지막 메세지 내용: " + last_log_msg_content);
        Log.d(TAG, "채팅방의 마지막 메세지 전송 시각: " + last_log_transmission_time_for_local);
        Log.d(TAG, "채팅방의 안읽은 메세지 개수: " + unread_msg_count);

        // 만약 들어 있는 이미지 URL 개수가 2개라면, 즉 1:1 채팅방이라면
        // 이미지 URL - 채팅방 표시에 들어갈 이미지 URL은 상대방이 되어야 한다
        // 닉네임 - 채팅방 표시에 들어갈 닉네임은 상대방만 있으면 된다
        String img_URL_for_setting = "";
        String nickName_for_setting = "";
        /** 1:1 채팅 일때 */
        if(member_count == 2) {
            // 이미지 URL
            if(rooms.get(pos).getUser_img_filename_arr().get(0).equals(myapp.getUser_img_filename())) {
                img_URL_for_setting = rooms.get(pos).getUser_img_filename_arr().get(1);
            }
            else if(rooms.get(pos).getUser_img_filename_arr().get(1).equals(myapp.getUser_img_filename())) {
                img_URL_for_setting = rooms.get(pos).getUser_img_filename_arr().get(0);
            }
            // 닉네임
            if(rooms.get(pos).getUser_nickname_arr().get(0).equals(myapp.getUser_nickname())) {
                nickName_for_setting = rooms.get(pos).getUser_nickname_arr().get(1);
            }
            else if(rooms.get(pos).getUser_nickname_arr().get(1).equals(myapp.getUser_nickname())) {
                nickName_for_setting = rooms.get(pos).getUser_nickname_arr().get(0);
            }
        }

        ArrayList<Users> temp_users = null;

        /** 그룹 채팅 일 때 */
        if(member_count > 2) {
            // 임시 Chat_room ArrayList 생성
            temp_users = rooms.get(pos).getUser_arr();

            Log.d(TAG, "제거 전, temp_users.size(): " + temp_users.size());
            // 임시 Chat_room 에서 user 객체 ArrayList들 중, 내 user 객체 삭제
            for(int p = 0; p<temp_users.size(); p++) {
                if(temp_users.get(p).getUser_no().equals(myapp.getUser_no())) {
                    temp_users.remove(p);
                }
            }
            Log.d(TAG, "제거 후, temp_users.size(): " + temp_users.size());

            StringBuilder stringBuilder = new StringBuilder();

            for(int y=0; y<temp_users.size(); y++) {
                if(y == temp_users.size()-1) {
                    stringBuilder.append(temp_users.get(y).getUser_nickname());
                }
                else {
                    stringBuilder.append(temp_users.get(y).getUser_nickname()).append(", ");
                }
            }
            Log.d(TAG, "stringBuilder: " + stringBuilder);
            nickName_for_setting = String.valueOf(stringBuilder);
        }

        // 이미지 URL을 제외한, 데이터 셋팅
//        holder.nickNames.setText(nickName_for_setting);
        if(chat_room_title.equals("none")) {
            holder.title.setText(nickName_for_setting);
        }
        else if(!chat_room_title.equals("none")) {
            holder.title.setText(chat_room_title);
        }
        holder.content.setText(last_log_msg_content);
        holder.counting.setText(String.valueOf(member_count));
        holder.time.setText(last_log_transmission_time_for_local);
        holder.unread_msg.setText(String.valueOf(unread_msg_count));

        // 1:1 채팅 기준, 이미지 URL로 셋팅
        if(member_count <= 2) {
            if(img_URL_for_setting.equals("none")) {
                holder.profile_img.setImageResource(R.drawable.default_profile);
            }
            else if(!img_URL_for_setting.equals("none")) {
                Glide
                    .with(context)
                    .load(Static.SERVER_URL_PROFILE_FILE_FOLDER + img_URL_for_setting)
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .bitmapTransform(new CropCircleTransformation(context))
                    .into(holder.profile_img);
            }
        }

        // 그룹 채팅 기준, 이미지 URL로 셋팅
        else if(member_count > 2) {
            if(rooms.get(pos).isCombined_img_ready()) {
                // 비트맵을 뷰에 셋팅하는데 글라이드를 이용
                // --> 비트맵을 동그랗게 자르기 위해서
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                rooms.get(pos).getCombined_img_bitmap().compress(Bitmap.CompressFormat.PNG, 100, stream);
                Glide.with(context)
                        .load(stream.toByteArray())
                        .asBitmap()
                        .transform(new CropCircleTransformation(context))
                        .into(holder.profile_img);

            }
//            else if(!rooms.get(pos).isCombined_img_ready()) {
//                // rooms.get(pos)를 복사하여 임시 객체 생성
//                final Chat_room temp_Chat_room = rooms.get(pos);
//                // 해당 chatroom_no를 복사하여, 임시로 target_chatRoom_no 담아놓기
//                final int target_chatRoom_no = temp_Chat_room.getChatroom_no();
//
//                // 이미지 URL을 담을 ArrayList 생성
//                ArrayList<String> temp_user_img_filename = new ArrayList<>();
//
//                // 채팅방에 참여중인 사람들의 이미지 URL 개수
//                final int temp_filename_arr_size = temp_Chat_room.getUser_img_filename_arr().size();
//
//                for(int i=0; i<temp_filename_arr_size; i++) {
//                    // 내 이미지 URL은 제외 + 프로필 없는 경우도 제외(none)
//                    if(temp_Chat_room.getUser_img_filename_arr().get(i).equals(myapp.getUser_nickname()) ||
//                            temp_Chat_room.getUser_img_filename_arr().get(i).equals("none")) {
//                        continue;
//                    }
//                    // ArrayList에 담기
//                    else {
//                        temp_user_img_filename.add(temp_Chat_room.getUser_img_filename_arr().get(i));
//                        Log.d(TAG, "temp_Chat_room.getUser_img_filename_arr().get(i): "
//                                + temp_Chat_room.getUser_img_filename_arr().get(i));
//                    }
//
//                    final int finalI = i;
//
//                    Glide
//                        .with(context)
//                        .load(Static.SERVER_URL_PROFILE_FILE_FOLDER + temp_Chat_room.getUser_img_filename_arr().get(i))
//                        .asBitmap()
//                        .into(new SimpleTarget<Bitmap>() {
//                            @Override
//                            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
//                                // 리소스 로드가 끝난 후 수행할 작업
//                                temp_bitmap_arr.add(resource);
//                                Log.d(TAG, "temp_bitmap_arr.size(): " + temp_bitmap_arr.size());
//
//                                if(finalI == temp_filename_arr_size-1) {
//                                    Log.d(TAG, "finalI: " + finalI);
//                                    Log.d(TAG, "temp_users_size: " + temp_filename_arr_size);
//
//                                    if(temp_bitmap_arr.size() < 4) {
//                                        int short_count = 4 - temp_bitmap_arr.size();
//
//                                        for(int i=0; i<short_count; i++) {
//                                            temp_bitmap_arr.add(myapp.get_default_img_bitmap());
//                                        }
//                                    }
//
//                                    // 해당 채팅방의 참여한 유저들의 비트맵들과, 채팅방 번호를 메소드로 넘김
//                                    // --> 이미지 4장 합쳐서 해당 아이템에 보여주기 위함
//                                    // 매개변수 1. 이 채팅방 번호
//                                    // 매개변수 2. 비트맵들을 담은 ArrayList
//                                    img_bitmap_put_complete(target_chatRoom_no, temp_bitmap_arr);
//                                }
//                            }
//                        });
//                }
//                Log.d(TAG, "temp_user_img_filename.size(): " + temp_user_img_filename.size());
//            }
        }


        // 채팅방 인원에 따른 '인원수' 표시 뷰 조절
        if(member_count <= 2) {
            holder.counting.setVisibility(View.GONE);
            holder.cross_divider.setVisibility(View.GONE);
        }
        else if(member_count > 2) {
            holder.counting.setVisibility(View.VISIBLE);
            holder.cross_divider.setVisibility(View.VISIBLE);
        }

        // 해당 채팅방, 읽지 않은 메세지 개수 셋팅
        if(unread_msg_count == 0) {
            holder.unread_msg.setVisibility(View.GONE);
        }
        else if(unread_msg_count > 0) {
            holder.unread_msg.setText(String.valueOf(unread_msg_count));
            holder.unread_msg.setVisibility(View.VISIBLE);
        }
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 전달받은 비트맵 4장을 정사각형의 하나의 배트맵으로 붙여서 리턴하기
     ---------------------------------------------------------------------------*/
    public void img_bitmap_put_complete(final int target_chatRoom_no, final ArrayList<Bitmap> this_arr) {
        Log.d(TAG, "test 메소드 들어옴");
        Log.d(TAG, "target_chatRoom_no: " + target_chatRoom_no);
        Log.d(TAG, "this_arr.size(): " + this_arr.size());

        new Thread() {
            @Override
            public void run() {
                int[] temp = random_pick(this_arr.size());

                BitmapFactory.Options option = new BitmapFactory.Options();
                option.inDither = true;
                option.inPurgeable = true;

                Bitmap bitmap = null;
//                Bitmap b1 = this_arr.get(temp[0]);
//                Bitmap b2 = this_arr.get(temp[1]);
//                Bitmap b3 = this_arr.get(temp[2]);
//                Bitmap b4 = this_arr.get(temp[3]);
                Bitmap b1 = this_arr.get(0);
                Bitmap b2 = this_arr.get(1);
                Bitmap b3 = this_arr.get(2);
                Bitmap b4 = this_arr.get(3);

                b1 = resizeBitmapImage(b1, 100);
                b2 = resizeBitmapImage(b2, 100);
                b3 = resizeBitmapImage(b3, 100);
                b4 = resizeBitmapImage(b4, 100);

                bitmap = Bitmap.createScaledBitmap(b1, b1.getWidth()+b1.getWidth(), b1.getHeight()+b1.getHeight(), true);

                Paint p = new Paint();
                p.setDither(true);
                p.setFlags(Paint.ANTI_ALIAS_FLAG);

                Canvas c = new Canvas(bitmap);
                c.drawBitmap(b1, 0, 0, p);
                c.drawBitmap(b2, 0, b1.getHeight(), p);
                c.drawBitmap(b3, b1.getWidth(), 0, p);
                c.drawBitmap(b4,b1.getWidth(),b1.getHeight(),p);

                b1.recycle();
                b2.recycle();
                b3.recycle();
                b4.recycle();

//                return bitmap;

                // 핸들러로 전달할 Message 객체 생성
                Message msg = Chat_F.handler.obtainMessage();
                // Message 객체에 넣을 bundle 객체 생성
                Bundle bundle = new Bundle();
                // bundle 객체에 'target_chatRoom_no' 변수 담기
                bundle.putString("target_chatRoom_no", String.valueOf(target_chatRoom_no));
                // Message 객체에 bundle, 'bitmap' 담기
                msg.setData(bundle);
                msg.obj = bitmap;
                // 핸들러에서 Message 객체 구분을 위한 'what' 값 설정
                msg.what = 1;
                // 핸들러로 Message 객체 전달
                Chat_F.handler.sendMessage(msg);
            }
        }.start();
    }


    /**---------------------------------------------------------------------------
     메소드 ==> bitmap을 원하는 resolution으로 사이즈 조절하기
     ---------------------------------------------------------------------------*/
    private Bitmap resizeBitmapImage(Bitmap source, int maxResolution) {
        int width = source.getWidth();
        int height = source.getHeight();
        int newWidth = width;
        int newHeight = height;
        float rate = 0.0f;

        if(width > height) {
            if(maxResolution < width) {
                rate = maxResolution / (float) width;
                newHeight = (int) (height * rate);
                newWidth = maxResolution;
            }
        }
        else {
            if(maxResolution < height) {
                rate = maxResolution / (float) height;
                newWidth = (int) (width * rate);
                newHeight = maxResolution;
            }
        }

        return Bitmap.createScaledBitmap(source, newWidth, newHeight, true);
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 0 ~ size 사이에 중복되지 않는 랜덤한 숫자 4개들의 배열을 return 하는 메소드
                이미지 URL를 랜덤으로 고르기 위함
     ---------------------------------------------------------------------------*/
    public int[] random_pick(int size) {
        // 배열 생성
        int picked_imgs[] = new int[4];

        for(int i=0; i<picked_imgs.length; i++) {
            // 랜덤 값 반환
            picked_imgs[i] = (int)(Math.random()*size);

            // 중복 값 제거1
            for(int j=0; j<i; j++) {
                if(picked_imgs[i]==picked_imgs[j]){
                    i--;
                    break;
                }
            }
        }
        for(int k=0; k<picked_imgs.length; k++){
            Log.d(TAG, "picked_imgs["+ k +"]: " + picked_imgs[k]);
        }

        return picked_imgs;
    }



    /** getItemCount => arr 사이즈 리턴 */
    @Override
    public int getItemCount() {
        return rooms.size();
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 서버로부터 받아온 내 파트너 리스트로 arrayList 교체하기
     ---------------------------------------------------------------------------*/
    public void refresh_arr(ArrayList<Chat_room> rooms) {
        this.rooms.clear();
        this.rooms = rooms;
        notifyDataSetChanged();
    }


//    public void refresh_group_chat_Representative_image() {
//        Log.d(TAG, "refresh_group_chat_Representative_image: "
//                +" 들어옴!!!!!!!!!!!!!!!!!!!!");
//        // 그룹 채팅방 번호를 담을 어레이리스트 생성
//        ArrayList<Integer> temp_save_chatroom_no_arr = new ArrayList<>();
//
//        // 어플리케이션 객체에서 이미지를 combine한 채팅방 번호를 모두 가져오기
//        temp_save_chatroom_no_arr.addAll(myapp.getCombined_bitmap_hash().keySet());
//
//        for(int k=0; k<temp_save_chatroom_no_arr.size(); k++) {
//            for(int i=0; i<getItemCount(); i++) {
//                if(rooms.get(i).getChatroom_no() == temp_save_chatroom_no_arr.get(k)) {
//                    rooms.get(i).setCombined_img_ready(true);
//                    rooms.get(i).setCombined_img_bitmap(
//                            myapp.getCombined_bitmap_hash().get(temp_save_chatroom_no_arr.get(k)));
//                    notifyItemChanged(i);
//                }
//            }
//        }
//        // 비트맵 담겨져 있는 해쉬맵 초기화
//        myapp.getCombined_bitmap_hash().clear();
//    }


    public void set_group_chat_representatice_image(
            int target_chatroom_no, Bitmap combined_bitmap) {

        for(int i=0; i<getItemCount(); i++) {
            if(rooms.get(i).getChatroom_no() == target_chatroom_no) {
                rooms.get(i).setCombined_img_ready(true);
                rooms.get(i).setCombined_img_bitmap(combined_bitmap);
                notifyItemChanged(i);
            }
        }
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 해당 채팅방의 마지막 메시지 갱신
                (채팅방이 없다면, 채팅방 정보를 서버로 부터 받아와서, arrayList에 추가)
     ---------------------------------------------------------------------------*/
    public void update_last_msg(String message, Data_for_netty data) {
        // 채팅방 리스트를 업데이트 하라는 지시일 때
        if(message.equals("update")) {
            Log.d(TAG, "update_last_msg_ getItemCount(): " + getItemCount());
            int target_chatroom_no = data.getChat_log().getChat_room_no();
            Log.d(TAG, "target_chatroom_no: " + target_chatroom_no);

            // update되는 item의 chat_room_no 값을 담는 변수
            int update_chat_room_no = -1;

            // 현재 arrayList에 아이템이 있을 때, 즉 현재 채팅방 리스트를 가지고 있을 때
            if(getItemCount() > 0) {
                for(int i=0; i<rooms.size(); i++) {
                    // 내가 가지고 있는 채팅방 목록에서, 서버로 부터 받은 채팅메시지에 해당하는 채팅방을 찾아서
                    // (채팅방 no 비교)
                    // 해당 채팅방 목록의 Chat_log(last_chat_log) 의 목록을 바꿔치기 한 후 리사이클러뷰 갱신
                    if(rooms.get(i).getChatroom_no() == data.getChat_log().getChat_room_no()) {
                        rooms.get(i).setLast_log(data.getChat_log());
                        Log.d(TAG, "갱신된 msg_position: " + i);
                        Log.d(TAG, "갱신된 msg의 unread_msg_count: " + rooms.get(i).getLast_log().getMsg_unread_count());
                        rooms.get(i).setUnread_msg_count(rooms.get(i).getLast_log().getMsg_unread_count());

                        update_chat_room_no = rooms.get(i).getChatroom_no();
                        Log.d(TAG, "update_chat_room_no: " + update_chat_room_no);
                        notifyItemChanged(i);
                    }
                    // 마지막 item일 때
                    if(i == rooms.size()-1) {
                        /** 마지막 채팅 로그의 msg_no를 기준으로, 가장 최근에 받은 메세지가 최상단에 올 수 있도록,
                         *  Chat_room arrayList sort 하기 */
                        Collections.sort(rooms, new Comparator<Chat_room>() {
                            @Override
                            public int compare(Chat_room o1, Chat_room o2) {
                                Log.d(TAG, "o1.getLast_log().getMsg_no(): " + o1.getLast_log().getMsg_no());
                                Log.d(TAG, "o2.getLast_log().getMsg_no(): " + o2.getLast_log().getMsg_no());
                                if(o1.getLast_log().getMsg_no() < o2.getLast_log().getMsg_no()) {
                                    return 1;
                                }
                                else if(o1.getLast_log().getMsg_no() > o2.getLast_log().getMsg_no()) {
                                    return -1;
                                }
                                else {
                                    return 0;
                                }
                            }
                        });

                        // 그리고 해당 Chat_room item을 notify
                        for(int j=0; j<rooms.size(); j++) {
                            if(rooms.get(j).getChatroom_no() == update_chat_room_no) {
                                notifyItemChanged(j);
                            }
                        }
                    }
                }

                // update되는 item 이 없는 경우, 즉 해당 채팅 로그에 대한 채팅방 리스트를 내가 가지고 있지 않을 경우.
                if(update_chat_room_no == -1) {
                    /**
                     * 메소드 호출
                     * 일치하는 chatRoom이 없기 때문에, 서버로부터 이 채팅방 정보를 받아오기
                     */
                    Chat_room new_chat_room = get_new_chatroom_info(target_chatroom_no);
                    Log.d(TAG, "new_chat_room 정보_getMsg_content:  " + new_chat_room.getLast_log().getMsg_content());
                    Log.d(TAG, "new_chat_room 정보_getUser_nickname_arr:  " + new_chat_room.getUser_nickname_arr().toString());
                    Log.d(TAG, "new_chat_room 정보_getUnread_msg_count:  " + new_chat_room.getUnread_msg_count());

                    // 서버로부터 가져온 chat_room 객체를 arrayList에 추가하기
                    rooms.add(0, new_chat_room);
                    notifyItemInserted(0);
                    // 현재 스크롤이 최상단에 있을 때만, 스크롤을 이동 시킨다
                    if(!Chat_F.recyclerView.canScrollVertically(-1)) {
                        Chat_F.recyclerView.getLayoutManager().scrollToPosition(0);
                    }

                }

            }
            // 현재 arrayList에 아이템이 없을 때, 즉 현재 가지고 있는 채팅방 리스트가 하나도 없을 때
            else if(getItemCount() == 0) {
                /**
                 * 메소드 호출
                 * 채팅방 정보가 아예 하나도 없기 때문에, 서버로부터 이 채팅방 정보를 받아오기
                 */
                Chat_room new_chat_room = get_new_chatroom_info(target_chatroom_no);

                // 서버로부터 가져온 chat_room 객체를 arrayList에 추가하기
                rooms.add(0, new_chat_room);
                notifyItemInserted(0);
                if(!Chat_F.recyclerView.canScrollVertically(-1)) {
                    Chat_F.recyclerView.getLayoutManager().scrollToPosition(0);
                }

                // Chat_F 뷰 조정
                Chat_F.no_result.setVisibility(View.GONE);
                Chat_F.recyclerView.setVisibility(View.VISIBLE);
            }

        }
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 현재 arrayList에는 없는 chatroom 정보를 서버로 부터 받아오기
     ---------------------------------------------------------------------------*/
    @SuppressLint("StaticFieldLeak")
    private Chat_room get_new_chatroom_info(final int target_chatroom_no) {

        Chat_room new_chat_room = new Chat_room();
        final RetrofitService rs = ServiceGenerator.createService(RetrofitService.class);

        // 동기 호출
        try {
            final Chat_room final_new_chat_room = new_chat_room;
            return new AsyncTask<Void, Void, Chat_room>() {

                @Override
                protected Chat_room doInBackground(Void... voids) {
                    try {
                        Call<ResponseBody> call = rs.get_new_chatroom_info(
                                Static.GET_NEW_CHATROOM_INFO,
                                myapp.user_no, target_chatroom_no);
                        Response<ResponseBody> call_result = call.execute();
                        String retrofit_result = call_result.body().string();
                        Log.d(TAG, "retrofit_result: "+retrofit_result);

                        if(retrofit_result.equals("fail")) {
                            myapp.logAndToast("예외발생: " + retrofit_result);
                        }
                        else if(retrofit_result.equals("no_result")) {}

                        else {
                            // jsonString --> jsonObject
                            JSONObject jsonObject = new JSONObject(retrofit_result);
                            // jsonObject --> jsonArray
                            JSONArray jsonArray = jsonObject.getJSONArray(Chat_F.JSON_TAG_CHAT_ROOM_LIST);
                            Log.d(TAG, "jsonArray 개수: " + jsonArray.length());

                            if(jsonArray.length() == 1) {
                                String jsonString = jsonArray.getJSONObject(0).toString();
                                Log.d(TAG, "jsonString: " + jsonString);

                                // Chat_room 객체안의 세부 ArrayList 객체들 생성
                                ArrayList<Users> user_arr = new ArrayList<>();
                                ArrayList<String> user_nickname_arr = new ArrayList<>();
                                ArrayList<String> user_img_filename_arr = new ArrayList<>();

                                // 채팅방 번호
                                int chatroom_no = jsonArray.getJSONObject(0).getInt("chatroom_no");
                                // 채팅방 방장 번호
                                int chat_room_authority_user_no = jsonArray.getJSONObject(0).getInt("chat_room_authority_user_no");
                                // 해당 채팅방의 마지막 메세지 번호
                                int last_msg_no = jsonArray.getJSONObject(0).getInt("last_msg_no");

                                /**
                                 * 만약 마지막 메세지 번호가 '0' 이라면, 채팅 메세지가 하나도 오고간 적이 없는 것이므로, 무시한다
                                 *  ==> continue;
                                 * */
                                if(last_msg_no == 0) {
                                    return null;
                                }

                                // 해당 채팅방의 메시지들 중에서 내가 안 읽은 메세지의 개수
                                int unread_msg_count = jsonArray.getJSONObject(0).getInt("unread_msg_count");
                                // 채팅방 제목
                                String chat_room_title = jsonArray.getJSONObject(0).getString("chat_room_title");
                                Log.d(TAG, "chatroom_no: " + chatroom_no);
                                Log.d(TAG, "chat_room_authority_user_no: " + chat_room_authority_user_no);
                                Log.d(TAG, "last_msg_no: " + last_msg_no);
                                Log.d(TAG, "unread_msg_count: " + unread_msg_count);

                                // 데이터 클래스로 파싱하기 위한 GSON 객체 생성
                                Gson gson = new Gson();

                                // 'user_ob' JSONString을 JSONObect로 파싱
                                JSONArray jsonArray_for_user = new JSONArray(jsonArray.getJSONObject(0).getString("user_ob"));
                                Log.d(TAG, "jsonArray_for_user.toString(): " + jsonArray_for_user.toString());
                                Log.d(TAG, "jsonArray_for_user.length(): " + jsonArray_for_user.length());

                                // gson 이용해서 user 객체로 변환해서, 그 user 객체 안에서 닉네임과, 이미지 URL 값을 가져와서,
                                // 각 ArrayList 값에 add 한다
                                for(int k=0; k<jsonArray_for_user.length(); k++) {
                                    Users user = gson.fromJson(jsonArray_for_user.get(k).toString(), Users.class);
                                    Log.d(TAG, "user.getUser_nickname(): " + user.getUser_nickname());
                                    Log.d(TAG, "user.getUser_img_filename(): " + user.getUser_img_filename());
                                    user_arr.add(user);
                                    user_nickname_arr.add(user.getUser_nickname());
                                    user_img_filename_arr.add(user.getUser_img_filename());
                                }
                                /** try code - start */
                                // 그룹채팅방일 경우,
                                if(user_arr.size() > 2) {
                                    // 핸들러로 전달할 Message 객체 생성
                                    Message msg = Chat_F.handler.obtainMessage();
                                    // Message 객체에 넣을 bundle 객체 생성
                                    Bundle bundle = new Bundle();
                                    // bundle 객체에 'target_chatRoom_no' 변수 담기
                                    bundle.putInt("target_chatRoom_no", chatroom_no);
                                    // Message 객체에 bundle, 'bitmap' 담기
                                    msg.setData(bundle);
                                    msg.obj = user_arr;
                                    // 핸들러에서 Message 객체 구분을 위한 'what' 값 설정
                                    msg.what = 1;
                                    // 핸들러로 Message 객체 전달
                                    Chat_F.handler.sendMessage(msg);
                                }
                                /** try code - end */

                                // 채팅방 마지막 메세지, JSONArray를 파싱
                                JSONArray jsonArray_last_chat_log = new JSONArray(jsonArray.getJSONObject(0).getString("last_chat_log_ob"));
                                Log.d(TAG, "jsonArray_last_chat_log.toString(): " + jsonArray_last_chat_log.toString());
                                Log.d(TAG, "jsonArray_last_chat_log.length(): " + jsonArray_last_chat_log.length());

                                // gson 이용해서 Chat_log 객체로 변환
                                Chat_log last_chat_log = null;
                                // 마지막 Chat_log가 있을 때만 변환
                                if(jsonArray_last_chat_log.length() == 1) {
                                    last_chat_log = gson.fromJson(jsonArray_last_chat_log.get(0).toString(), Chat_log.class);
                                    Log.d(TAG, "jsonArray_last_chat_log.get(0).toString(): " + jsonArray_last_chat_log.get(0).toString());
                                    Log.d(TAG, "last_chat_log.getMsg_content(): " + last_chat_log.getMsg_content());
                                    Log.d(TAG, "last_chat_log.getChat_room_no(): " + last_chat_log.getChat_room_no());
                                }

                                /** final_new_chat_room 객체에 데이터 넣기 */
                                final_new_chat_room.setChatroom_no(chatroom_no);
                                final_new_chat_room.setLast_msg_no(last_msg_no);
                                final_new_chat_room.setUser_nickname_arr(user_nickname_arr);
                                final_new_chat_room.setUser_img_filename_arr(user_img_filename_arr);
                                // 마지막 Chat_log가 있을때만 데이터 넣음
                                if(jsonArray_last_chat_log.length() == 1) {
                                    final_new_chat_room.setLast_log(last_chat_log);
                                }
                                final_new_chat_room.setUnread_msg_count(unread_msg_count);
                                final_new_chat_room.setChat_room_title(chat_room_title);
                                final_new_chat_room.setUser_arr(user_arr);

                                return final_new_chat_room;
                            }
                        }
                    }
                    catch (JSONException e) {
                        e.printStackTrace();
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            }.execute().get();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
