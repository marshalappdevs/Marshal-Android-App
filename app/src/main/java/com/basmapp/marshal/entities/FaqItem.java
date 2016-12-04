package com.basmapp.marshal.entities;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import com.simplite.orm.DBObject;
import com.simplite.orm.annotations.Column;
import com.simplite.orm.annotations.Entity;
import com.simplite.orm.annotations.PrimaryKey;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Entity(tableName = FaqItem.TABLE_NAME)
public class FaqItem extends DBObject implements Parcelable {

    public static final String TABLE_NAME = "t_faq_item";

    public static final String COL_ID = "id";
    public static final String COL_QUESTION = "question";
    public static final String COL_ANSWER = "answer";
    public static final String COL_ANSWER_LINK = "answer_link";
    public static final String COL_ANSWER_ADDRESS = "answer_address";
    public static final String COL_ANSWER_PHONE_NUMBER = "answer_phone_number";
    public static final String COL_ANSWER_IMAGE_URL = "answer_image_url";
    public static final String COL_ORDER = "item_order";
    public static final String COL_IS_UP_TO_DATE = "is_up_to_date";
    public static final String COL_IS_RATED = "is_rated";
    public static final String COL_SEARCH_WORDS = "search_words";
    public static final String COL_ADDRESS_LATITUDE = "address_latitude";
    public static final String COL_ADDRESS_LONGITUDE = "address_longitude";

    @Expose
    @SerializedName("_id")
    @PrimaryKey(columnName = COL_ID)
    private String id;

    @Column(name = COL_QUESTION, options = {OPTION_UNIQUE})
    @Expose
    @SerializedName("Question")
    private String question;

    @Column(name = COL_ANSWER)
    @Expose
    @SerializedName("Answer")
    private String answer;

    @Column(name = COL_ANSWER_LINK)
    @Expose
    @SerializedName("Link")
    private String answerLink;

    @Column(name = COL_ANSWER_ADDRESS)
    @Expose
    @SerializedName("Address")
    private String answerAddress;

    @Column(name = COL_ADDRESS_LATITUDE)
    @Expose
    @SerializedName("Latitude")
    private double addressLatitude;

    @Column(name = COL_ADDRESS_LONGITUDE)
    @Expose
    @SerializedName("Longitude")
    private double addressLongitude;

    @Column(name = COL_ANSWER_PHONE_NUMBER)
    @Expose
    @SerializedName("PhoneNumber")
    private String answerPhoneNumber;

    @Column(name = COL_ANSWER_IMAGE_URL)
    @Expose
    @SerializedName("ImageUrl")
    private String answerImageUrl;

    @Column(name = COL_SEARCH_WORDS)
    @Expose
    @SerializedName("SearchWords")
    private String searchWords;

    @Column(name = COL_ORDER)
    @Expose
    @SerializedName("Order")
    private int order;

    @Expose
    @SerializedName("IsRated")
    @Column(name = COL_IS_RATED)
    private boolean isRated;

    @Column(name = COL_IS_UP_TO_DATE)
    private boolean isUpToDate;

    // Constructors
    public FaqItem(Context context) {
        super(context);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getAnswerLink() {
        return answerLink;
    }

    public void setAnswerLink(String answerLink) {
        this.answerLink = answerLink;
    }

    public String getAnswerAddress() {
        return answerAddress;
    }

    public void setAnswerAddress(String answerAddress) {
        this.answerAddress = answerAddress;
    }

    public String getAnswerPhoneNumber() {
        return answerPhoneNumber;
    }

    public void setAnswerPhoneNumber(String answerPhoneNumber) {
        this.answerPhoneNumber = answerPhoneNumber;
    }

    public String getAnswerImageUrl() {
        return answerImageUrl;
    }

    public void setAnswerImageUrl(String answerImageUrl) {
        this.answerImageUrl = answerImageUrl;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public boolean getIsUpToDate() {
        return isUpToDate;
    }

    public void setIsUpToDate(boolean isUpToDate) {
        this.isUpToDate = isUpToDate;
    }

    public Boolean getIsRated() {
        return isRated;
    }

    public void setIsRated(boolean isRated) {
        this.isRated = isRated;
    }

    public String getSearchWords() {
        return searchWords;
    }

    public void setSearchWords(String searchWords) {
        this.searchWords = searchWords;
    }

    public double getAddressLatitude() {
        return addressLatitude;
    }

    public void setAddressLatitude(double addressLatitude) {
        this.addressLatitude = addressLatitude;
    }

    public double getAddressLongitude() {
        return addressLongitude;
    }

    public void setAddressLongitude(double addressLongitude) {
        this.addressLongitude = addressLongitude;
    }

    ///////////////////// Parcelable methods //////////////////////

    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Storing the FAQ data to Parcel object
     **/
    @Override
    public void writeToParcel(Parcel dest, int i) {
        dest.writeString(id);
        dest.writeString(question);
        dest.writeString(answer);
        dest.writeString(answerLink);
        dest.writeString(answerAddress);
        dest.writeString(answerPhoneNumber);
        dest.writeString(answerImageUrl);
        dest.writeString(searchWords);
        dest.writeInt(order);
        dest.writeInt(isUpToDate ? 1 : 0);
        dest.writeInt(isRated ? 1 : 0);
    }

    /**
     * Retrieving FAQ data from Parcel object
     * This constructor is invoked by the method createFromParcel(Parcel source) of
     * the object CREATOR
     **/
    private FaqItem(Parcel in) {
        this.id = in.readString();
        this.question = in.readString();
        this.answer = in.readString();
        this.answerLink = in.readString();
        this.answerAddress = in.readString();
        this.answerPhoneNumber = in.readString();
        this.answerImageUrl = in.readString();
        this.searchWords = in.readString();
        this.order = in.readInt();
        this.isUpToDate = in.readInt() != 0;
        this.isRated = (in.readInt() != 0);
    }

    public static final Parcelable.Creator<FaqItem> CREATOR = new Parcelable.Creator<FaqItem>() {

        @Override
        public FaqItem createFromParcel(Parcel source) {
            return new FaqItem(source);
        }

        @Override
        public FaqItem[] newArray(int size) {
            return new FaqItem[size];
        }
    };
}