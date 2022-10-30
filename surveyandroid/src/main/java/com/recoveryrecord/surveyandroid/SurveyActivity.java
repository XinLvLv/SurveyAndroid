package com.recoveryrecord.surveyandroid;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.recoveryrecord.surveyandroid.condition.CustomConditionHandler;
import com.recoveryrecord.surveyandroid.validation.DefaultValidator;
import com.recoveryrecord.surveyandroid.validation.FailedValidationListener;
import com.recoveryrecord.surveyandroid.validation.Validator;

import org.json.JSONObject;

public abstract class SurveyActivity extends AppCompatActivity implements FailedValidationListener {
    private static final String TAG = SurveyActivity.class.getSimpleName();

    public static final String JSON_FILE_NAME_EXTRA = "json_filename";
    public static final String SURVEY_TITLE_EXTRA = "survey_title";

    private SurveyState mState;
    private OnSurveyStateChangedListener mOnSurveyStateChangedListener;
    private RecyclerView mRecyclerView;
    private JSONObject questions;
    private JSONObject original_questions;

    public void setQuestions(JSONObject questions) {
        this.questions = questions;
    }
    public JSONObject getQuestions(){
        return questions;
    }
    public void setOriginal_questions(JSONObject original_questions){this.original_questions = original_questions;}
    public JSONObject getOriginal_questions(){
        return getQuestions();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResId());
        setTitle(getSurveyTitle());

        SurveyQuestions surveyQuestions = null;
        try {
            surveyQuestions = createSurveyQuestions();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "Questions = " + surveyQuestions);

        mState = createSurveyState(surveyQuestions);
        setupRecyclerView();
    }

    protected @LayoutRes int getLayoutResId() {
        return R.layout.activity_survey;
    }

//    protected SurveyQuestions createSurveyQuestions() {
//        return SurveyQuestions.load(this, getJsonFilename());
//    }
    protected SurveyQuestions createSurveyQuestions() throws JsonProcessingException {
        Log.v("questions success",getQuestions().toString());
        return SurveyQuestions.load(this, getQuestions());
    }

    protected void setupRecyclerView() {
        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.addItemDecoration(new SpaceOnLastItemDecoration(getDisplayHeightPixels()));
        mRecyclerView.setItemAnimator(new SurveyQuestionItemAnimator());
        mRecyclerView.setAdapter(new SurveyQuestionAdapter(this, mState));
    }

    protected SurveyState createSurveyState(SurveyQuestions surveyQuestions) {
        return new SurveyState(surveyQuestions)
                .setValidator(getValidator())
                .setCustomConditionHandler(getCustomConditionHandler())
                .setSubmitSurveyHandler(getSubmitSurveyHandler())
                .setOriginal_question(original_questions)
                .initFilter();
    }

    private int getDisplayHeightPixels() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.heightPixels;
    }

    @Override
    protected void onStart() {
        super.onStart();
        mState.addOnSurveyStateChangedListener(getOnSurveyStateChangedListener());
    }

    @Override
    protected void onStop() {
        super.onStop();
        mState.removeOnSurveyStateChangedListener(getOnSurveyStateChangedListener());
    }

    protected String getSurveyTitle() {
        if (getIntent().hasExtra(SURVEY_TITLE_EXTRA)) {
            return getIntent().getStringExtra(SURVEY_TITLE_EXTRA);
        }
        return null;
    }

    protected String getJsonFilename() {
        if (getIntent().hasExtra(JSON_FILE_NAME_EXTRA)) {
            return getIntent().getStringExtra(JSON_FILE_NAME_EXTRA);
        }
        return null;
    }

    protected Validator getValidator() {
        return new DefaultValidator(this);
    }

    // Subclasses should return a non-null if they are using custom show_if conditions.
    protected CustomConditionHandler getCustomConditionHandler() {
        return null;
    }

    protected AnswerProvider getAnswerProvider() {
        return mState;
    }

    public void validationFailed(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public SubmitSurveyHandler getSubmitSurveyHandler() {
        return new DefaultSubmitSurveyHandler(this);
    }

    public RecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    public OnSurveyStateChangedListener getOnSurveyStateChangedListener() {
        if (mOnSurveyStateChangedListener == null) {
            mOnSurveyStateChangedListener = new DefaultOnSurveyStateChangedListener(this, getRecyclerView());
        }
        return mOnSurveyStateChangedListener;
    }

    public void setOnSurveyStateChangedListener(OnSurveyStateChangedListener listener) {
        mOnSurveyStateChangedListener = listener;
    }
}
