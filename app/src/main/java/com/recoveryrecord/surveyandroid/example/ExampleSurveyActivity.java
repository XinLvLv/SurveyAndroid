package com.recoveryrecord.surveyandroid.example;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;

import com.recoveryrecord.surveyandroid.Answer;
import com.recoveryrecord.surveyandroid.R;
import com.recoveryrecord.surveyandroid.SurveyActivity;
import com.recoveryrecord.surveyandroid.condition.CustomConditionHandler;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.json.*;

public class ExampleSurveyActivity extends SurveyActivity implements CustomConditionHandler {
//    private String questionnaire;

    private volatile JSONObject q_json;

    public void setQ_json(JSONObject q_json) {
        this.q_json = q_json;
    }
    public JSONObject getQ_json(){
        return this.q_json;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            translateFromAPI("http://132.239.135.195:5000/questionnaire/BP_Daily");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.v("json object bbbbbb", q_json.toString());
        this.setQuestions(q_json);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected String getSurveyTitle() {
        return getString(R.string.example_survey);
    }

//    @Override
//    protected String getJsonFilename() {
//        return questionnaire;
//    }

    @Override
    protected CustomConditionHandler getCustomConditionHandler() {
        return this;
    }

    @Override
    public boolean isConditionMet(Map<String, Answer> answers, Map<String, String> extra) {
        String id = extra.get("id");
        if (id != null && id.equals("check_age")) {
            if (answers.get("birthyear") == null || answers.get("age") == null || extra.get("wiggle_room") == null) {
                return false;
            }
            String birthYearStr = answers.get("birthyear").getValue();
            Integer birthYear = Integer.valueOf(birthYearStr);
            String ageStr = answers.get("age").getValue();
            Integer age = Integer.valueOf(ageStr);
            Integer wiggleRoom = Integer.valueOf(extra.get("wiggle_room"));
            Calendar calendar = Calendar.getInstance();
            int currentYear = calendar.get(Calendar.YEAR);
            return Math.abs(birthYear + age - currentYear) > wiggleRoom;
        } else {
            return false;
        }
    }

    @Override
    public void onBackPressed() {
        if (!getOnSurveyStateChangedListener().scrollBackOneQuestion()) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.close_survey)
                    .setMessage(R.string.are_you_sure_you_want_to_close)
                    .setNeutralButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ExampleSurveyActivity.super.onBackPressed();
                }
            }).show();
        }
    }
//    public void setQuestionnaire(String filename){
//        questionnaire = filename;
//    }
//    public String getQuestionnaire(){
//        return questionnaire;
//    }
    public void translateFromAPI(String api) throws InterruptedException {
        //avoid performing a networking operation on its main thread.

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try  {
                    //Your code goes here
                    OkHttpClient okHttpClient = new OkHttpClient();
                    Request request = new Request.Builder().url(api).build();
                    try (Response response = okHttpClient.newCall(request).execute()) {
                        String responseBody = response.body().string();
//                        Log.v("getJsonFromInternet", responseBody);
                        // ... do something with response
                        setQ_json(translation(responseBody)); ;
//                        Log.v("return json", questions.toString());
                        Log.v("json object aaaaaaaa", getQ_json().toString());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
        thread.join();
    }
    public JSONObject translation(String apiJson) throws JSONException {
        JSONObject obj = new JSONObject(apiJson);
        JSONArray questions = obj.getJSONArray("questions");
//        Log.v("questions", questions.toString());
        JSONObject after_translation = new JSONObject();
        JSONArray after_translation_questions_array = new JSONArray();
        // store the info of the hook question;

        HashMap<String , HookQuestion> hook_questions = new HashMap<String, HookQuestion>();

        for (int i = 0; i < questions.length(); i++)
        {

            JSONObject question_details = new JSONObject();

            String question_id = questions.getJSONObject(i).getString("id");
            question_details.put("id", question_id);
            String header = questions.getJSONObject(i).getString("brief");
            question_details.put("header", header);
            String question = questions.getJSONObject(i).getString("text");
            question_details.put("question", question);
            String type = questions.getJSONObject(i).getString("type_string");

            if (type.equals("slider")){
                String question_type = "segment_select";
                JSONArray selections = questions.getJSONObject(i).getJSONArray("selections");
                String low_tag = selections.getJSONObject(0).getString("text");
                String high_tag = selections.getJSONObject(selections.length()-1).getString("text");
                JSONArray values = new JSONArray();
                for (int j = 0; j < selections.length(); j++){
                    values.put(selections.getJSONObject(j).getString("backend_string"));
                }
                question_details.put("question_type", question_type);
                question_details.put("low_tag", low_tag);
                question_details.put("high_tag", high_tag);
                question_details.put("values", values);

            }
            if (type.equals("multipleChoice")){
                String question_type = "single_select";
                JSONArray selections = questions.getJSONObject(i).getJSONArray("selections");
                JSONArray options = new JSONArray();

                for (int j = 0; j < selections.length(); j++){
                    options.put(selections.getJSONObject(j).getString("text"));
//                    Log.v("hook", selections.getJSONObject(j).getString("hook"));
                    if (selections.getJSONObject(j).getString("hook").equals("null")){
//                        Log.v("hook","equals to null");
                    }
                    else{
                        HookQuestion hookQuestion = new HookQuestion();
                        hookQuestion.setOn_hook_value(selections.getJSONObject(j).getString("text"));
                        hookQuestion.setOn_hook_id(question_id);
                        String hook_question_id = selections.getJSONObject(j).getString("hook");
                        hook_questions.put(hook_question_id,hookQuestion);
                    }
                }
                question_details.put("question_type", question_type);
                question_details.put("options", options);

            }
            if (type.equals("text")){
                String question_type = "single_text_area";
                question_details.put("question_type", question_type);
            }
            if (type.equals("numeric")){
                question_details.put("question_type", "single_text_field");
                question_details.put("input_type", "number");
            }
//            Log.v("hook_questions", hook_questions.toString());
            //hook questions
            if (hook_questions.containsKey(question_id)){
                HookQuestion hook_question = hook_questions.get(question_id);
                JSONObject show_if = new JSONObject();
                assert hook_question != null;
                show_if.put("id",hook_question.getOn_hook_id());
                show_if.put("operation","equals");
                show_if.put("value", hook_question.getOn_hook_value());
                question_details.put("show_if", show_if);
            }
            after_translation_questions_array.put(question_details);

        }
        after_translation.put("questions", after_translation_questions_array);
        JSONObject submit = new JSONObject();
        submit.put("button_title", "Submit Answer");
        submit.put("url", "http://132.239.135.195:5000/androidquestionnaire");
        after_translation.put("submit", submit);
        after_translation.put("auto_focus_text", true);
//        Log.v("translation", after_translation.toString());
        return after_translation;
    }
}


