package com.example.wanderwand.utilities;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.wanderwand.R;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import objects.Feedback;

public class BugReportAdapter extends ArrayAdapter<Feedback> {

    private Context mContext;
    private List<Feedback> mFeedbacks;
    private LayoutInflater mInflater;

    public BugReportAdapter(Context context, List<Feedback> feedbacks) {
        super(context, R.layout.issues_list_item, feedbacks);
        mContext = context;
        mFeedbacks = feedbacks;
        mInflater = (LayoutInflater) mContext.getSystemService(AppCompatActivity.LAYOUT_INFLATER_SERVICE);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;
        View view = convertView;
        if (view == null) {
            view = mInflater.inflate(R.layout.issues_list_item, parent, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        String[] parts = mFeedbacks.get(position).getDateCreated().split("T");
        DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
        DateFormat outputFormat = new SimpleDateFormat("dd-MMM");
        Date date;
        String outputDate = null;
        try {
            date = inputFormat.parse(parts[0]);
            outputDate = outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        holder.issueDate.setText(outputDate);
        holder.issueText.setText(mFeedbacks.get(position).getFeedbackText());
        holder.issueType.setText(mFeedbacks.get(position).getFeedbackType());
        return view;
    }

    class ViewHolder {
        @BindView(R.id.issue_type)
        TextView issueType;
        @BindView(R.id.issue_text)
        TextView issueText;
        @BindView(R.id.issue_date)
        TextView issueDate;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

}

