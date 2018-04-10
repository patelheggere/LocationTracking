
package patelheggere.com.locationtracking.models;

import com.google.gson.annotations.SerializedName;

@SuppressWarnings("unused")
public class UserDetails {

    @SerializedName("billing_plan_id")
    private Long mBillingPlanId;
    @SerializedName("created_date")
    private Long mCreatedDate;
    @SerializedName("email")
    private String mEmail;
    @SerializedName("first_name")
    private String mFirstName;
    @SerializedName("last_name")
    private String mLastName;
    @SerializedName("mobile_no")
    private String mMobileNo;
    @SerializedName("name")
    private String mName;
    @SerializedName("notifications")
    private Long mNotifications;
    @SerializedName("role")
    private Long mRole;
    @SerializedName("status")
    private Long mStatus;
    @SerializedName("uid")
    private String mUid;
    @SerializedName("updated_date")
    private Object mUpdatedDate;

    public Long getBillingPlanId() {
        return mBillingPlanId;
    }

    public void setBillingPlanId(Long billingPlanId) {
        mBillingPlanId = billingPlanId;
    }

    public Long getCreatedDate() {
        return mCreatedDate;
    }

    public void setCreatedDate(Long createdDate) {
        mCreatedDate = createdDate;
    }

    public String getEmail() {
        return mEmail;
    }

    public void setEmail(String email) {
        mEmail = email;
    }

    public String getFirstName() {
        return mFirstName;
    }

    public void setFirstName(String firstName) {
        mFirstName = firstName;
    }

    public String getLastName() {
        return mLastName;
    }

    public void setLastName(String lastName) {
        mLastName = lastName;
    }

    public String getMobileNo() {
        return mMobileNo;
    }

    public void setMobileNo(String mobileNo) {
        mMobileNo = mobileNo;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public Long getNotifications() {
        return mNotifications;
    }

    public void setNotifications(Long notifications) {
        mNotifications = notifications;
    }

    public Long getRole() {
        return mRole;
    }

    public void setRole(Long role) {
        mRole = role;
    }

    public Long getStatus() {
        return mStatus;
    }

    public void setStatus(Long status) {
        mStatus = status;
    }

    public String getUid() {
        return mUid;
    }

    public void setUid(String uid) {
        mUid = uid;
    }

    public Object getUpdatedDate() {
        return mUpdatedDate;
    }

    public void setUpdatedDate(Object updatedDate) {
        mUpdatedDate = updatedDate;
    }

}
