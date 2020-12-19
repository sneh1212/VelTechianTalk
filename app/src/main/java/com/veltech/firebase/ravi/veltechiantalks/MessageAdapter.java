package com.veltech.firebase.ravi.veltechiantalks;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.github.library.bubbleview.BubbleTextView;
import com.veltech.firebase.ravi.veltechiantalks.FriendlyMessage;

import android.text.format.DateFormat;
import java.util.List;

import uk.co.senab.photoview.PhotoViewAttacher;

public class MessageAdapter extends ArrayAdapter<FriendlyMessage> {
    public MessageAdapter(Context context, int resource, List<FriendlyMessage> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.item_message, parent, false);
        }

        ImageView photoImageView = (ImageView) convertView.findViewById(R.id.photoImageView);
        BubbleTextView messageTextView = (BubbleTextView) convertView.findViewById(R.id.messageTextView);
        TextView authorTextView = (TextView) convertView.findViewById(R.id.nameTextView);

        TextView messTime=(TextView)convertView.findViewById(R.id.message_time);
        FriendlyMessage message = getItem(position);

        boolean isPhoto = message.getPhotoUrl() != null;
        if (isPhoto) {
            messageTextView.setVisibility(View.GONE);
            photoImageView.setVisibility(View.VISIBLE);
            Glide.with(photoImageView.getContext())
                    .load(message.getPhotoUrl())
                    .into(photoImageView);
            PhotoViewAttacher photoViewAttacher=new PhotoViewAttacher(photoImageView);
            photoViewAttacher.update();
        } else {
            messageTextView.setVisibility(View.VISIBLE);
            photoImageView.setVisibility(View.GONE);
            messageTextView.setText(message.getText());
        }
        authorTextView.setText(message.getName());

        messTime.setText(DateFormat.format("dd-MM-yyyy (HH:mm:ss)",message.getMessageTime()));

        return convertView;
    }
}
