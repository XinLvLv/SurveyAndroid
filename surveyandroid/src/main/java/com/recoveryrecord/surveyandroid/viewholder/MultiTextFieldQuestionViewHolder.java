package com.recoveryrecord.surveyandroid.viewholder;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;

import com.recoveryrecord.surveyandroid.QuestionState;
import com.recoveryrecord.surveyandroid.question.MultiTextFieldQuestion;

public class MultiTextFieldQuestionViewHolder extends QuestionViewHolder<MultiTextFieldQuestion> {

    public MultiTextFieldQuestionViewHolder(Context context, @NonNull View itemView) {
        super(context, itemView);
    }

    public void bind(MultiTextFieldQuestion question, QuestionState questionState) {
        super.bind(question);
        // TODO
    }
}
